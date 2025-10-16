package com.example.room.service.Impl;

import com.example.room.dto.request.RefreshTokenRequest;
import com.example.room.dto.request.SignInRequest;
import com.example.room.dto.response.TokenResponse;
import com.example.room.dto.response.UserResponse;
import com.example.room.exception.ForBiddenException;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.UserMapper;
import com.example.room.model.Role;
import com.example.room.model.User;
import com.example.room.repository.RoleRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.AuthenticationService;
import com.example.room.service.JwtService;
import com.example.room.utils.Enums.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    @Override
    public TokenResponse getAccessToken(SignInRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }catch (AccessDeniedException e){
            throw new AccessDeniedException(e.getMessage());
        }
        var user = userRepository.findByEmail(request.getEmail()).get();
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }
        Role role = roleRepository.findById(user.getRole().getId()).orElseThrow(
                ()->new ResourceNotFoundException("Role not found")
        );
        UserResponse userResponse = userMapper.toResponse(user);
        String accessToken = jwtService.generateAccessToken(user.getId(),request.getEmail(),user.getAuthorities());
        String refreshToken = jwtService.generateRefreshToken(user.getId(),request.getEmail(),user.getAuthorities());
        return TokenResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .roleName(role.getName())
                .userResponse(userResponse)
                .build();
    }

    @Override
    public TokenResponse getRefreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if(!StringUtils.hasLength(refreshToken)){
            throw new InvalidDataException("Token must be not blank ");
        }
        try {
            String email = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
            User user = userRepository.findByEmail(email).orElseThrow(
                    ()-> new ResourceNotFoundException("User not found")
            );
            Role role = roleRepository.findById(user.getRole().getId()).orElseThrow(
                    ()->new ResourceNotFoundException("Role not found")
            );
            UserResponse userResponse = userMapper.toResponse(user);
            String accessToken = jwtService.generateAccessToken(user.getId(),user.getUsername(),user.getAuthorities());
            return TokenResponse.builder()
                    .refreshToken(refreshToken)
                    .accessToken(accessToken)
                    .roleName(role.getName())
                    .userResponse(userResponse)
                    .build();
        }catch (Exception e){
            throw new ForBiddenException(e.getMessage());
        }
    }
}