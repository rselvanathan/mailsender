package com.mailsender.dto;

import java.util.Objects;

public abstract class EmailMessage {
    private String email;

    EmailMessage(String email) {
        this.email = email;
    }

    EmailMessage() {}

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {this.email = email;}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmailMessage message = (EmailMessage) o;
        return Objects.equals(email, message.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
