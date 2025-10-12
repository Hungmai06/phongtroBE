package com.example.room.config.init;

import com.example.room.model.Role;
import com.example.room.model.User;
import com.example.room.repository.RoleRepository;
import com.example.room.repository.UserRepository;
import com.example.room.utils.Enums.GenderEnum;
import com.example.room.utils.Enums.RoleEnum;
import com.example.room.utils.PasswordUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordUtil passwordUtil;

    @PostConstruct
    @Transactional
    public void init() {
        Optional<User> optionalUser = userRepository.findByRole(RoleEnum.ADMIN);
        if(optionalUser.isPresent()){
            return;
        }
        Optional<Role> optionalRole = roleRepository.findByName(RoleEnum.ADMIN);
        Role role = new Role();
         if(optionalRole.isEmpty()){
             role = Role.builder()
                     .name(RoleEnum.ADMIN)
                     .description("Quyền quản trị hệ thống")
                     .build();
             roleRepository.save(role);
         }else{
             role = optionalRole.get();
         }

            User admin = User.builder()
                    .address("admin")
                    .email("admin@gmail.com")
                    .fullName("admin")
                    .phone("0987654321")
                    .citizenId("098765456782")
                    .gender(GenderEnum.FEMALE)
                    .password(passwordUtil.encode("admin"))
                    .build();
            admin.setRole(role);
            userRepository.save(admin);
    }
}