package infrastructure.adapter.in.web.teams;

import domain.model.team.Membership;
import domain.port.teams.in.AcceptInvitationUseCase;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.in.web.teams.dto.MembershipResponse;
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
@RequestMapping("/api/invitations")
@Tag(name = "Team Invitations", description = "Endpoints for team invitation management")
public class AcceptInvitationController {

    private static final Logger log = LoggerFactory.getLogger(AcceptInvitationController.class);

    private final AcceptInvitationUseCase acceptInvitationUseCase;

    public AcceptInvitationController(AcceptInvitationUseCase acceptInvitationUseCase) {
        this.acceptInvitationUseCase = acceptInvitationUseCase;
    }

    @Operation(summary = "Accept Invitation", description = "Accept a team invitation using the token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(@RequestParam String token, @RequestParam Long userId) {
        try {
            Membership membership = acceptInvitationUseCase.acceptInvitation(token, userId);

            MembershipResponse response = new MembershipResponse(
                    membership.getId(), membership.getUserId(), membership.getTeamId(),
                    membership.getRoleId(), membership.getIsActive(),
                    membership.getJoinedAt(), membership.getUpdatedAt()
            );

            log.info("Invitation accepted: membershipId={}, userId={}", response.id(), response.userId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Failed to accept invitation: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 400), HttpStatus.BAD_REQUEST);
        }
    }
}
