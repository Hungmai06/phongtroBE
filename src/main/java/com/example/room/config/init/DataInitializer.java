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

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordUtil passwordUtil;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        initRoles();

        initAdminUser();
    }

    private void initRoles() {
        Arrays.stream(RoleEnum.values()).forEach(roleEnum -> {
            Optional<Role> optionalRole = roleRepository.findByName(roleEnum);
            if (optionalRole.isEmpty()) {
                Role role = Role.builder()
                        .name(roleEnum)
                        .description("Vai trò " + roleEnum.name())
                        .build();
                roleRepository.save(role);
            }
        });
    }

    private void initAdminUser() {
        String email = "admin@gmail.com";
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return; 
        }

        Role adminRole = roleRepository.findByName(RoleEnum.ADMIN).get();

        User admin = User.builder()
                .address("admin")
                .email("admin@gmail.com")
                .fullName("admin")
                .phone("0987654321")
                .citizenId("098765456782")
                .gender(GenderEnum.FEMALE)
                .password(passwordUtil.encode("admin"))
                .role(adminRole)
                .build();

        userRepository.save(admin);
    }
}