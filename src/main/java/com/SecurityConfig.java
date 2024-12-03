package com;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // REST API 사용 시에는 CSRF 비활성화
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/login", "/register").permitAll() // 로그인, 회원가입은 모두 허용
                .anyRequest().authenticated() // 나머지 요청은 인증 필요
            )
            .formLogin(form -> form.disable()) // 기본 폼 로그인 사용 안함
            .httpBasic(httpBasic -> httpBasic.disable()); // HTTP 기본 인증 비활성화

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser("user")
            .password(passwordEncoder().encode("password"))
            .roles("USER")
            .and()
            .withUser("admin")
            .password(passwordEncoder().encode("admin"))
            .roles("ADMIN");
    }
}