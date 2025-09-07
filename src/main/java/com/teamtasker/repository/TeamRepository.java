package com.teamtasker.repository;

import com.teamtasker.entity.Team;
import com.teamtasker.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team,Integer> {
    Optional<Team> findByName(String name);
    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Team> findByIsActiveTrue(Pageable pageable);
    Page<Team> findByIsActiveFalse(Pageable pageable);

    List<Team> findByOwner(User owner);
    List<Team> findByOwnerAndIsActiveTrue(User owner);
    List<Team> findByMembersContaining(User user);
    List<Team> findByMembersContainingAndIsActiveTrue(User user);
    List<Team> findByManagersContaining(User user);
    List<Team> findByManagersContainingAndIsActiveTrue(User user);

    @Query("SELECT t FROM Team t WHERE t.owner = :user OR :user MEMBER OF t.managers")
    List<Team> findTeamsWithManagementRights(@Param("user") User user);

    @Query("SELECT t FROM Team t WHERE (t.owner = :user OR :user MEMBER OF t.managers) AND t.isActive = true")
    List<Team> findActiveTeamsWithManagementRights(@Param("user") User user);

    @Query("SELECT COUNT(t) FROM Team t WHERE t.owner = :user")
    long countTeamsByOwner(@Param("user") User user);

    @Query("SELECT COUNT(t) FROM Team t WHERE :user MEMBER OF t.managers")
    long countTeamsByManager(@Param("user") User user);

    @Query("SELECT COUNT(t) FROM Team t WHERE :user MEMBER OF t.members")
    long countTeamsByMember(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t WHERE t.id = :teamId AND (t.owner = :user OR :user MEMBER OF t.managers)")
    boolean userHasManagementRights(@Param("teamId") Integer teamId, @Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t WHERE t.id = :teamId AND (t.owner = :user OR :user MEMBER OF t.members)")
    boolean isUserAssociatedWithTeam(@Param("teamId") Integer teamId, @Param("user") User user);
}
