package com.mailsender.dto;

import java.util.Objects;

public class RomCharmEmail extends EmailMessage {
    private String firstName;
    private String lastName;
    private boolean areAttending;
    private int numberAttending;

    public RomCharmEmail(String email, String firstName, String lastName, boolean areAttending, int numberAttending) {
        super(email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.areAttending = areAttending;
        this.numberAttending = numberAttending;
    }

    public RomCharmEmail() {
        super();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean isAreAttending() {
        return areAttending;
    }

    public int getNumberAttending() {
        return numberAttending;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAreAttending(boolean areAttending) {
        this.areAttending = areAttending;
    }

    public void setNumberAttending(int numberAttending) {
        this.numberAttending = numberAttending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        RomCharmEmail email = (RomCharmEmail) o;
        return areAttending == email.areAttending && numberAttending == email.numberAttending && Objects
            .equals(firstName, email.firstName) && Objects.equals(lastName, email.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), firstName, lastName, areAttending, numberAttending);
    }
}
