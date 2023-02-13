package com.csye7125.webapp.webapp.daos;

import com.csye7125.webapp.webapp.models.users.User;
import io.micrometer.core.annotation.Timed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDAO extends JpaRepository<User, Long> {

    @Timed(value = "get.user.by.name.time", description = "Time taken to get the user by name")
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findByUserName(String email);

    @Timed(value = "get.user.password.time", description = "Time taken to get user's password'")
    @Query("SELECT password FROM User  WHERE email = ?1")
    String findUserPassword(String email);

    @Timed(value = "get.user.by.email.time", description = "Time taken to get the user by email")
    Optional<User> findByEmail(String email);
}
