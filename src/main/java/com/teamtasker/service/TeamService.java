package com.teamtasker.service;

import com.teamtasker.entity.Role;
import com.teamtasker.entity.Team;
import com.teamtasker.entity.User;
import com.teamtasker.exception.UserNotFoundException;
import com.teamtasker.repository.TeamRepository;
import com.teamtasker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;

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
        if (isTeamNameTaken(teamName)) {
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
            if (isTeamNameTaken(newName)) {
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
    // Query Methods

    public Page<Team> getAllActiveTeams(Pageable pageable) {
        return teamRepository.findByIsActiveTrue(pageable);
    }

    public Page<Team> getAllInactiveTeams(Pageable pageable) {
        return teamRepository.findByIsActiveFalse(pageable);
    }

    public Page<Team> searchTeamsByName(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        return teamRepository.findByNameContainingIgnoreCase(searchTerm.trim(), pageable);
    }

    public List<Team> getActiveTeamsByOwner(User owner) {
        return teamRepository.findByOwnerAndIsActiveTrue(owner);
    }

    public List<Team> getActiveTeamsByManager(User manager) {
        return teamRepository.findByManagersContainingAndIsActiveTrue(manager);
    }

    public List<Team> getActiveTeamsWithManagementRights(User user) {
        return teamRepository.findActiveTeamsWithManagementRights(user);
    }

    public List<Team> getActiveTeamsByMember(User member) {
        return teamRepository.findByMembersContainingAndIsActiveTrue(member);
    }

    public List<Team> getAllTeamsByOwner(User owner) {
        return teamRepository.findByOwner(owner);
    }

    public List<Team> getAllTeamsByManager(User manager) {
        return teamRepository.findByManagersContaining(manager);
    }

    public List<Team> getAllTeamsWithManagementRights(User user) {
        return teamRepository.findTeamsWithManagementRights(user);
    }

    public List<Team> getAllTeamsByMember(User member) {
        return teamRepository.findByMembersContaining(member);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Access Helper Methods

    public boolean hasViewAccess(Team team, User user) {
        return user.getRole() == Role.ADMIN || team.isMember(user);
    }

    public boolean hasManagementAccess(Team team, User user) {
        return team.hasManagementRights(user);
    }

    private void validateManagementAccess(Team team, User user) {
        if (!hasManagementAccess(team, user)) {
            throw new IllegalArgumentException("You don't have management rights for this team"); //TeamAccessException
        }
    }

    // duplicates using query instead of entity - may be useful later
    public boolean userHasManagementRights(Integer teamId, User user) {
        return teamRepository.userHasManagementRights(teamId, user);
    }

    public boolean isUserAssociatedWithTeam(Integer teamId, User user) {
        return teamRepository.isUserAssociatedWithTeam(teamId, user);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Statistics

    public long getTeamCountByOwner(User owner) {
        return teamRepository.countTeamsByOwner(owner);
    }

    public long getTeamCountByManager(User manager) {
        return teamRepository.countTeamsByManager(manager);
    }

    public long getTeamCountByMember(User member) {
        return teamRepository.countTeamsByMember(member);
    }

    public boolean isTeamNameTaken(String teamName) {
        return teamRepository.findByName(teamName).isPresent();
    }
}
