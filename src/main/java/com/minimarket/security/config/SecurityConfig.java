package com.minimarket.security.config;

import com.minimarket.security.service.CustomUserDetailsService;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.minimarket.security.handler.CustomAccessDeniedHandler;
import com.minimarket.security.handler.CustomAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/productos/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**")
                        .hasRole("GERENTE")

                        .requestMatchers(HttpMethod.GET, "/api/categorias/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**")
                        .hasRole("GERENTE")

                        .requestMatchers("/api/carrito/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")

                        .requestMatchers(HttpMethod.GET, "/api/ventas/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/ventas/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")

                        .requestMatchers("/api/detalle-ventas/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")

                        .requestMatchers("/api/inventario/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")

                        .requestMatchers("/api/usuarios/**")
                        .hasRole("GERENTE")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider())
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}