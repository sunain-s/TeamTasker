package com.teamtasker.exception;

public class TeamAlreadyExistsException extends RuntimeException {

    private final String teamName;

    public TeamAlreadyExistsException(String teamName) {
        super("Team name already exists: " + teamName);
        this.teamName = teamName;
    }

    public String getTeamName() {
        return teamName;
    }
}
