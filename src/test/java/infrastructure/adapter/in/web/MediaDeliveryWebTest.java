package infrastructure.adapter.in.web;

import application.services.auth.JwtService;
import com.example.exerciceapi.ExerciceApiApplication;
import domain.model.exercise.Exercise;
import domain.model.exercise.Muscle;
import domain.model.user.User;
import domain.port.exercise.ExerciseRepository;
import domain.port.exercise.MuscleRepository;
import domain.port.storage.FileStorageService;
import domain.port.users.UserRepository;
import infrastructure.adapter.in.web.exercise.ViewAllMuscleController;
import infrastructure.adapter.in.web.exercise.dto.MuscleResponse;
import infrastructure.adapter.out.persistence.auth.CustomUserDetails;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * D1(b) read path end-to-end (spec media-delivery/ + SR2 + SR4 response builders).
 *
 * <p>Profile {@code dev} with an isolated {@code storage.local.root} makes the REAL
 * {@code LocalMediaConfig} resource handler serve files over {@code /media/**} — no
 * mocking of the read path itself. The storage bean is overridden ONLY to shape
 * {@code getUrl()} into the {@code /media/<key>} form the handler serves, so
 * "response URL → unauthenticated GET → 200" is a genuine end-to-end assertion.
 *
 * <p>Security chain is applied explicitly: {@code webAppContextSetup} alone does NOT
 * register Spring Security's filter, so SR2 (permitAll /media, authenticated /api)
 * would otherwise go untested.
 *
 * <p>Note on defects (report-only, main code untouched per G3):
 * <ul>
 *   <li>Unknown media key currently yields 500 (GlobalExceptionHandler catch-all
 *       swallows NoResourceFoundException); spec wants 404 → DEFECT-1. Test asserts
 *       denial/no-content only, never codifies 500.</li>
 *   <li>{@code GET /api/muscle} (view-all) is unroutable (no @GetMapping on
 *       {@link ViewAllMuscleController}) → DEFECT-3. The builder is exercised via a
 *       direct bean call instead of HTTP.</li>
 * </ul>
 */
@SpringBootTest(classes = ExerciceApiApplication.class)
@ActiveProfiles("dev")
@Transactional
class MediaDeliveryWebTest {

    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x01, 0x02};
    private static final byte[] GIF_BYTES = "GIF89a-phase5-media".getBytes(StandardCharsets.US_ASCII);
    private static final String TRAP_SENTINEL = "TRAP-SENTINEL-MUST-NEVER-BE-SERVED";

    private static Path storageRoot;
    private static Path trapFile;

    @DynamicPropertySource
    static void mediaRoot(DynamicPropertyRegistry registry) throws IOException {
        Path outer = Files.createTempDirectory("phase5-media-outer");
        storageRoot = outer.resolve("storage");
        Files.createDirectories(storageRoot.resolve("images"));
        Files.createDirectories(storageRoot.resolve("videos"));
        Files.writeString(outer.resolve("trap-secret.txt"), TRAP_SENTINEL);
        trapFile = outer.resolve("trap-secret.txt");
        registry.add("storage.local.root", () -> storageRoot.toString());
    }

    /**
     * Replaces the test double named {@code fileStorageService} so MediaUrlResolver
     * emits the {@code /media/<key>} shape the dev resource handler actually serves
     * (the stock test double returns bare keys — useless for end-to-end fetch checks).
     */
    @MockitoBean(name = "fileStorageService")
    private FileStorageService storage;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private MuscleRepository muscleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ViewAllMuscleController viewAllMuscleController;

    private MockMvc mvc;
    private String bearerToken;

    @BeforeEach
    void setUp() throws Exception {
        when(storage.getUrl(anyString())).thenAnswer(inv -> "/media/" + inv.getArgument(0));
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(wac.getBean("springSecurityFilterChain", Filter.class))
                .build();
        User user = userRepository.save(User.create("Phase5 Web User", "phase5web@example.com", "password123"));
        bearerToken = "Bearer " + jwtService.generateAccessToken(new CustomUserDetails(user));
    }

    private void putAsset(String key, byte[] content) throws IOException {
        Path target = storageRoot.resolve(key);
        Files.createDirectories(target.getParent());
        Files.write(target, content);
    }

    private Exercise saveExercise(String name, List<String> images, List<String> videos) {
        return exerciseRepository.save(Exercise.create(name,
                Map.of("en", "Phase5 builder parity"),
                "Strength",
                Map.of("en", List.of("Step one")),
                "Bar", 1L, List.of(), images, videos, 1L));
    }

    // ------------------------------------------------------------------
    // Resource handler: stored file served with correct content type (SR4)
    // ------------------------------------------------------------------

    @Test
    void storedFileIsServedOverMediaPathWithMatchingContentType() throws Exception {
        putAsset("images/probe.jpg", JPEG_BYTES);

        MvcResult result = mvc.perform(get("/media/images/probe.jpg"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentType()).contains("image/jpeg");
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(JPEG_BYTES);
    }

    // ------------------------------------------------------------------
    // SR2: media unauthenticated; API routes stay protected
    // ------------------------------------------------------------------

    @Test
    void mediaUrlReturnedByApiIsFetchableWithoutAnyAuthorizationHeader() throws Exception {
        putAsset("images/e2e.jpg", JPEG_BYTES);
        putAsset("videos/e2e.gif", GIF_BYTES);
        Exercise saved = saveExercise("Phase5 E2E Fetch",
                List.of("images/e2e.jpg"), List.of("videos/e2e.gif"));

        MvcResult view = mvc.perform(get("/api/exercise/" + saved.getId())
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]").value("/media/images/e2e.jpg"))
                .andExpect(jsonPath("$.videos[0]").value("/media/videos/e2e.gif"))
                .andReturn();

        String body = view.getResponse().getContentAsString();
        assertThat(body).doesNotContain("example.com")
                .doesNotContain("src/main")
                .doesNotContain("\"images/e2e.jpg\"");

        // The client holding ONLY the returned URL fetches the bytes with NO token
        MvcResult media = mvc.perform(get("/media/images/e2e.jpg"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(media.getResponse().getContentAsByteArray()).isEqualTo(JPEG_BYTES);

        // Response-time resolution: the database still holds the short storage key
        Exercise reread = exerciseRepository.findById(saved.getId()).orElseThrow();
        assertThat(reread.getImages()).containsExactly("images/e2e.jpg");
        assertThat(reread.getVideos()).containsExactly("videos/e2e.gif");
    }

    @Test
    void apiRoutesRemainProtectedWithoutToken() throws Exception {
        mvc.perform(get("/api/exercise/1")).andExpect(status().is4xxClientError())
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
        mvc.perform(get("/api/muscle/1")).andExpect(status().is4xxClientError())
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .isIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
    }

    // ------------------------------------------------------------------
    // Security of the read path: traversal denied, unknown key serves nothing
    // ------------------------------------------------------------------

    @Test
    void pathTraversalCannotEscapeTheStorageRootNorServeTheTrapFile() throws Exception {
        assertThat(trapFile)
                .as("precondition: trap sentinel sits exactly one level above the storage root")
                .exists();

        for (String attempt : new String[]{
                "/media/../trap-secret.txt",
                "/media/%2e%2e/trap-secret.txt",
                "/media/images/../../trap-secret.txt"}) {
            MvcResult result = mvc.perform(get(attempt)).andReturn();
            int status = result.getResponse().getStatus();
            assertThat(status)
                    .as("traversal [%s] must be denied with a client error", attempt)
                    .isGreaterThanOrEqualTo(400)
                    .isLessThan(500);
            assertThat(result.getResponse().getContentAsString())
                    .as("traversal [%s] must not leak the trap file", attempt)
                    .doesNotContain(TRAP_SENTINEL);
        }
    }

    @Test
    void unknownMediaKeyServesNoContent() throws Exception {
        // Spec requires 404 here; current implementation returns 500 because
        // GlobalExceptionHandler's catch-all @ExceptionHandler(Exception.class)
        // swallows NoResourceFoundException — DEFECT-1 (reported, NOT pinned).
        MvcResult missing = mvc.perform(get("/media/images/definitely-absent.jpg")).andReturn();

        assertThat(missing.getResponse().getStatus()).isGreaterThanOrEqualTo(400);
        assertThat(missing.getResponse().getContentType())
                .as("an unknown key must never be served as a media type")
                .doesNotContain("image/");
        byte[] body = missing.getResponse().getContentAsByteArray();
        assertThat(body.length < 2 || body[0] != (byte) 0xFF || body[1] != (byte) 0xD8)
                .as("an unknown key must never stream media bytes")
                .isTrue();
    }

    // ------------------------------------------------------------------
    // Response builders: exercise view, create, update, muscle view-all
    // ------------------------------------------------------------------

    @Test
    void allFourResponseBuildersResolveKeysIntoFetchableUrls() throws Exception {
        putAsset("images/create-key.jpg", JPEG_BYTES);
        putAsset("images/update-key.jpg", JPEG_BYTES);
        putAsset("images/builder-muscle.jpg", JPEG_BYTES);

        // 1) Create builder (multipart, JSON-provided storage keys).
        //    description/instruccion are OMITTED: those Map fields are annotated with
        //    StringListDeserializer (a List<String> deserializer) → any present value
        //    throws ClassCastException → 500 — DEFECT-4 (pre-existing, reported, NOT pinned).
        MockMultipartFile data = new MockMultipartFile("data", null, "application/json",
                """
                {
                  "name": "Phase5 Create Builder",
                  "category": "Strength",
                  "equipament": "Bar",
                  "mainMuscle": 1,
                  "othersMuscle": [],
                  "images": ["images/create-key.jpg"],
                  "videos": []
                }
                """.getBytes(StandardCharsets.UTF_8));

        String createBody = mvc.perform(multipart("/api/exercise/create").file(data)
                        .header("Authorization", bearerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.images[0]").value("/media/images/create-key.jpg"))
                .andReturn().getResponse().getContentAsString();
        assertThat(createBody).doesNotContain("\"images/create-key.jpg\"");

        long createId = Long.parseLong(createBody.replaceAll(".*\"id\":(\\d+).*", "$1"));

        // 2) View builder over the SAME record must produce the IDENTICAL URL shape
        mvc.perform(get("/api/exercise/" + createId).header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]").value("/media/images/create-key.jpg"));

        // The URL emitted by the builders is fetchable as-is
        mvc.perform(get("/media/images/create-key.jpg")).andExpect(status().isOk());

        // 3) Update builder (description/instruccion omitted — DEFECT-4, same as create)
        Exercise target = saveExercise("Phase5 Update Builder",
                List.of("images/old-key.jpg"), List.of());
        String updateBody = mvc.perform(put("/api/exercise/" + target.getId())
                        .contentType("application/json")
                        .header("Authorization", bearerToken)
                        .content("""
                                {
                                  "name": "Phase5 Update Builder",
                                  "category": "Strength",
                                  "equipament": "Bar",
                                  "mainMuscle": 1,
                                  "othersMuscle": [],
                                  "images": ["images/update-key.jpg"],
                                  "videos": [],
                                  "isActive": true
                                }
                                """.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]").value("/media/images/update-key.jpg"))
                .andReturn().getResponse().getContentAsString();
        assertThat(updateBody).doesNotContain("\"images/update-key.jpg\"");

        mvc.perform(get("/media/images/update-key.jpg")).andExpect(status().isOk());

        // 4) Muscle view-all builder — exercised as a bean call because the HTTP route
        //    is missing (@GetMapping absent on ViewAllMuscleController) — DEFECT-3.
        Muscle muscle = muscleRepository.save(
                Muscle.create("Phase5BuilderMuscle", "desc", List.of("images/builder-muscle.jpg"), 1L));
        ResponseEntity<List<MuscleResponse>> entity = viewAllMuscleController.viewMuscle();
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<MuscleResponse> all = entity.getBody();
        assertThat(all).isNotNull();
        MuscleResponse ours = all.stream()
                .filter(m -> "Phase5BuilderMuscle".equals(m.name()))
                .findFirst().orElseThrow();
        assertThat(ours.images()).containsExactly("/media/images/builder-muscle.jpg");

        for (MuscleResponse m : all) {
            for (String image : m.images()) {
                assertThat(image)
                        .as("view-all must never pass storage keys through verbatim")
                        .startsWith("/media/")
                        .doesNotContain("example.com")
                        .doesNotContain("src/main");
            }
        }

        // Same record surfaced through the HTTP muscle view-by-id builder
        MvcResult muscleView = mvc.perform(get("/api/muscle/" + muscle.getId())
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]").value("/media/images/builder-muscle.jpg"))
                .andReturn();
        assertThat(muscleView.getResponse().getContentAsString()).doesNotContain("example.com");
    }

    // ------------------------------------------------------------------
    // Pre-existing UUID-keyed records resolve through the same path
    // ------------------------------------------------------------------

    @Test
    void preExistingUuidKeyedRecordResolvesToFetchableUrl() throws Exception {
        String uuidKey = "images/" + UUID.randomUUID() + "-legacy.jpg";
        putAsset(uuidKey, JPEG_BYTES);
        Exercise legacy = saveExercise("Phase5 Legacy UUID Key", List.of(uuidKey), List.of());

        mvc.perform(get("/api/exercise/" + legacy.getId())
                        .header("Authorization", bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images[0]").value("/media/" + uuidKey));

        mvc.perform(get("/media/" + uuidKey)).andExpect(status().isOk());
    }

    // ------------------------------------------------------------------
    // Storage bean is the resolution authority (SR4 / test-double shape)
    // ------------------------------------------------------------------

    @Test
    void resolutionGoesThroughTheActiveStorageBean() {
        when(storage.getUrl("images/authority.jpg")).thenReturn("https://cdn.test/images/authority.jpg");
        MediaUrlResolver resolver = new MediaUrlResolver(storage);

        assertThat(resolver.toUrl("images/authority.jpg")).isEqualTo("https://cdn.test/images/authority.jpg");
        assertThat(resolver.toUrl("https://example.com/legacy.jpg")).isEqualTo("https://example.com/legacy.jpg");
        assertThat(resolver.toUrl("")).isEmpty();
        assertThat(resolver.toUrls(null)).isEmpty();
    }

}
