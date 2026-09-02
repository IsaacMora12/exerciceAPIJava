package infrastructure.adapter.in.web.teams;

import domain.port.teams.in.CancelInvitationUseCase;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/invitations")
@Tag(name = "Team Invitations", description = "Endpoints for team invitation management")
public class CancelInvitationController {

    private static final Logger log = LoggerFactory.getLogger(CancelInvitationController.class);

    private final CancelInvitationUseCase cancelInvitationUseCase;

    public CancelInvitationController(CancelInvitationUseCase cancelInvitationUseCase) {
        this.cancelInvitationUseCase = cancelInvitationUseCase;
    }

    @Operation(summary = "Cancel Invitation", description = "Cancel a pending invitation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Invitation cancelled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelInvitation(@PathVariable Long id) {
        try {
            cancelInvitationUseCase.cancelInvitation(id);
            log.info("Invitation cancelled: id={}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Failed to cancel invitation: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 400), HttpStatus.BAD_REQUEST);
        }
    }
}
