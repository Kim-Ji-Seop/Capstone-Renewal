package com.uou.capstone.domain.app.user.repository;

import com.sun.istack.NotNull;
import com.uou.capstone.domain.app.user.entity.UserEntity;
import com.uou.capstone.domain.web.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);
    @NotNull
    Optional<UserEntity> findById(@NotNull Long userIdx);
}
