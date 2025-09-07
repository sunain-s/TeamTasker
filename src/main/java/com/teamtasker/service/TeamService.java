package com.teamtasker.service;

import com.teamtasker.entity.Role;
import com.teamtasker.entity.Team;
import com.teamtasker.entity.User;
import com.teamtasker.exception.UserNotFoundException;
import com.teamtasker.repository.TeamRepository;
import com.teamtasker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Team Management

    public Team createTeam(String teamName, String description, User owner) {
        if (teamRepository.findByName(teamName).isPresent()) {
            throw new IllegalArgumentException("Team name already exists: " + teamName); //TeamAlreadyExistsException
        }
        Team team = new Team(teamName, description, owner);
        return teamRepository.save(team);
    }

    public Team getTeamById(Integer teamId) {
        return teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("Team not found. TeamID: " + teamId)); //TeamNotFoundException
    }

    public Team getTeamByName(String teamName) {
        return teamRepository.findByName(teamName).orElseThrow(() -> new IllegalArgumentException("Team not found. Name: " + teamName)); //TeamNotFoundException
    }

    public Team updateTeam(Integer teamId, String newName, String newDescription, User currUser) {
        Team team = getTeamById(teamId);
        validateManagementAccess(team, currUser);
        if (newName != null && !newName.equals(team.getName())) {
            if (teamRepository.findByName(newName).isPresent()) {
                throw new IllegalArgumentException("Team name already exists: " + newName); //TeamAlreadyExistsException
            }
            team.setName(newName);
        }

        if (newDescription != null) {
            team.setDescription(newDescription);
        }
        return teamRepository.save(team);
    }

    public void deleteTeam(Integer teamId, User currUser) {
        Team team =  getTeamById(teamId);
        if (!team.isOwner(currUser) && currUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only team owners or admins can delete teams"); //TeamAccessException
        }
        teamRepository.delete(team);
    }

    public void deactivateTeam(Integer teamId, User currUser) {
        Team team = getTeamById(teamId);
        validateManagementAccess(team, currUser);
        team.setIsActive(false);
        teamRepository.save(team);
    }

    public void reactivateTeam(Integer teamId, User currUser) {
        Team team = getTeamById(teamId);
        validateManagementAccess(team, currUser);
        team.setIsActive(true);
        teamRepository.save(team);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Member Management

    public void addMemberToTeam(Integer teamId, String username, User currUser) {
        Team team = getTeamById(teamId);
        validateManagementAccess(team, currUser);
        User newMember = userRepository.findByUsername(username).orElseThrow(()
                -> new UserNotFoundException("User not found. Username: " + username));

        if (team.isMember(newMember)) {
            throw new IllegalArgumentException("User is already a member of this team");
        }
        team.getMembers().add(newMember);
        teamRepository.save(team);
    }

    public void removeMemberFromTeam(Integer teamId, String username, User currUser) {
        Team team = getTeamById(teamId);
        validateManagementAccess(team, currUser);
        User memberToRemove = userRepository.findByUsername(username).orElseThrow(()
                -> new UserNotFoundException("User not found. Username: " + username));

        if (!team.isMember(memberToRemove)) {
            throw new IllegalArgumentException("User is not a member of this team"); // UserNotInTeamException
        }
        if (team.isOwner(memberToRemove)) {
            throw new IllegalArgumentException("Cannot remove owner from team");
        }
        team.getMembers().remove(memberToRemove);
        team.getManagers().remove(memberToRemove);
        teamRepository.save(team);
    }

    public void promoteToManager(Integer teamId, String username, User currUser) {
        Team team = getTeamById(teamId);
        if (!team.isOwner(currUser) &&  currUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only team owners or admins can promote members to managers"); // TeamAccessException
        }
        User userToPromote = userRepository.findByUsername(username).orElseThrow(()
                -> new UserNotFoundException("User not found. Username: " + username));

        if (!team.isMember(userToPromote)) {
            throw new IllegalArgumentException("User is not a member of this team"); // UserNotInTeamException
        }
        if (team.isManager(userToPromote)) {
            throw new IllegalArgumentException("User is already a manager of this team");
        }
        team.getManagers().add(userToPromote);
        teamRepository.save(team);
    }

    public void demoteFromManager(Integer teamId, String username, User currUser) {
        Team team = getTeamById(teamId);
        if (!team.isOwner(currUser) &&  currUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only team owners or admins can demote members from managers"); // TeamAccessException
        }
        User userToDemote = userRepository.findByUsername(username).orElseThrow(()
                -> new UserNotFoundException("User not found. Username: " + username));

        if (!team.isManager(userToDemote)) {
            throw new IllegalArgumentException("User is not a manager of this team");
        }
        if (team.isOwner(userToDemote)) {
            throw new IllegalArgumentException("Cannot demote team owner from manager role");
        }
        team.getManagers().remove(userToDemote);
        teamRepository.save(team);
    }

    // transferring ownership demotes previous owner to manager
    public void transferOwnership(Integer teamId, String newOwnerUsername, User currUser) {
        Team team = getTeamById(teamId);
        if (!team.isOwner(currUser) &&  currUser.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Only team owners or admins can transfer team ownership"); // TeamAccessException
        }
        User newOwner = userRepository.findByUsername(newOwnerUsername).orElseThrow(()
                -> new UserNotFoundException("User not found. Username: " + newOwnerUsername));

        if (!team.isMember(newOwner)) {
            throw new IllegalArgumentException("User is not a member of this team"); // UserNotInTeamException
        }
        if (team.isOwner(newOwner)) {
            throw new IllegalArgumentException("User is already the owner of this team");
        }
        team.setOwner(newOwner);
        teamRepository.save(team);
    }

    //------------------------------------------------------------------------------------------------------------------
    //

    public boolean hasManagementAccess(Team team, User user) {
        return team.hasManagementRights(user);
    }

    private void validateManagementAccess(Team team, User user) {
        if (!hasManagementAccess(team, user)) {
            throw new IllegalArgumentException("You don't have management rights for this team"); //TeamAccessException
        }

    }
}
