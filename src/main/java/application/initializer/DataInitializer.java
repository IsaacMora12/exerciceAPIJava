package application.initializer;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import application.services.storage.ClasspathMultipartFile;
import domain.model.exercise.Exercise;
import domain.model.exercise.Muscle;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.MuscleRepository;
import domain.port.storage.ImageStorageService;
import domain.port.storage.StorageInfrastructureException;
import domain.port.storage.VideoStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Seeds the database with muscles and exercises from JSON files.
 * Only runs when the "seed" profile is active.
 *
 * Idempotent PER RECORD: a record already present (matched by name) is skipped,
 * so a partial run can be completed by simply restarting the application.
 *
 * Media (seed-media-storage D1/J1/J2): every referenced asset is uploaded through
 * {@link ImageStorageService}/{@link VideoStorageService} — SAME validation chain as the
 * API path — and the DETERMINISTIC key ({@code images/<basename>}, no UUID) is what gets
 * persisted. Placeholder {@code example.com} URLs are never persisted. Missing/unreadable
 * assets (D2, e.g. the 40 muscle placeholders) skip the medium and increment
 * {@code assetsMissing}; the record is still created with an empty list.
 *
 * Failure semantics (J1): data-level errors are caught PER RECORD and counted
 * ({@code failed}), in BOTH loops; infrastructure errors surface as
 * {@link StorageInfrastructureException} from the adapters and are RETHROWN — the run
 * aborts at the first one with the counters reached so far logged (fail-fast, design
 * Decisions 1 and 8). {@link #run(String[])} deliberately does NOT catch them.
 *
 * Deliberately NOT transactional as a whole: each save runs in its own transaction
 * so a single bad record cannot poison the rest (a rollback-only transaction would
 * make every subsequent save fail with "current transaction is aborted").
 * NEVER re-introduce a class-level {@code @Transactional} here.
 *
 * Usage: --spring.profiles.active=dev,seed (local) or =garage,seed (S3/Garage).
 * {@code seed} alone fails fast at startup via ProfileWiringValidator.
 */
@Component
@Profile("seed")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final Long SYSTEM_USER_ID = 1L;

    private final MuscleRepository muscleRepository;
    private final ExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper;
    private final ImageStorageService imageStorageService;
    private final VideoStorageService videoStorageService;
    private final String exerciseFile;
    private final String muscleFile;

    /**
     * One-shot de Docker: el contenedor del seed corre con {@code --server.port=0}
     * (contexto servlet necesario para el wiring de Security) y Tomcat deja threads
     * no-daemon vivos — sin esto el JVM nunca terminaría y el contenedor queda
     * "running" para siempre. Default false: en {@code dev,seed} local NUNCA se
     * activa. Inyectado POR CAMPO para no tocar el constructor (el test construye
     * DataInitializer manualmente). Los FALLOS no llegan acá: la excepción escapa
     * de main() y el proceso sale con exit 1 (fail-fast).
     */
    @Value("${seed.exit-after-run:false}")
    private boolean exitAfterRun;

    public DataInitializer(MuscleRepository muscleRepository,
                           ExerciseRepository exerciseRepository,
                           ObjectMapper objectMapper,
                           ImageStorageService imageStorageService,
                           VideoStorageService videoStorageService,
                           @Value("${seed.exercise-file:static/data/exercise.json}") String exerciseFile,
                           @Value("${seed.muscle-file:static/data/muscle.json}") String muscleFile) {
        this.muscleRepository = muscleRepository;
        this.exerciseRepository = exerciseRepository;
        this.objectMapper = objectMapper;
        this.imageStorageService = imageStorageService;
        this.videoStorageService = videoStorageService;
        this.exerciseFile = exerciseFile;
        this.muscleFile = muscleFile;
    }

    /**
     * No class-level transaction on purpose — see the class javadoc.
     * Every repository.save() opens and commits its own transaction.
     *
     * No catch here either: a StorageInfrastructureException escaping a loop must
     * propagate out of the CommandLineRunner so startup aborts (fail-fast).
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 DataInitializer — starting seed (per-record idempotent)...");

        // 1. Load muscles
        MuscleSeedLoad muscles = loadMuscles();
        MuscleSeedResult muscleResult = muscles.result();
        log.info("✅ Muscles ready: {} ({} created, {} already existed, {} failed, {} assets missing)",
                muscleResult.unique(), muscleResult.created(), muscleResult.alreadyExisted(),
                muscleResult.failed(), muscleResult.assetsMissing());

        // 2. Load exercises
        ExerciseSeedResult exercises = loadExercises(muscles.nameToId());
        log.info("✅ Exercises ready: {} ({} created, {} already existed, {} failed, {} assets missing)",
                exercises.unique(), exercises.created(), exercises.alreadyExisted(),
                exercises.failed(), exercises.assetsMissing());

        log.info("🎉 Seed process completed! Muscles: {}, Exercises: {}",
                muscleResult.unique(), exercises.unique());

        // SOLO para el one-shot de Docker (--seed.exit-after-run=true): cerrar el
        // JVM aquí, DESPUÉS del log de éxito (nunca en finally ni antes de él).
        // System.exit(0) dispara los shutdown hooks de Spring → cierra contexto,
        // Tomcat e Hikari; el contenedor sale con exit 0 y deploy.sh no tinea el
        // wait-loop. Default false → en dev,seed local jamás se ejecuta; los
        // fallos ya salen con exit 1 porque la excepción no está acá.
        if (exitAfterRun) {
            log.info("seed.exit-after-run=true → System.exit(0) (one-shot de Docker)");
            System.exit(0);
        }
    }

    private MuscleSeedLoad loadMuscles() throws Exception {
        List<Map<String, Object>> muscleData = readSeedJson(muscleFile);

        Map<String, Long> nameToId = new LinkedHashMap<>();
        int created = 0;
        int alreadyExisted = 0;
        int failed = 0;
        int assetsMissing = 0;

        for (Map<String, Object> muscle : muscleData) {
            String name = null;
            try {
                name = (String) muscle.get("name");

                // Idempotency: reuse the existing record instead of duplicating it.
                // Its id MUST go into the map, otherwise exercises can't resolve it.
                Optional<Muscle> existing = muscleRepository.findByName(name);
                if (existing.isPresent()) {
                    nameToId.put(name.toLowerCase(), existing.get().getId());
                    alreadyExisted++;
                    continue;
                }

                String description = (String) muscle.getOrDefault("description", "");
                MediaResult images = uploadAssets(extractStringList(muscle.get("images")), "images");
                assetsMissing += images.missing();

                Muscle saved = muscleRepository.save(
                        Muscle.create(name, description, images.keys(), SYSTEM_USER_ID)
                );
                nameToId.put(name.toLowerCase(), saved.getId());
                created++;
                log.debug("  Muscle: {} → id={}", name, saved.getId());
            } catch (StorageInfrastructureException e) {
                // J1 fail-fast: log the counters reached, then abort the whole run.
                log.error("⛔ Infrastructure failure — aborting muscle seed. Counters so far: "
                                + "{} created, {} already existed, {} failed, {} assets missing",
                        created, alreadyExisted, failed, assetsMissing);
                throw e;
            } catch (Exception e) {
                // J1 data-level: one bad record must not sink the loop (this loop had
                // NO per-record catch before — a single failure aborted the runner).
                failed++;
                log.error("  ❌ Error loading muscle '{}': {}", name, e.getMessage());
            }
        }

        log.info("  Muscles: {} unique, {} created, {} already existed, {} failed, {} assets missing",
                muscleData.size(), created, alreadyExisted, failed, assetsMissing);
        return new MuscleSeedLoad(nameToId,
                new MuscleSeedResult(muscleData.size(), created, alreadyExisted, failed, assetsMissing));
    }

    /** Outcome of one exercise seeding pass. */
    record ExerciseSeedResult(int unique, int created, int alreadyExisted, int failed, int assetsMissing) {}

    /** Outcome of one muscle seeding pass. */
    record MuscleSeedResult(int unique, int created, int alreadyExisted, int failed, int assetsMissing) {}

    /** Name→id map must survive a partial pass: exercises resolve against it. */
    private record MuscleSeedLoad(Map<String, Long> nameToId, MuscleSeedResult result) {}

    /** Storage keys actually uploaded + how many referenced asset files were missing. */
    private record MediaResult(List<String> keys, int missing) {}

    private ExerciseSeedResult loadExercises(Map<String, Long> muscleNameToId) throws Exception {
        List<Map<String, Object>> exerciseData = readSeedJson(exerciseFile);

        int created = 0;
        int alreadyExisted = 0;
        int failed = 0;
        int assetsMissing = 0;
        // exercise.json contains 1324 rows but only 1318 unique names (6 duplicates).
        // Tracking seen names separates "duplicate inside the source file" from
        // "already seeded in a previous run" — otherwise the summary lies.
        Set<String> seen = new HashSet<>();

        for (Map<String, Object> exercise : exerciseData) {
            String name = (String) exercise.get("name");

            // Same name twice inside the source file: the first copy wins.
            if (!seen.add(name)) {
                continue;
            }

            // Idempotency: cheapest check first, before touching muscles or the DB.
            if (exerciseRepository.findByName(name).isPresent()) {
                alreadyExisted++;
                continue;
            }

            try {
                // Resolve mainMuscle name → ID
                String mainMuscleName = (String) exercise.getOrDefault("mainMuscle", "");
                Long mainMuscleId = resolveMuscleId(mainMuscleName, muscleNameToId);
                if (mainMuscleId == null) {
                    log.warn("  ⚠️ Skipping '{}': mainMuscle '{}' not found", name, mainMuscleName);
                    failed++;
                    continue;
                }

                // Resolve othersMuscle names → IDs
                List<Long> othersMuscleIds = resolveMuscleIds(
                        extractStringList(exercise.get("othersMuscle")), muscleNameToId);

                Map<String, String> description = extractDescriptionMap(exercise.get("description"));
                Map<String, List<String>> instruccion = extractInstructionMap(exercise.get("instruccion"));

                String category = (String) exercise.getOrDefault("category", "");
                String equipament = (String) exercise.getOrDefault("equipament", "");

                // Media (J2/D2): uploaded from the classpath under a deterministic key.
                // Missing/unreadable asset → that medium gets an empty list and the FILE
                // is counted in assetsMissing; the placeholder URL is never persisted.
                MediaResult images = uploadAssets(extractStringList(exercise.get("images")), "images");
                MediaResult videos = uploadAssets(extractStringList(exercise.get("videos")), "videos");
                assetsMissing += images.missing() + videos.missing();

                exerciseRepository.save(
                        Exercise.create(name, description, category, instruccion, equipament,
                                mainMuscleId, othersMuscleIds, images.keys(), videos.keys(), SYSTEM_USER_ID)
                );
                created++;

                if (created % 100 == 0) {
                    log.info("  ... {} exercises loaded", created);
                }
            } catch (StorageInfrastructureException e) {
                // J1 fail-fast: log the counters reached, then abort the whole run.
                // The exception keeps travelling out of run() → startup fails with ONE
                // diagnostic instead of one stacktrace per record.
                log.error("⛔ Infrastructure failure — aborting exercise seed. Counters so far: "
                                + "{} unique, {} created, {} already existed, {} failed, {} assets missing",
                        seen.size(), created, alreadyExisted, failed, assetsMissing);
                throw e;
            } catch (Exception e) {
                // Safe now: without an outer transaction this failure only rolls back
                // its own save, the next iteration starts a fresh transaction.
                failed++;
                log.error("  ❌ Error loading exercise '{}': {}", name, e.getMessage());
            }
        }

        log.info("  Exercises: {} unique, {} created, {} already existed, {} failed, {} assets missing",
                seen.size(), created, alreadyExisted, failed, assetsMissing);
        return new ExerciseSeedResult(seen.size(), created, alreadyExisted, failed, assetsMissing);
    }

    private List<Map<String, Object>> readSeedJson(String classpathFile) throws IOException {
        try (InputStream input = new ClassPathResource(classpathFile).getInputStream()) {
            return objectMapper.readValue(input, new TypeReference<List<Map<String, Object>>>() {});
        }
    }

    /**
     * Uploads every asset of one medium and reports how many were missing.
     *
     * @param assetUrls raw seed-JSON references (URLs or bare paths)
     * @param medium    {@code images} or {@code videos} — doubles as storage directory
     */
    private MediaResult uploadAssets(List<String> assetUrls, String medium) {
        List<String> keys = new ArrayList<>();
        int missing = 0;
        for (String assetUrl : assetUrls) {
            String key = uploadSeedAsset(assetUrl, medium);
            if (key == null) {
                missing++;
            } else {
                keys.add(key);
            }
        }
        return new MediaResult(keys, missing);
    }

    /**
     * Uploads ONE seed asset from the classpath through the storage abstraction.
     *
     * @return the deterministic storage key, or {@code null} when the asset file is
     *         missing or unreadable (D2 → caller counts it, the run continues)
     * @throws IllegalArgumentException data-level: unsafe basename (G7) or unsupported
     *                                  extension — the record fails, nothing is persisted
     * @throws StorageInfrastructureException infra: raised by the adapter on upload
     */
    private String uploadSeedAsset(String assetUrl, String medium) {
        String basename = basenameOf(assetUrl);
        requireConverterSafe(basename);

        ClassPathResource asset = new ClassPathResource("static/data/" + medium + "/" + basename);
        if (!asset.exists()) {
            return null;
        }

        byte[] bytes;
        try {
            bytes = asset.getContentAsByteArray();
        } catch (IOException e) {
            log.debug("  Unreadable seed asset {}/{}: {}", medium, basename, e.getMessage());
            return null;
        }

        // contentTypeFor throws for unknown extensions → data-level, counts as failed.
        MultipartFile file = new ClasspathMultipartFile(
                basename, ClasspathMultipartFile.contentTypeFor(basename), bytes);
        String key = medium + "/" + basename;
        return "videos".equals(medium)
                ? videoStorageService.uploadAs(file, key)
                : imageStorageService.uploadAs(file, key);
    }

    /** Basename of a seed asset reference: everything after the last '/'. */
    private static String basenameOf(String assetUrl) {
        if (assetUrl == null || assetUrl.isBlank()) {
            throw new IllegalArgumentException("Asset reference must not be empty");
        }
        String basename = assetUrl.substring(assetUrl.lastIndexOf('/') + 1);
        if (basename.isBlank()) {
            throw new IllegalArgumentException("Asset reference has no basename: " + assetUrl);
        }
        return basename;
    }

    /**
     * G7: seed keys must survive the {@code ListStringConverter} round-trip, which joins
     * with ',', splits on ',' and strips '[', ']' and all whitespace. An unsafe basename
     * would be persisted corrupted → reject it as data-level (J1 matrix row 10): the
     * record is counted as failed and never saved.
     */
    private static void requireConverterSafe(String basename) {
        for (int i = 0; i < basename.length(); i++) {
            char c = basename.charAt(i);
            if (c == ',' || c == '[' || c == ']' || Character.isWhitespace(c)) {
                throw new IllegalArgumentException(
                        "Unsafe asset basename (corrupts ListStringConverter): '" + basename + "'");
            }
        }
    }

    /**
     * Resolves a muscle name to its ID. Case-insensitive, fuzzy match.
     */
    private Long resolveMuscleId(String name, Map<String, Long> nameToId) {
        if (name == null || name.isBlank()) return null;
        String key = name.toLowerCase().trim();

        // Exact match
        if (nameToId.containsKey(key)) {
            return nameToId.get(key);
        }

        // Fuzzy match: check if key is contained in any map key or vice versa
        for (Map.Entry<String, Long> entry : nameToId.entrySet()) {
            if (entry.getKey().contains(key) || key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Resolves a list of muscle names to their IDs.
     */
    private List<Long> resolveMuscleIds(List<String> names, Map<String, Long> nameToId) {
        List<Long> ids = new ArrayList<>();
        for (String name : names) {
            Long id = resolveMuscleId(name, nameToId);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    /**
     * Extracts a List<String> from a JSON value.
     * Handles: null, String, List, List of Maps.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractStringList(Object value) {
        if (value == null) return List.of();
        if (value instanceof String s) {
            return s.isBlank() ? List.of() : List.of(s);
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(Object::toString)
                    .filter(s -> !s.isBlank())
                    .toList();
        }
        return List.of();
    }

    /**
     * Extracts a List<String> from a multilingual JSON value.
     * Input: {"en": "text", "es": "texto"} → ["text"]
     * Input: {"en": ["step1", "step2"], "es": ["paso1"]} → ["step1", "step2"]
     * Input: "plain string" → ["plain string"]
     * Input: ["step1", "step2"] → ["step1", "step2"]
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> extractDescriptionMap(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            Map<String, String> result = new HashMap<>();
            map.forEach((k, v) -> {
                if (k != null && v != null) {
                    result.put(k.toString(), v.toString());
                }
            });
            return result;
        }
        return Collections.emptyMap();
    }

    private Map<String, List<String>> extractInstructionMap(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            Map<String, List<String>> result = new HashMap<>();
            map.forEach((k, v) -> {
                if (k != null) {
                    result.put(k.toString(), extractStringList(v));
                }
            });
            return result;
        }
        return Collections.emptyMap();
    }
}
