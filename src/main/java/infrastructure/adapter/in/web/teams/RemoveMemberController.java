package infrastructure.adapter.in.web.teams;

import domain.port.teams.in.RemoveMemberUseCase;
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
@RequestMapping("/api/teams/members")
@Tag(name = "Team Members", description = "Endpoints for team membership management")
public class RemoveMemberController {

    private static final Logger log = LoggerFactory.getLogger(RemoveMemberController.class);

    private final RemoveMemberUseCase removeMemberUseCase;

    public RemoveMemberController(RemoveMemberUseCase removeMemberUseCase) {
        this.removeMemberUseCase = removeMemberUseCase;
    }

    @Operation(summary = "Remove Member", description = "Remove a member from a team")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member removed successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeMember(@PathVariable Long id) {
        try {
            removeMemberUseCase.removeMember(id);
            log.info("Member removed: membershipId={}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to remove member: {}", e.getMessage());
            return new ResponseEntity<>(new ErrorResponse(e.getMessage(), 404), HttpStatus.NOT_FOUND);
        }
    }
}
