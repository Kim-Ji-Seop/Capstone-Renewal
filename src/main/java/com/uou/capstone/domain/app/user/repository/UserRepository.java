package com.uou.capstone.domain.app.user.repository;

import com.sun.istack.NotNull;
import com.uou.capstone.domain.app.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    @NotNull
    Optional<User> findById(@NotNull Long userIdx);
    Optional<User> findByEmailAndProvider(String email, User.Provider provider);
}
