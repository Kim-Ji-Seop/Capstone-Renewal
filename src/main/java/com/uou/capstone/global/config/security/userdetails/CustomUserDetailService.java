package com.uou.capstone.global.config.security.userdetails;

import com.uou.capstone.domain.app.user.entity.User;
import com.uou.capstone.domain.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String combinedEmailAndProvider) throws UsernameNotFoundException {
        // 파싱
        String[] parts = combinedEmailAndProvider.split(":"); // jskim2x@nate.com:KAKAO 형식을 : 기준으로 파싱
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid format.");
        }

        String email = parts[0];
        User.Provider provider;
        try {
            provider = User.Provider.valueOf(parts[1]);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid provider.");
        }

        UserDetails user = userRepository.findByEmailAndProvider(email, provider)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다."));
        return user;
    }
}
