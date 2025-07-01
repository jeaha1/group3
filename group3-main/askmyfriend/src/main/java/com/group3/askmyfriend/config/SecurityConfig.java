package com.group3.askmyfriend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.group3.askmyfriend.service.CustomUserDetailsService;

@Configuration
@Order(2) // ★ 사용자용 보안 설정은 두 번째로 적용
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationFailureHandler customAuthFailureHandler;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          AuthenticationFailureHandler customAuthFailureHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthFailureHandler = customAuthFailureHandler;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/**"); // ✅ 모든 사용자 요청 적용

        // 세션 관리
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
                .maximumSessions(1).expiredUrl("/auth/login?expired")
        );

        // CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // 인증 프로바이더 등록
        http.authenticationProvider(userAuthenticationProvider());

        // 권한 설정
        http.authorizeHttpRequests(auth -> auth
                // 퍼블릭 리소스
                .requestMatchers(
                        new AntPathRequestMatcher("/"),
                        new AntPathRequestMatcher("/auth/login"),
                        new AntPathRequestMatcher("/auth/signup"),
                        new AntPathRequestMatcher("/auth/find-password"),
                        new AntPathRequestMatcher("/auth/send-code"),
                        new AntPathRequestMatcher("/auth/verify-code"),
                        new AntPathRequestMatcher("/auth/reset-password"),
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**"),
                        new AntPathRequestMatcher("/images/**"),
                        new AntPathRequestMatcher("/error/**"),
                        new AntPathRequestMatcher("/api/auth/**")
                ).permitAll()
                // 신고 등록 API는 인증 없이 허용
                .requestMatchers(HttpMethod.POST, "/reports").permitAll()
                // 관리자 전용 페이지/API
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 기타 나머지 요청은 인증 필요
                .anyRequest().authenticated()
        );

        // 로그인 폼
        http.formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/loginProc")
                .usernameParameter("loginId")
                .passwordParameter("password")
                .defaultSuccessUrl("/index", true)
                .failureHandler(customAuthFailureHandler)
        );

        // 로그아웃
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
        );

        return http.build();
    }
}
