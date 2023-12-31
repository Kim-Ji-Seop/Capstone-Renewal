package com.uou.capstone.global.config.security.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uou.capstone.global.config.error.exception.BaseException;
import com.uou.capstone.global.config.redis.RedisDao;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static com.uou.capstone.global.config.error.ErrorCode.EXPIRED_AUTHENTICATION;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final RedisDao redisDao;

    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey, RedisDao redisDao){
        this.redisDao = redisDao;
        byte[] secretByteKey = DatatypeConverter.parseBase64Binary(secretKey);
        this.key = Keys.hmacShaKeyFor(secretByteKey);
    }

    // 토큰 유효시간
    private static final int JWT_EXPIRATION_MS = 604800000; // 유효시간 : 일주일

    // jwt 토큰 생성
    public TokenDto generateToken(Authentication authentication, Long userIdx) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);
        String accessToken=Jwts.builder()
                .setSubject(username) // 사용자
                .claim("auth",authorities)
                .claim("userIdx",userIdx)
                .setIssuedAt(new Date()) // 현재 시간 기반으로 생성
                .setExpiration(new Date(now.getTime()+30 * 60 * 1000L)) // 만료 시간 세팅 (30분)
                .signWith(key, SignatureAlgorithm.HS256) // 사용할 암호화 알고리즘, signature에 들어갈 secret 값 세팅
                .compact();
        String refreshToken=Jwts.builder()
                .setSubject(username) // 사용자
                .setIssuedAt(new Date()) // 현재 시간 기반으로 생성
                .setExpiration(expiryDate) // 만료 시간 세팅
                .signWith(key, SignatureAlgorithm.HS256) // 사용할 암호화 알고리즘, signature에 들어갈 secret 값 세팅
                .compact();
        // redis에 저장
        redisDao.setValues(authentication, refreshToken, JWT_EXPIRATION_MS + 5000L);

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    private Claims parseClaims(String accessToken){
        try{
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        }catch(ExpiredJwtException e){
            return e.getClaims();
        }
    }

    public Authentication getAuthentication(String accessToken){
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if(claims.get("auth") == null){
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        UserDetails principal = new User(claims.getSubject(),"",authorities);
        return new UsernamePasswordAuthenticationToken(principal,"",authorities);
    }

    // Jwt 토큰에서 이메일:프로바이더 추출
    public String getUserEmailAndProviderFromJWT(String token) {
        System.out.println(token);
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();

        }catch (ExpiredJwtException e) {
            // 토큰이 만료되었을 때, 여전히 Subject claim을 가져올 수 있습니다.
            return e.getClaims().getSubject();
        }
    }

    // refresh token으로 accessToken 재발급
    public TokenDto reissue(String userEmailAndProvider, String rtkKey, Long userIdx, Authentication authentication) throws JsonProcessingException {

        if(!rtkKey.equals(userEmailAndProvider)) {
            throw new BaseException(EXPIRED_AUTHENTICATION);
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);

        String accessToken=Jwts.builder()
                .setSubject(username) // 사용자
                .claim("auth",authorities)
                .claim("userIdx",userIdx)
                .setIssuedAt(new Date()) // 현재 시간 기반으로 생성
                .setExpiration(new Date(now.getTime()+30 * 60 * 1000L)) // 만료 시간 세팅 (30분)
                .signWith(key, SignatureAlgorithm.HS256) // 사용할 암호화 알고리즘, signature에 들어갈 secret 값 세팅
                .compact();

        String refreshToken = redisDao.getValues(rtkKey);

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
