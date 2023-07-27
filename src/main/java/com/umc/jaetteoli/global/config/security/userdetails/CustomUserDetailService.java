package com.umc.jaetteoli.global.config.security.userdetails;

import com.umc.jaetteoli.domain.web.seller.repository.SellerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CustomUserDetailService implements UserDetailsService {
    private SellerRepository sellerRepository;


    private PasswordEncoder passwordEncoder;

    @Override // 유저PK를 통한 사용자 검색
    public UserDetails loadUserByUsername(String userIdx) throws UsernameNotFoundException {
        return sellerRepository.findById(Long.parseLong(userIdx))
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
    }
}
