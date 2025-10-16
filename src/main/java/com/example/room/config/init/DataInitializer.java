package com.example.room.config.init;

import com.example.room.model.Role;
import com.example.room.model.User;
import com.example.room.repository.RoleRepository;
import com.example.room.repository.UserRepository;
import com.example.room.utils.Enums.GenderEnum;
import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordUtil passwordUtil;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        String email = "admin@gmail.com";
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return;
        }

        Role role = roleRepository.findByName(RoleEnum.ADMIN)
                .orElseGet(() -> {
                    return roleRepository.save(Role.builder()
                            .name(RoleEnum.ADMIN)
                            .description("Quyền quản trị hệ thống")
                            .build());
                });

        User admin = User.builder()
                .address("admin")
                .email("admin@gmail.com")
                .fullName("admin")
                .phone("0987654321")
                .citizenId("098765456782")
                .gender(GenderEnum.FEMALE)
                .password(passwordUtil.encode("admin"))
                .role(role)
                .build();

        userRepository.save(admin);
    }
}
