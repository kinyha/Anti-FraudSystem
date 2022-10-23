package antifraud.dto;

import antifraud.model.User;

public class UserResponse {
    private Long id;
    private String name;
    private String username;
    private String role;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String username, String role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
    }

    public UserResponse(User user) {
        this.id = user.getUserId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.role = user.getRole();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
