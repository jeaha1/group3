package com.group3.askmyfriend.config;

import com.group3.askmyfriend.service.AdminDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
@Order(1) // ★ 관리자용 보안 설정 먼저 적용
public class AdminSecurityConfig {

    private final AdminDetailsService adminDetailsService;

    // ✅ 암호화 없이 평문 그대로 비교하는 PasswordEncoder (개발 테스트용)
    @Bean
    public PasswordEncoder adminPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminDetailsService);
        provider.setPasswordEncoder(adminPasswordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/admin-login", "/admin-login/**");

        http.csrf(AbstractHttpConfigurer::disable);

        http.authenticationProvider(adminAuthenticationProvider());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin-login", "/admin-login/**").permitAll()
                .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")
                .anyRequest().denyAll()
        );

        http.formLogin(form -> form
                .loginPage("/admin-login")
                .loginProcessingUrl("/admin-login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .usernameParameter("admin_id")
                .passwordParameter("admin_pw")
        );

        return http.build();
    }
}
