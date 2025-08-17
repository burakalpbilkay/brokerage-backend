package com.brokaragefirm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.brokaragefirm.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomerRepository customerRepository;

  @Value("${app.security.admin.username}")
  String adminUser;
  @Value("${app.security.admin.password}")
  String adminPass;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(PasswordEncoder encoder) {
    InMemoryUserDetailsManager inMem = new InMemoryUserDetailsManager(
        User.withUsername(adminUser).password(encoder.encode(adminPass)).roles("ADMIN").build());
    return username -> {
      if (inMem.userExists(username))
        return inMem.loadUserByUsername(username);
      return customerRepository.findByUsername(username)
          .map(c -> User.withUsername(c.getUsername())
              .password(c.getPassword())
              .roles(c.getRoles().replace("ROLE_", "").split(","))
              .disabled(!c.isEnabled())
              .build())
          .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    };
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable());
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/h2-console/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/**").hasAnyRole("ADMIN", "CUSTOMER")
        .anyRequest().authenticated());
    http.headers(h -> h.frameOptions(f -> f.disable())); 
    http.httpBasic(Customizer.withDefaults());
    return http.build();
  }
}
