package com.teamtasker.exception;

public class UserNotInTeamException extends RuntimeException {

    private final String username;
    private final String teamName;
    private final Integer teamId;

    public UserNotInTeamException(String username, String teamName, Integer teamId) {
        super("User is not a member of this team. User: " + username + "; Team: " + teamName + "; Team ID: " + teamId);
        this.username = username;
        this.teamName = teamName;
        this.teamId = teamId;
    }

    public String getUsername() {
        return username;
    }

    public String getTeamName() {
        return teamName;
    }

    public Integer getTeamId() {
        return teamId;
    }
}
