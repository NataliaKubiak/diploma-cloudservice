package org.example.diplomacloudservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
public class AuthDto {

    private String login;

    private String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
