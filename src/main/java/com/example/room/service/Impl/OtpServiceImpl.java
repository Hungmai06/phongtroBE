package com.example.room.service.Impl;

import com.example.room.exception.ResourceNotFoundException;
import com.example.room.model.User;
import com.example.room.repository.UserRepository;
import com.example.room.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final long OTP_VALID_DURATION = 5; // 5 phút
    private static final String OTP_KEY_PREFIX = "otp:";


    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Override
    public void generateAndSendOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        String redisKey = OTP_KEY_PREFIX + email;

        redisTemplate.opsForValue().set(
                redisKey,
                otp,
                Duration.ofMinutes(OTP_VALID_DURATION)
        );
        sendOtpEmail(email, otp);
    }

    private void sendOtpEmail(String toEmail, String otp) {
        Optional<User> optionalUser = userRepository.findByEmail(toEmail);
        if(optionalUser.isEmpty()){
                throw new ResourceNotFoundException("Email không tồn tại trong hệ thống");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã Xác Thực OTP Đặt Lại Mật Khẩu");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã này có hiệu lực trong " + OTP_VALID_DURATION + " phút.");
        mailSender.send(message);
    }

    @Override
    public void validateOtp(String email, String otp) {
        String redisKey = OTP_KEY_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new RuntimeException("Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        redisTemplate.delete(redisKey);
    }

}