package com.example.room.service;

import com.example.room.utils.Enums.TokenType;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface JwtService {
    String generateAccessToken(long userid, String username, Collection<? extends GrantedAuthority> authorities);
    String generateRefreshToken(long userid, String username, Collection<? extends GrantedAuthority> authorities);
    String extractUsername(String token , TokenType type);
}