package com.group3.askmyfriend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class AuthService {

    // 이메일별 인증코드 저장소
    private final Map<String, String> codeMap = new ConcurrentHashMap<>();

    // 인증코드 전송 (콘솔 출력용)
    public String sendVerificationCode(String email) {
        String code = generateRandomCode();
        codeMap.put(email, code);

        // ✅ 콘솔에 출력 (테스트용)
        System.out.println("[콘솔 인증] 이메일: " + email + ", 인증코드: " + code);

        // ❌ 실제 이메일 전송 코드 (현재는 주석 처리)
        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[서담] 이메일 인증번호 안내");
        message.setText("안녕하세요.\n\n이메일 인증번호는 다음과 같습니다:\n\n[" + code + "]\n\n" +
                        "해당 인증번호를 정확히 입력해 주세요.\n\n감사합니다.");
        mailSender.send(message);
        */

        return code;
    }

    // 인증코드 확인
    public boolean verifyCode(String email, String inputCode) {
        String storedCode = codeMap.get(email);
        return storedCode != null && storedCode.equals(inputCode);
    }

    // 랜덤 6자리 숫자 생성
    private String generateRandomCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
