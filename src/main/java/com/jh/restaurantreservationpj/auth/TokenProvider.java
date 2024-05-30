package com.jh.restaurantreservationpj.auth;

import com.jh.restaurantreservationpj.member.service.MemberService;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenProvider {

    private static final String KEY_ROLES = "roles";
    private static final long TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1hour
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final MemberService memberService;

    @Value("${spring.jwt.secret}")
    private String secret;

    // 토큰 생성 발급
    public String generateToken(String userName, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userName);
        claims.put(KEY_ROLES, roles);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(now) // 토큰 생성 시간
                .setExpiration(expireDate) // 토큰 만료 시간
                .signWith(SignatureAlgorithm.HS512, secret) // 사용할 암호화 알고리즘, 비밀키
                .compact();
    }

    // jwt를 사용하여 사용자의 인증 정보 가져오는 메소드
    @Transactional
    public Authentication getAuthentication(String jwt) {
        UserDetails userDetails = memberService.loadUserByUsername(getUserName(jwt));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 유저 아이디 가져오기
    public String getUserName(String token) {
        return parseClaims(token).getSubject();
    }

    // 헤더 정보의 토큰을 가져온다.
    public String resolveTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }

        return null;
    }

    // 컨트롤러에서 사용할 메소드
    // 토큰을 통해 회원 아이디를 가져온다.
    public String getUserId(HttpServletRequest request) {
        String token = resolveTokenFromRequest(request);
        return getUserName(token);
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;

        Claims claims = parseClaims(token);
        return !claims.getExpiration().before(new Date());
    }

    // 토큰 정보 가져오기
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.error("토큰 정보 에러 = {}", e.getMessage());
            return e.getClaims();
        }
    }
}
