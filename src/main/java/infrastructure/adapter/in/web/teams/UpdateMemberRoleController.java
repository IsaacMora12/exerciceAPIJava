package infrastructure.adapter.in.web.teams;

import domain.model.team.Membership;
import domain.port.teams.in.UpdateMemberRoleUseCase;
import infrastructure.adapter.in.web.teams.dto.ErrorResponse;
import infrastructure.adapter.in.web.teams.dto.MembershipResponse;
import infrastructure.adapter.in.web.teams.dto.UpdateMemberRoleRequest;
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
@RequestMapping("/api/teams/members")
@Tag(name = "Team Members", description = "Endpoints for team membership management")
public class UpdateMemberRoleController {

    private static final Logger log = LoggerFactory.getLogger(UpdateMemberRoleController.class);

    private final UpdateMemberRoleUseCase updateMemberRoleUseCase;

    public UpdateMemberRoleController(UpdateMemberRoleUseCase updateMemberRoleUseCase) {
        this.updateMemberRoleUseCase = updateMemberRoleUseCase;
    }

    @Operation(summary = "Update Member Role", description = "Update the role of a team member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateMemberRole(@PathVariable Long id, @RequestBody UpdateMemberRoleRequest request) {
        try {
            Membership membership = updateMemberRoleUseCase.updateMemberRole(id, request.roleId());

            MembershipResponse response = new MembershipResponse(
                    membership.getId(), membership.getUserId(), membership.getTeamId(),
                    membership.getRoleId(), membership.getIsActive(),
                    membership.getJoinedAt(), membership.getUpdatedAt()
            );

            log.info("Member role updated: membershipId={}, newRoleId={}", response.id(), response.roleId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update member role: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 400), HttpStatus.BAD_REQUEST);
        }
    }
}
