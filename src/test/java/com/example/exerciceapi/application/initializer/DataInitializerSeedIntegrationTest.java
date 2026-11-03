package com.example.exerciceapi.application.initializer;

import application.initializer.DataInitializer;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import domain.model.exercise.Exercise;
import domain.model.exercise.Muscle;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.MuscleRepository;
import domain.port.storage.FileStorageService;
import domain.port.storage.ImageStorageService;
import domain.port.storage.StorageInfrastructureException;
import domain.port.storage.VideoStorageService;
import domain.validation.storage.ImageFileValidator;
import domain.validation.storage.VideoFileValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Seed semantics integration tests (spec domain seeding/ + J1/J2/D2/G7).
 *
 * <p>The {@code seed} profile stays INACTIVE in the shared test context: the seeder is
 * constructed manually with injectable fixture paths (design Decision 12) and a recording
 * storage fake, so every run is deterministic, in-memory and millisecond-fast. The
 * test-managed transaction isolates the shared H2 database (rollback after each test).
 *
 * <p>Counters are asserted through the DataInitializer's own summary logs (the only
 * observable seam — see apply-progress phase-4) plus database state and the fake's
 * recorded upload keys.
 */
@SpringBootTest
@Transactional
class DataInitializerSeedIntegrationTest {

    private static final Pattern CONVERTER_SAFE_KEY =
            Pattern.compile("^(images|videos)/[A-Za-z0-9._\\-]+$");
    private static final Pattern UUID_SEGMENT =
            Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Autowired
    private MuscleRepository muscleRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    private RecordingStorage storage;
    private Logger seedLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        storage = new RecordingStorage();
        seedLogger = (Logger) LoggerFactory.getLogger(DataInitializer.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        seedLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        seedLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    private DataInitializer initializer(String exerciseFile, String muscleFile) {
        return new DataInitializer(
                muscleRepository,
                exerciseRepository,
                new ObjectMapper(),
                new ImageStorageService(storage, new ImageFileValidator()),
                new VideoStorageService(storage, new VideoFileValidator()),
                exerciseFile,
                muscleFile);
    }

    private List<String> logMessages() {
        return logAppender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    private void assertLogContains(String needle) {
        List<String> matches = logMessages().stream().filter(m -> m.contains(needle)).toList();
        assertEquals(1, matches.size(),
                "expected exactly one log line containing [" + needle + "], got: " + logMessages());
    }

    // ------------------------------------------------------------------
    // 1. Fresh seed: deterministic keys, no placeholders, summaries (J2/G7/D1)
    // ------------------------------------------------------------------

    @Test
    void freshSeedPersistsDeterministicKeysAndNeverPlaceholderUrls() throws Exception {
        initializer("seedtest/exercises-happy.json", "seedtest/muscles-happy.json").run();

        Muscle muscleWithAsset = muscleRepository.findByName("SeedTestBenchMuscle").orElseThrow();
        Muscle muscleMissingAsset = muscleRepository.findByName("SeedTestSquatMuscle").orElseThrow();
        Exercise bench = exerciseRepository.findByName("SeedTestBenchPress").orElseThrow();
        Exercise squat = exerciseRepository.findByName("SeedTestFrontSquat").orElseThrow();

        // J2: deterministic keys derived from the asset basename — persisted verbatim
        assertEquals(List.of("images/0001-2gPfomN.jpg"), muscleWithAsset.getImages());
        assertEquals(List.of("images/0001-2gPfomN.jpg"), bench.getImages());
        assertEquals(List.of("videos/0001-2gPfomN.gif"), bench.getVideos());
        assertEquals(List.of("videos/0002-Hy9D21L.gif"), squat.getVideos());

        // D2: missing asset → empty list, record still created, placeholder never persisted
        assertEquals(List.of(), muscleMissingAsset.getImages());
        assertEquals(List.of(), squat.getImages());

        // No example.com anywhere in the database, no UUID keys (J2: no random component)
        for (Exercise e : exerciseRepository.findAll()) {
            e.getImages().forEach(v -> assertFalse(v.contains("example.com"), v));
            e.getVideos().forEach(v -> assertFalse(v.contains("example.com"), v));
        }
        for (Muscle m : muscleRepository.findAll()) {
            m.getImages().forEach(v -> assertFalse(v.contains("example.com"), v));
        }

        // J2: uploadAt called with EXACTLY the deterministic keys, in order, no UUIDs
        assertEquals(List.of(
                "images/0001-2gPfomN.jpg",
                "images/0001-2gPfomN.jpg",
                "videos/0001-2gPfomN.gif",
                "videos/0002-Hy9D21L.gif"), storage.uploadKeys);
        for (String key : storage.uploadKeys) {
            assertFalse(UUID_SEGMENT.matcher(key).find(), "seed keys must not contain UUIDs: " + key);
        }

        // G7: every key (recorded and persisted) survives the ListStringConverter round-trip
        for (String key : storage.uploadKeys) {
            assertTrue(CONVERTER_SAFE_KEY.matcher(key).matches(), key);
        }
        for (Exercise e : exerciseRepository.findAll()) {
            e.getImages().forEach(v -> assertTrue(CONVERTER_SAFE_KEY.matcher(v).matches(), v));
            e.getVideos().forEach(v -> assertTrue(CONVERTER_SAFE_KEY.matcher(v).matches(), v));
        }

        // Per-loop summaries: unique, created, already existed, failed, assets missing
        assertLogContains("Muscles: 2 unique, 2 created, 0 already existed, 0 failed, 1 assets missing");
        assertLogContains("Exercises: 2 unique, 2 created, 0 already existed, 0 failed, 1 assets missing");
    }

    // ------------------------------------------------------------------
    // 2. Missing asset → skip + counter, run keeps going (D2)
    // ------------------------------------------------------------------

    @Test
    void missingAssetCreatesRecordWithEmptyListAndCountsAssetsMissing() throws Exception {
        initializer("seedtest/exercises-happy.json", "seedtest/muscles-happy.json").run();

        // The run completed: both loops logged their summaries with the missing counters
        assertLogContains("Muscles: 2 unique, 2 created, 0 already existed, 0 failed, 1 assets missing");
        assertLogContains("Exercises: 2 unique, 2 created, 0 already existed, 0 failed, 1 assets missing");

        // Records exist with empty lists; assetsMissing is NOT counted as failed
        assertTrue(muscleRepository.findByName("SeedTestSquatMuscle").isPresent());
        assertTrue(exerciseRepository.findByName("SeedTestFrontSquat").isPresent());

        // Only the assets that exist were uploaded — missing ones never hit storage
        assertEquals(4, storage.uploadKeys.size());
        assertFalse(storage.uploadKeys.contains("images/missing-muscle-asset.jpg"));
        assertFalse(storage.uploadKeys.contains("images/missing-exercise-image.jpg"));
    }

    // ------------------------------------------------------------------
    // 3. Re-seed idempotency: skip existing, storage object count stable (J2)
    // ------------------------------------------------------------------

    @Test
    void reSeedSkipsExistingRecordsAndStorageObjectCountNeverGrows() throws Exception {
        DataInitializer seeder = initializer("seedtest/exercises-happy.json", "seedtest/muscles-happy.json");

        seeder.run();
        int uploadsAfterFirstRun = storage.uploadKeys.size();
        int objectsAfterFirstRun = storage.store.size();
        assertEquals(4, uploadsAfterFirstRun);
        assertEquals(3, objectsAfterFirstRun);

        logAppender.list.clear();
        seeder.run();
        // Skipped records never reach uploadAssets → assetsMissing counts 0 on re-runs
        assertLogContains("Muscles: 2 unique, 0 created, 2 already existed, 0 failed, 0 assets missing");
        assertLogContains("Exercises: 2 unique, 0 created, 2 already existed, 0 failed, 0 assets missing");
        assertEquals(uploadsAfterFirstRun, storage.uploadKeys.size(),
                "an already-seeded database must not touch storage again");
        assertEquals(objectsAfterFirstRun, storage.store.size());
        assertEquals(2, muscleRepository.findAll().size());
        assertEquals(2, exerciseRepository.findAll().size());

        logAppender.list.clear();
        seeder.run();
        assertLogContains("Muscles: 2 unique, 0 created, 2 already existed, 0 failed, 0 assets missing");
        assertEquals(uploadsAfterFirstRun, storage.uploadKeys.size());
        assertEquals(objectsAfterFirstRun, storage.store.size());
    }

    @Test
    void wipeAndReSeedReuploadsUnderTheExactSameKeys() throws Exception {
        DataInitializer seeder = initializer("seedtest/exercises-happy.json", "seedtest/muscles-happy.json");

        seeder.run();
        List<String> firstRunKeys = List.copyOf(storage.uploadKeys);
        List<String> firstRunObjects = List.copyOf(storage.store.keySet());

        exerciseRepository.findAll().forEach(e -> exerciseRepository.deleteById(e.getId()));
        muscleRepository.findAll().forEach(m -> muscleRepository.deleteById(m.getId()));
        storage.reset();

        seeder.run();

        assertEquals(firstRunKeys, storage.uploadKeys,
                "wipe + re-seed must re-upload every asset under its previous deterministic key");
        assertEquals(firstRunObjects, List.copyOf(storage.store.keySet()),
                "storage object count must converge, not grow");
        assertEquals(2, exerciseRepository.findAll().size());
    }

    // ------------------------------------------------------------------
    // 4. In-file duplicates are deduplicated, not failed
    // ------------------------------------------------------------------

    @Test
    void duplicateRowInsideSourceFileIsSeededOnce() throws Exception {
        // exercises-happy.json has 3 rows, the last one repeats "SeedTestBenchPress"
        initializer("seedtest/exercises-happy.json", "seedtest/muscles-happy.json").run();

        long persisted = exerciseRepository.findAll().stream()
                .filter(e -> "SeedTestBenchPress".equals(e.getName()))
                .count();
        assertEquals(1, persisted, "the duplicate row must be deduplicated by name");

        assertLogContains("Exercises: 2 unique, 2 created, 0 already existed, 0 failed, 1 assets missing");
    }

    // ------------------------------------------------------------------
    // 5. J1 data level: per-record isolation in BOTH loops + G7 guard
    // ------------------------------------------------------------------

    @Test
    void dataLevelFailuresAreCountedPerRecordAndDoNotAbortEitherLoop() throws Exception {
        // muscles-datafail: unsafe basename (G7: comma) between two good records
        // exercises-datafail: unsafe basename (space) + unresolvable mainMuscle + 2 good
        DataInitializer seeder = initializer("seedtest/exercises-datafail.json", "seedtest/muscles-datafail.json");

        seeder.run(); // must NOT throw

        assertLogContains("Muscles: 3 unique, 2 created, 0 already existed, 1 failed, 0 assets missing");
        assertLogContains("Exercises: 4 unique, 2 created, 0 already existed, 2 failed, 0 assets missing");

        // Good records on both sides of the bad ones were created → loops continued
        assertNotNull(muscleRepository.findByName("SeedTestGoodMuscle1").orElseThrow().getId());
        assertNotNull(muscleRepository.findByName("SeedTestGoodMuscle2").orElseThrow().getId());
        assertTrue(exerciseRepository.findByName("SeedTestGoodEx1").isPresent());
        assertTrue(exerciseRepository.findByName("SeedTestGoodEx2").isPresent());

        // Data-failed records were NOT persisted (no dead values)
        assertTrue(muscleRepository.findByName("SeedTestUnsafeMuscle").isEmpty());
        assertTrue(exerciseRepository.findByName("SeedTestUnsafeEx").isEmpty());
        assertTrue(exerciseRepository.findByName("SeedTestOrphanEx").isEmpty());

        // G7: the unsafe basenames were rejected before any storage or DB write
        assertTrue(storage.uploadKeys.isEmpty());
    }

    // ------------------------------------------------------------------
    // 6. J1 infrastructure level: fail-fast, counters-so-far, BOTH loops
    // ------------------------------------------------------------------

    @Test
    void infrastructureFailureAbortsWithCountersSoFarAfterDataFailures() {
        storage.failKey = "images/0003-1ZFqTDN.jpg";
        DataInitializer seeder =
                initializer("seedtest/exercises-infra.json", "seedtest/muscles-infra-precursor.json");

        assertThrows(StorageInfrastructureException.class, () -> seeder.run());

        // The muscle loop finished BEFORE the infra failure (no media uploads)
        assertLogContains("Muscles: 1 unique, 1 created, 0 already existed, 0 failed, 0 assets missing");

        // Data-level outcome before the abort was counted: 1 failed (unsafe basename)
        assertLogContains("Infrastructure failure — aborting exercise seed. Counters so far: "
                + "3 unique, 1 created, 0 already existed, 1 failed, 0 assets missing");

        // Exactly two storage operations: the successful Pre1 upload, then the failure
        assertEquals(2, storage.attempts, "run must abort at the FIRST infrastructure failure");
        assertEquals(List.of("images/0002-Hy9D21L.jpg"), storage.uploadKeys);

        // Created row kept; failed and aborted rows not persisted; later rows never reached
        assertTrue(exerciseRepository.findByName("SeedTestInfraPre1").isPresent());
        assertTrue(exerciseRepository.findByName("SeedTestInfraBadPre").isEmpty());
        assertTrue(exerciseRepository.findByName("SeedTestInfraPre2").isEmpty());
        assertTrue(exerciseRepository.findByName("SeedTestInfraPre3").isEmpty());

        // The exercise loop never completed → no exercise summary, no completion log
        assertTrue(logMessages().stream().noneMatch(m -> m.contains("Exercises: " + "4 unique")),
                "no completed-loop summary may be logged after an infra abort");
        assertTrue(logMessages().stream().noneMatch(m -> m.contains("Seed process completed")));
    }

    @Test
    void infrastructureFailureAbortsMuscleLoopOnFirstStorageAttempt() {
        storage.failKey = "images/0001-2gPfomN.jpg";
        DataInitializer seeder = initializer("seedtest/empty.json", "seedtest/muscles-infra.json");

        assertThrows(StorageInfrastructureException.class, () -> seeder.run());

        // Exactly ONE storage operation attempted — no stacked failures per record
        assertEquals(1, storage.attempts);

        // Counters-so-far logged before aborting (J1/spec: abort diagnostic includes them)
        assertLogContains("Infrastructure failure — aborting muscle seed. Counters so far: "
                + "0 created, 0 already existed, 0 failed, 0 assets missing");

        // Nothing persisted; the second muscle was never reached
        assertEquals(0, muscleRepository.findAll().size());

        // The exercise loop never started and the run never completed
        assertTrue(logMessages().stream().noneMatch(m -> m.contains("Exercises: ")));
        assertTrue(logMessages().stream().noneMatch(m -> m.contains("Seed process completed")));
    }

    // ------------------------------------------------------------------
    // 7. D2: zero muscle placeholders delivered → 40 created, assetsMissing=40
    // ------------------------------------------------------------------

    @Test
    void allFortyMusclePlaceholdersMissingStillSeedsAllFortyMuscles() throws Exception {
        // Real muscle.json (40 entries), zero assets on disk (D2 ground truth),
        // empty exercise file. A missing-asset condition must never fail the run.
        initializer("seedtest/empty.json", "static/data/muscle.json").run();

        List<Muscle> muscles = muscleRepository.findAll();
        assertEquals(40, muscles.size());
        for (Muscle m : muscles) {
            assertEquals(List.of(), m.getImages(), m.getName());
            m.getImages().forEach(v -> assertFalse(v.contains("example.com"), v));
        }

        assertTrue(storage.uploadKeys.isEmpty(), "no asset exists → zero uploads attempted");

        assertLogContains("Muscles: 40 unique, 40 created, 0 already existed, 0 failed, 40 assets missing");
        assertLogContains("Exercises: 0 unique, 0 created, 0 already existed, 0 failed, 0 assets missing");
    }

    // ------------------------------------------------------------------
    // Recording storage fake (in-memory) — J2 observation point
    // ------------------------------------------------------------------

    static class RecordingStorage implements FileStorageService {

        final List<String> uploadKeys = new ArrayList<>();
        final Map<String, byte[]> store = new LinkedHashMap<>();
        int attempts;
        /** When set, uploadAt throws StorageInfrastructureException for that exact key. */
        String failKey;

        @Override
        public String upload(MultipartFile file, String directory) {
            String key = directory + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            return uploadAt(file, key);
        }

        @Override
        public String uploadAt(MultipartFile file, String storageKey) {
            attempts++;
            if (storageKey.equals(failKey)) {
                throw new StorageInfrastructureException(
                        "Simulated infrastructure failure for key: " + storageKey,
                        new IOException("connection refused"));
            }
            uploadKeys.add(storageKey);
            try {
                store.put(storageKey, file.getBytes());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return storageKey;
        }

        @Override
        public void delete(String fileKey) {
            store.remove(fileKey);
        }

        @Override
        public String getUrl(String fileKey) {
            return "/media/" + fileKey;
        }

        void reset() {
            uploadKeys.clear();
            store.clear();
            attempts = 0;
            failKey = null;
        }
    }
}
