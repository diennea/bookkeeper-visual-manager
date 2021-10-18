package org.bkvm.auth;

import java.io.Serializable;

public class User implements Serializable {
    String role;
    String password;

    public User(String role, String password)  {
        this.role = role;
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }
}
