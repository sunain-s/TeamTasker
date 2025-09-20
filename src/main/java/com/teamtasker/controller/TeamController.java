package com.teamtasker.controller;

import com.teamtasker.auth.CustomUserDetails;
import com.teamtasker.entity.Team;
import com.teamtasker.entity.User;
import com.teamtasker.service.TeamService;
import com.teamtasker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/create")
    public String showCreateTeamForm(Model model) {
        if (!model.containsAttribute("team")) {
            model.addAttribute("team", new Team());
        }
        return "teams/create";
    }

    @GetMapping("/create")
    public String createTeam(@Valid @ModelAttribute("team") Team team,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (teamService.isTeamNameTaken(team.getName())) {
            result.rejectValue("name", "isNameTaken", "Team name already exists");
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.team", result);
            redirectAttributes.addFlashAttribute("team", team);
            return "redirect:/teams/create";
        }

        User currUser = getCurrentUser(authentication);
        Team createdTeam = teamService.createTeam(team.getName(), team.getDescription(), currUser);
        redirectAttributes.addFlashAttribute("createdTeam", "Team: '" + createdTeam.getName() + "' created successfully!");
        return "redirect:/teams/" + createdTeam.getId();
    }

    @GetMapping("/{teamId}")
    public String viewTeam(@PathVariable int teamId, Model model, Authentication authentication) {
        User currUser = getCurrentUser(authentication);
        Team team = teamService.getTeamById(teamId);
        if (!teamService.hasViewAccess(team, currUser)) {
            model.addAttribute("user_error_message", "You don't have access to this team");
            return "error/403";
        }

        model.addAttribute("team", team);
        model.addAttribute("currentUser", currUser);
        model.addAttribute("hasManagementAccess", teamService.hasManagementAccess(team, currUser));
        model.addAttribute("isOwner", team.isOwner(currUser));
        return "teams/view";
    }

    private User getCurrentUser(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser();
    }
}
