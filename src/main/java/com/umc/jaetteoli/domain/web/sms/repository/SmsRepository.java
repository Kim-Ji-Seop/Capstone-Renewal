package com.umc.jaetteoli.domain.web.sms.repository;

import com.umc.jaetteoli.domain.web.seller.entity.Seller;
import com.umc.jaetteoli.domain.web.sms.entity.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmsRepository extends JpaRepository<Sms,Long> {

}