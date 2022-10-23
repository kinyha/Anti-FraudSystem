package antifraud.dto;

import javax.validation.constraints.NotBlank;

public class EditUserRoleRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String role;

    public EditUserRoleRequest() {
    }

    public EditUserRoleRequest(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "EditUserRoleResponse{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
