package com.minimarket.security.config;

import com.minimarket.security.handler.CustomAccessDeniedHandler;
import com.minimarket.security.handler.CustomAuthenticationEntryPoint;
import com.minimarket.security.jwt.JwtAuthenticationFilter;
import com.minimarket.security.service.CustomUserDetailsService;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()

                        // Productos
                        .requestMatchers(HttpMethod.GET, "/api/productos/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**")
                        .hasRole("GERENTE")

                        // Categorias
                        .requestMatchers(HttpMethod.GET, "/api/categorias/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**")
                        .hasRole("GERENTE")

                        // Carrito
                        .requestMatchers("/api/carrito/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")

                        // Ventas
                        .requestMatchers(HttpMethod.GET, "/api/ventas/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/ventas/**")
                        .hasAnyRole("CLIENTE", "EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/ventas/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/ventas/**")
                        .hasRole("GERENTE")

                        // Detalle ventas
                        .requestMatchers(HttpMethod.GET, "/api/detalle-ventas/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.POST, "/api/detalle-ventas/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/detalle-ventas/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/detalle-ventas/**")
                        .hasRole("GERENTE")

                        // Inventario
                        .requestMatchers("/api/inventario/**")
                        .hasAnyRole("EMPLEADO", "GERENTE")

                        // Usuarios
                        .requestMatchers("/api/usuarios/**")
                        .hasRole("GERENTE")

                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}