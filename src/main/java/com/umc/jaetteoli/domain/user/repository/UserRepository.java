package com.umc.jaetteoli.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.umc.jaetteoli.domain.user.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long> {
    boolean existsByUid(String uid);
    boolean existsByNickName(String nickName);
}