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

    //------------------------------------------------------------------------------------------------------------------
    // Team Listing

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

    //------------------------------------------------------------------------------------------------------------------
    // Create + Delete Team

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
        redirectAttributes.addFlashAttribute("createdTeam", "Team: '" + createdTeam.getName() + "' successfully  created");
        return "redirect:/teams/" + createdTeam.getId();
    }

    @PostMapping("/{teamId}/delete")
    public String deleteTeam(@PathVariable("teamId") Integer teamId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User currUser = getCurrentUser(authentication);
            Team team = teamService.getTeamById(teamId);
            String teamName = team.getName();
            teamService.deleteTeam(teamId, currUser);
            redirectAttributes.addFlashAttribute("success_message", "Team: '" + teamName + "' successfully deleted");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("user_error_message", e.getMessage());
            return "redirect:/teams/" + teamId;
        }
        return "redirect:/teams";
    }

    //------------------------------------------------------------------------------------------------------------------
    // View + Edit Team

    @GetMapping("/{teamId}")
    public String viewTeam(@PathVariable Integer teamId, Model model, Authentication authentication) {
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

    @GetMapping("/{teamId}/edit")
    public String editTeam(@PathVariable Integer teamId, Model model, Authentication authentication) {
        User currUser = getCurrentUser(authentication);
        Team team = teamService.getTeamById(teamId);
        if (!teamService.hasManagementAccess(team, currUser)) {
            model.addAttribute("user_error_message", "You don't have permission to edit this team");
            return "error/403";
        }
        if (!model.containsAttribute("team")) {
            model.addAttribute("team", team);
        }
        return "teams/edit";
    }

    @PostMapping("/{teamId}/edit")
    public String updateTeam(@PathVariable Integer teamId,
                             @Valid @ModelAttribute("team") Team updatedTeam,
                             BindingResult result,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {

        User currUser = getCurrentUser(authentication);
        Team existingTeam = teamService.getTeamById(teamId);
        if (!updatedTeam.getName().equals(existingTeam.getName()) && teamService.isTeamNameTaken(updatedTeam.getName())) {
            result.rejectValue("name", "isNameTaken", "Team name already exists");
        }
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.team", result);
            redirectAttributes.addFlashAttribute("team", updatedTeam);
            return "redirect:/teams/" + teamId + "/edit";
        }

        try {
            teamService.updateTeam(teamId, updatedTeam.getName(), updatedTeam.getDescription(), currUser);
            redirectAttributes.addFlashAttribute("success_message", "Team successfully updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("user_error_message", e.getMessage());
        }
        return "redirect:/teams/" + teamId;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Team Activation

    @PostMapping("/{teamId}/deactivate")
    public String deactivateTeam(@PathVariable Integer teamId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User currUser = getCurrentUser(authentication);
            teamService.deactivateTeam(teamId, currUser);
            redirectAttributes.addFlashAttribute("success_message", "Team successfully deactivated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("user_error_message", e.getMessage());
        }
        return "redirect:/teams/" + teamId;
    }

    @PostMapping("/{teamId}/reactivate")
    public String reactivateTeam(@PathVariable Integer teamId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User currUser = getCurrentUser(authentication);
            teamService.reactivateTeam(teamId, currUser);
            redirectAttributes.addFlashAttribute("success_message", "Team successfully reactivated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("user_error_message", e.getMessage());
        }
        return "redirect:/teams/" + teamId;
    }

    private User getCurrentUser(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser();
    }
}
