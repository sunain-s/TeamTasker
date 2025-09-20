package com.teamtasker.controller;

import com.teamtasker.auth.CustomUserDetails;
import com.teamtasker.entity.Team;
import com.teamtasker.entity.User;
import com.teamtasker.service.TeamService;
import com.teamtasker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

    @Autowired
    public TeamController(TeamService teamService, UserService userService) {
        this.teamService = teamService;
        this.userService = userService;
    }

    @GetMapping
    public String listTeams(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "active") String filter,
                            Model model,
                            Authentication authentication) {
        User currUser = getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size);

        Page<Team> teams = switch (filter.toLowerCase()) {
            case "inactive" -> teamService.getAllInactiveTeams(pageable);
            case "all" -> teamService.getAllTeams(pageable);
            default -> teamService.getAllActiveTeams(pageable);
        };

        model.addAttribute("teams", teams);
        model.addAttribute("currentUser", currUser);
        model.addAttribute("currentFilter", filter);

        return "teams/list";
    }

    @GetMapping("/my-teams")
    public String listMyTeams(@RequestParam(defaultValue = "active") String filter,
                              Model model,
                              Authentication authentication) {
        User currUser = getCurrentUser(authentication);
        List<Team> ownedTeams;
        List<Team> managerTeams;
        List<Team> managementTeams;
        List<Team> memberTeams;

        switch (filter.toLowerCase()) {
            case "inactive":
                ownedTeams = teamService.getInactiveTeamsByOwner(currUser);
                managerTeams = teamService.getInactiveTeamsByManager(currUser);
                managementTeams = teamService.getInactiveTeamsWithManagementRights(currUser);
                memberTeams = teamService.getInactiveTeamsByMember(currUser);
                break;
            case "all":
                ownedTeams = teamService.getAllTeamsByOwner(currUser);
                managerTeams = teamService.getAllTeamsByManager(currUser);
                managementTeams = teamService.getAllTeamsWithManagementRights(currUser);
                memberTeams = teamService.getAllTeamsByMember(currUser);
                break;
            case "active":
            default:
                ownedTeams = teamService.getActiveTeamsByOwner(currUser);
                managerTeams = teamService.getActiveTeamsByManager(currUser);
                managementTeams = teamService.getActiveTeamsWithManagementRights(currUser);
                memberTeams = teamService.getActiveTeamsByMember(currUser);
                break;
        }

        model.addAttribute("ownedTeams", ownedTeams);
        model.addAttribute("managerTeams", managerTeams);
        model.addAttribute("managementTeams", managementTeams);
        model.addAttribute("memberTeams", memberTeams);
        model.addAttribute("currentUser", currUser);
        model.addAttribute("currentFilter", filter);

        return "teams/my-teams";
    }

    private User getCurrentUser(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser();
    }

}
