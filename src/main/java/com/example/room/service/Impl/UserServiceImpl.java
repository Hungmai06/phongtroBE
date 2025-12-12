package com.example.room.service.Impl;

import com.example.room.dto.BaseResponse;
import com.example.room.dto.PageResponse;
import com.example.room.dto.request.UserCreateRequest;
import com.example.room.dto.request.UserUpdateRequest;
import com.example.room.dto.response.UserResponse;
import com.example.room.exception.InvalidDataException;
import com.example.room.exception.ResourceNotFoundException;
import com.example.room.mapper.UserMapper;
import com.example.room.model.Role;
import com.example.room.model.User;
import com.example.room.repository.RoleRepository;
import com.example.room.repository.UserRepository;
import com.example.room.service.EmailService;
import com.example.room.service.OtpService;
import com.example.room.service.UserService;
import com.example.room.specification.UserSpecification;
import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.PasswordUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final OtpService otpService;
    @Override
    public BaseResponse<UserResponse> create(UserCreateRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if(optionalUser.isPresent()){
            throw new InvalidDataException("Email đã tồn tại");
        }
        User user = User.builder()
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordUtil.encode(request.getPassword()))
                .gender(request.getGender())
                .citizenId(request.getCitizenId())
                .fullName(request.getFullName())
                .address(request.getAddress())
                .build();
        Role role = roleRepository.findByName(RoleEnum.RENTER).get();
        user.setRole(role);
        userRepository.save(user);

        return BaseResponse.<UserResponse>builder()
                .code(201)
                .data(userMapper.toResponse(user))
                .message("Tạo user thành công")
                .build();
    }

    @Override
    public BaseResponse<UserResponse> update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("User không tồn tại")
        );

        if(request.getEmail()!= null && !request.getEmail().isEmpty()){
            user.setEmail(request.getEmail());
        }
        if(request.getPhone()!= null && !request.getPhone().isEmpty()) user.setPhone(request.getPhone());
        if(request.getGender()!= null ) user.setGender(request.getGender());
        if(request.getAddress()!= null && !request.getAddress().isEmpty()) user.setAddress(request.getAddress());
        if(request.getFullName()!= null && !request.getFullName().isEmpty()) user.setFullName(request.getFullName());
        if(request.getPassword()!= null && !request.getPassword().isEmpty()) user.setPassword(passwordUtil.encode(request.getPassword()));
        if(request.getCitizenId()!= null && !request.getCitizenId().isEmpty()) user.setCitizenId(request.getCitizenId());
        userRepository.save(user);
        return BaseResponse.<UserResponse>builder()
                .code(200)
                .data(userMapper.toResponse(user))
                .message("Cập nhật thông tin người dùng")
                .build();
    }

    @Override
    public BaseResponse<UserResponse> findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Người dùng không tồn tại")
        );
        return BaseResponse.<UserResponse>builder()
                .code(200)
                .data(userMapper.toResponse(user))
                .message("Lấy thông tin người dùng")
                .build();
    }

    @Override
    public PageResponse<UserResponse> findByRoleName(RoleEnum name, Integer page, Integer size) {
        Role role = roleRepository.findByName(name).orElseThrow(
                ()-> new ResourceNotFoundException("Vai trò không tồn tại")
        );

        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> userPage = userRepository.findAllByRole(role, pageable).map(user -> userMapper.toResponse(user));
        List<UserResponse> userResponses = userPage.getContent().stream().toList();
        return PageResponse.<UserResponse>builder()
                .code(200)
                .data(userResponses)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .pageNumber(userPage.getNumber())
                .message("Lấy danh sách người dùng theo vai trò")
                .build();

    }

    @Override
    public BaseResponse<String> delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Người dùng không tồn tại")
        );
        user.setDeleted(Boolean.TRUE);
        userRepository.save(user);
        return BaseResponse.<String>builder()
                .message("Xóa user")
                .code(204)
                .data("Xóa người dùng thành công")
                .build();
    }

    @Override
    public PageResponse<UserResponse> search(String q, Integer page, Integer size, String sort) {
        Sort sortOption = Sort.by(Sort.Direction.ASC, "createdAt");

        if (sort != null && sort.equalsIgnoreCase("desc")) {
            sortOption = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(page,size,sortOption);

        Page<User> userPage = userRepository.findAll(UserSpecification.filter(q),pageable);

        List<UserResponse> users = userPage.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .address(user.getAddress())
                .citizenId(user.getCitizenId())
                .gender(user.getGender())
                .roleName(user.getRole().getName())
                .build() ).toList();
        return PageResponse.<UserResponse>builder()
                .code(200)
                .data(users)
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .pageNumber(userPage.getNumber())
                .message("Lấy danh sách người dùng")
                .build();
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        otpService.validateOtp(email, otp);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        user.setPassword(passwordUtil.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void chanegePassword( String oldPassword, String newPassword) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!passwordUtil.matches(oldPassword, user.getPassword())){
            throw new InvalidDataException("Mật khẩu cũ không đúng");
        }
        user.setPassword( passwordUtil.encode(newPassword));
        userRepository.save(user);
    }
}