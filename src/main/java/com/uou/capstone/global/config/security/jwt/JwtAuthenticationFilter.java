package com.uou.capstone.global.config.security.jwt;

import com.uou.capstone.global.config.error.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.uou.capstone.global.config.error.ErrorCode.INVALID_TOKEN;
import static com.uou.capstone.global.config.error.ErrorCode.REDIS_ERROR;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends GenericFilterBean {


    // 클라이언트 요청 시 JWT 인증을 하기 위해 설치하는 커스텀 필터로 UsernamePasswordAuthenticationFilter 이전에 실행

    // Username + Password를 통한 인증을 Jwt를 통해 수행한다는 것이다.

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 1. Request Header 에서 JWT 토큰 추출
        String token = resolveToken((HttpServletRequest) request);
        try{
            // 2. validateToken 으로 토큰 유효성 검사 ( 기간이 유효한 토큰인지? )
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 토큰이 유효할 경우 토큰에서 유저정보를 받아온다
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }catch (RedisConnectionFailureException e){
            SecurityContextHolder.clearContext();
            throw new BaseException(REDIS_ERROR);
        }catch (Exception e){
            throw new BaseException(INVALID_TOKEN);
        }

        chain.doFilter(request, response);
    }



    // Request Header 에서 토큰 정보 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("X-ACCESS-TOKEN");
        // StringUtils.hasText(bearerToken): bearerToken에 값이 있는지 확인
        // bearerToken.startsWith("Bearer"): "X-ACCESS-TOKEN" 헤더의 값이 "Bearer"로 시작하는지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

