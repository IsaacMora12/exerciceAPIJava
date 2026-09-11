package infrastructure.adapter.in.web.teams;

import domain.model.team.Membership;
import domain.port.teams.in.AddMemberUseCase;
import infrastructure.adapter.in.web.teams.dto.AddMemberRequest;
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
@RequestMapping("/api/teams/{teamId}/members")
@Tag(name = "Team Members", description = "Endpoints for team membership management")
public class AddMemberController {

    private static final Logger log = LoggerFactory.getLogger(AddMemberController.class);

    private final AddMemberUseCase addMemberUseCase;

    public AddMemberController(AddMemberUseCase addMemberUseCase) {
        this.addMemberUseCase = addMemberUseCase;
    }

    @Operation(summary = "Add Member", description = "Add a user to a team with a role ('admin' or 'member')")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> addMember(@PathVariable Long teamId, @RequestBody AddMemberRequest request) {
        try {
            Membership membership = addMemberUseCase.addMember(request.userId(), teamId, request.role());

            MembershipResponse response = new MembershipResponse(
                    membership.getId(), membership.getUserId(), membership.getTeamId(),
                    membership.getRole(), membership.getIsActive(),
                    membership.getJoinedAt(), membership.getUpdatedAt()
            );

            log.info("Member added: userId={}, teamId={}", response.userId(), response.teamId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add member: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 400), HttpStatus.BAD_REQUEST);
        }
    }
}
