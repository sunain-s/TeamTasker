package com.teamtasker.service;

import com.teamtasker.entity.Role;
import com.teamtasker.entity.Team;
import com.teamtasker.entity.User;
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
        team.getManagers().add(owner);
        team.getMembers().add(owner);
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
