package com.modeon.backend.service;

import com.modeon.backend.dto.EmailRequest;
import com.modeon.backend.entity.User;
import com.modeon.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final MembershipService membershipService;

    // 랜덤으로 숫자 생성
    public String createNumber() {
        Random random = new Random();
        StringBuilder key = new StringBuilder();

        for (int i = 0; i < 8; i++) { // 인증 코드 8자리
            int index = random.nextInt(3); // 0~2까지 랜덤, 랜덤값으로 switch문 실행

            switch (index) {
                case 0 -> key.append((char) (random.nextInt(26) + 97)); // 소문자
                case 1 -> key.append((char) (random.nextInt(26) + 65)); // 대문자
                case 2 -> key.append(random.nextInt(10)); // 숫자
            }
        }
        return key.toString();
    }

    public MimeMessage createMail(String mail, String number) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.setFrom(mail);
        message.setRecipients(MimeMessage.RecipientType.TO, mail);
        message.setSubject("이메일 인증");
        String body = "";
        body += "<h3>ModeOn 에서 요청하신 인증 번호 입니다.</h3>";
        body += "<h1>" + number + "</h1>";
        body += "<h3>감사합니다.</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

    public String sendSimpleMessage(String sendEmail) throws MessagingException {

        if(!userRepository.existsByEmail(sendEmail)) {
            throw new IllegalArgumentException("User not found");
        }


        String number = createNumber();

        MimeMessage message = createMail(sendEmail, number);

        try {
            javaMailSender.send(message); // 메일 발송
            String redisKey = "EmailCodeCache::" + sendEmail;
            redisTemplate.opsForValue().set(redisKey, number, 3, TimeUnit.MINUTES);
        } catch (MailException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("메일 발송 중 오류가 발생했습니다.");
        }
        return number;
    }

    public String getCode(String email){
        return redisTemplate.opsForValue().get("EmailCodeCache::" + email);
    }

    public boolean verifyEmail(EmailRequest emailRequest, String emailCode){
        String email = emailRequest.getEmail();
        if (!emailCode.equals(getCode(email))){
            throw new IllegalArgumentException("wrong Code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        // 이메일 인증 완료 시 웰컴 쿠폰 발급
        log.info("이메일 인증 완료, 웰컴 쿠폰 발급 시작: userId={}", user.getId());
        try {
            membershipService.welcomeCoupon(user.getId());
            log.info("웰컴 쿠폰 발급 완료: userId={}", user.getId());
        } catch (Exception e) {
            log.error("웰컴 쿠폰 발급 중 오류 발생: userId={}", user.getId(), e);
            // 쿠폰 발급 실패해도 이메일 인증은 완료된 것으로 처리
        }

        return user.isEnabled();
    }

    public String findPw(EmailRequest emailRequest, String emailCode){
        String email = emailRequest.getEmail();
        if (!emailCode.equals(getCode(email))){
            throw new IllegalArgumentException("wrong Code");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("User not found"));

        String number = createNumber();
        String redisKey = "PwChangeCodeCache::" + email;
        redisTemplate.opsForValue().set(redisKey, number, 5, TimeUnit.MINUTES);

        return number;
    }
}