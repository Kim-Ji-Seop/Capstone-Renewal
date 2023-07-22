package com.umc.jaetteoli.domain.web.seller.service;

import com.umc.jaetteoli.domain.web.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
}
