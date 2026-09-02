package infrastructure.adapter.in.web.teams;

import domain.model.team.Invitation;
import domain.port.teams.in.SendInvitationUseCase;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.in.web.teams.dto.InvitationResponse;
import infrastructure.adapter.in.web.teams.dto.SendInvitationRequest;
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
@RequestMapping("/api/teams/{teamId}/invitations")
@Tag(name = "Team Invitations", description = "Endpoints for team invitation management")
public class SendInvitationController {

    private static final Logger log = LoggerFactory.getLogger(SendInvitationController.class);

    private final SendInvitationUseCase sendInvitationUseCase;

    public SendInvitationController(SendInvitationUseCase sendInvitationUseCase) {
        this.sendInvitationUseCase = sendInvitationUseCase;
    }

    @Operation(summary = "Send Invitation", description = "Send an invitation to join a team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Invitation sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> sendInvitation(@PathVariable Long teamId, @RequestBody SendInvitationRequest request) {
        try {
            Invitation invitation = sendInvitationUseCase.sendInvitation(
                    teamId, request.email(), request.roleId(), request.expiresAt());

            InvitationResponse response = new InvitationResponse(
                    invitation.getId(), invitation.getTeamId(), invitation.getEmail(),
                    invitation.getRoleId(), invitation.getToken(),
                    invitation.getStatus().name(), invitation.getExpiresAt(),
                    invitation.getCreatedAt(), invitation.getUpdatedAt()
            );

            log.info("Invitation sent: id={}, email={}", response.id(), response.email());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to send invitation: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 400), HttpStatus.BAD_REQUEST);
        }
    }
}
