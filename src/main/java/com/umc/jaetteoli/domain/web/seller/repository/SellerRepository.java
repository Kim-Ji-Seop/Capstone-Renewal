package com.umc.jaetteoli.domain.web.seller.repository;

import com.umc.jaetteoli.domain.web.seller.entity.Seller;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller,Long> {
    Optional<Seller> findByUid(String uid);
    @NotNull
    Optional<Seller> findById(@NotNull Long sellerIdx);
}
