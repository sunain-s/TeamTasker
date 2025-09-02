package com.teamtasker.repository;

import com.teamtasker.entity.Role;
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
public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Page<User> findByRole(Role role, Pageable pageable);
    long countByRole(Role role);

    @Query("SELECT u FROM User u WHERE u NOT IN (SELECT m FROM Team t JOIN t.members m WHERE t.id = :teamId)")
    Page<User> findUsersNotInTeam(@Param("teamId") Integer teamId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchByNameOrUsername(@Param("searchTerm") String searchTerm, Pageable pageable);
}
