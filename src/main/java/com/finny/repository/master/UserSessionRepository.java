package com.finny.repository.master;

import com.finny.domain.master.User;
import com.finny.domain.master.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    List<UserSession> findByUserAndActiveTrue(User user);
    java.util.Optional<UserSession> findByTokenAndActiveTrue(String token);
}
