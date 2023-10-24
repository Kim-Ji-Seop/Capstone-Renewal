package com.uou.capstone.global.config.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uou.capstone.global.config.error.BaseResponse;
import com.uou.capstone.global.config.error.ErrorCode;
import com.uou.capstone.global.config.redis.RedisDao;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisDao redisDao;

    // Filter를 무시할 URL 형식을 정하는 함수
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        request.getMethod();
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return (
                pathMatcher.match("/api/app/test", path) && request.getMethod().equals("GET") ||
                pathMatcher.match("/api/app/users/auth/email/validation", path) && request.getMethod().equals("POST") || // 이메일 유효성 검사
                pathMatcher.match("/api/app/users/auth/email", path) && request.getMethod().equals("POST") || // 이메일 회원가입
                pathMatcher.match("/api/app/users/login/email", path) && request.getMethod().equals("POST") || // 이메일 로그인
                pathMatcher.match("/api/app/users/auth/kakao", path) && request.getMethod().equals("POST") || // 카카오 로그인
                pathMatcher.match("/api/app/users/auth/google", path) && request.getMethod().equals("POST") // 구글 로그인
        );
    }

    // Filter 방식 커스텀
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwt = getJwtFromRequest(request); // request에서 jwt 토큰을 꺼낸다.

        if(jwt==null){
            sendErrorResponse(response,ErrorCode.TOKEN_NOT_EXIST);
        }
        else  {

            try{
                //Redis 에 해당 accessToken logout 여부 확인
                String isLogout = redisDao.getValues(jwt);
                if (ObjectUtils.isEmpty(isLogout)) {
                    String userEmailAndProvider = jwtTokenProvider.getUserEmailAndProviderFromJWT(jwt); //jwt에서 subject 추출

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userEmailAndProvider, null, null); // 식별자 인증
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //기본적으로 제공한 details 세팅
                    SecurityContextHolder.getContext().setAuthentication(authentication); //세션에서 계속 사용하기 위해 securityContext에 Authentication 등록

                }
                filterChain.doFilter(request, response);

                // catch 구문 jwt 토큰 유효성 검사
            }catch (IllegalArgumentException e) {
                log.error("an error occured during getting username from token", e);
                sendErrorResponse(response,ErrorCode.INVALID_TOKEN);
            } catch (ExpiredJwtException e) {
                log.warn("the token is expired and not valid anymore", e);
                sendErrorResponse(response,ErrorCode.ACCESS_TOKEN_EXPIRED);
            } catch(SignatureException e){
                log.error("Authentication Failed. Username or Password not valid.");
                sendErrorResponse(response,ErrorCode.FAIL_AUTHENTICATION);
            }catch(UnsupportedJwtException e){
                log.error("UnsupportedJwt");
                sendErrorResponse(response,ErrorCode.FAIL_AUTHENTICATION);
            }
        }
    }

    // 헤더에 붙여 전송한 "Bearer {JWT 토큰}" 형식 추출
    // Key = Authorization , Value = Bearer {JWT 토큰}
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (!StringUtils.isEmpty(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring("Bearer ".length());
        }
        return null;
    }

    // 에러 응답 커스텀 함수
    private void sendErrorResponse(HttpServletResponse httpServletResponse, ErrorCode errorCode) throws IOException{
        httpServletResponse.setCharacterEncoding("utf-8");
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setContentType(APPLICATION_JSON_VALUE);

        BaseResponse errorResponse = new BaseResponse(errorCode);
        //object를 텍스트 형태의 JSON으로 변환
        new ObjectMapper().writeValue(httpServletResponse.getWriter(), errorResponse);
    }
}

