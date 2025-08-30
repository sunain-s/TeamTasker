package com.teamtasker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 30, message = "Username must be between 4 and 30 characters")
    @Column(unique = true, nullable = false, length = 30)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    // Teams where this user is the owner
    @JsonIgnore
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private Set<Team> ownedTeams = new HashSet<>();

    // Teams where this user is a manager
    @JsonIgnore
    @ManyToMany(mappedBy = "managers", fetch = FetchType.LAZY)
    private Set<Team> managedTeams = new HashSet<>();

    // Teams where this user is a member (including managed teams)
    @JsonIgnore
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<Team> memberTeams = new HashSet<>();

    public User() {}

    public User(String firstName, String lastName, String email, String username, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public User(String firstName, String lastName, String email, String username, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Query Methods

    public boolean isOwnerOf(Team team) {
        return ownedTeams.contains(team);
    }

    public boolean isManagerOf(Team team) {
        return managedTeams.contains(team);
    }

    public boolean isMemberOf(Team team) {
        return memberTeams.contains(team);
    }

    public boolean hasManagementRightsFor(Team team) {
        return isOwnerOf(team) || isManagerOf(team) || this.role == Role.ADMIN;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

//    public Set<Team> getTeams() {
//        return teams;
//    }
//
//    public void setTeams(Set<Team> teams) {
//        this.teams = teams;
//    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", role=" + role +
                '}';
    }
}