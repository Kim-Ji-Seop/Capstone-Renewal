package com.umc.jaetteoli.global.config.security;

import com.umc.jaetteoli.global.config.security.jwt.JwtAuthenticationFilter;
import com.umc.jaetteoli.global.config.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //jwt 사용
                .and()
                .authorizeRequests()
                // 판매자 회원관련 API는 비밀번호 재설정빼고 토큰 필요 X
                .antMatchers("/api/web/jat/sellers/pw-restore").hasRole("SELLER")
                .antMatchers("/api/web/jat/sellers/**").permitAll()
                // 그 외 웹 요청 판매자 권한으로 들어와야 함
                .antMatchers("api/web/jat/**").hasRole("SELLER")
                // app 부분 들어가면 전체 허용 해제 해야함
                .antMatchers("/api/app/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
