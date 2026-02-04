package com.lollito.fm.model.rest;

import java.io.Serializable;
import javax.validation.constraints.NotBlank;

public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    private String usernameOrEmail;

    @NotBlank
    private String password;

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
