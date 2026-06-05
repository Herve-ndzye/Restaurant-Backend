package com.mavic.backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin invitation details with temporary credentials")
public class AdminInvitationResponse {
    
    @Schema(description = "Created user ID", example = "5")
    private Long userId;
    
    @Schema(description = "Username for login", example = "new_admin")
    private String username;
    
    @Schema(description = "Email address", example = "newadmin@restaurant.com")
    private String email;
    
    @Schema(description = "Temporary password (must be changed on first login)", example = "TempPass123!")
    private String temporaryPassword;
    
    @Schema(description = "Login URL", example = "http://localhost:3000/login")
    private String loginUrl;
    
    @Schema(description = "Invitation message")
    private String message;
    
    @Schema(description = "Whether this is first login requiring password change", example = "true")
    private boolean requirePasswordChange;
}
