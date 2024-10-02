package goblin.app.Group.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class InviteTokenService {

  // 안전한 키 생성
  private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);

  public String generateInviteToken(Long groupId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + 86400000); // 24시간 만료

    // 초대 링크에 그룹 ID를 담은 JWT 토큰 생성
    return Jwts.builder()
        .setSubject(String.valueOf(groupId))
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(SECRET_KEY)
        .compact();
  }

  public Long validateInviteToken(String token) {
    try {
      return Long.parseLong(
          Jwts.parserBuilder()
              .setSigningKey(SECRET_KEY)
              .build()
              .parseClaimsJws(token)
              .getBody()
              .getSubject());
    } catch (Exception e) {
      throw new RuntimeException("Invalid invite token");
    }
  }
}
