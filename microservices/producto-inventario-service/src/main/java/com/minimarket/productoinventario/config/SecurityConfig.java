package com.minimarket.productoinventario.config;

import com.minimarket.productoinventario.security.JwtAuthenticationFilter;
import com.minimarket.productoinventario.security.RestAccessDeniedHandler;
import com.minimarket.productoinventario.security.RestAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/h2-console/**",
                                "/error"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/productos/*/movimientos",
                                "/api/productos/*/movimientos/**"
                        )
                        .hasAnyRole("EMPLEADO", "GERENTE")

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos/*/movimientos",
                                "/api/productos/*/movimientos/**"
                        )
                        .hasAnyRole("EMPLEADO", "GERENTE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/productos/**"
                        )
                        .hasAnyRole(
                                "CLIENTE",
                                "EMPLEADO",
                                "GERENTE"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos",
                                "/api/productos/"
                        )
                        .hasRole("GERENTE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/productos/**"
                        )
                        .hasRole("GERENTE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/productos/**"
                        )
                        .hasRole("GERENTE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/categorias/**"
                        )
                        .hasAnyRole(
                                "CLIENTE",
                                "EMPLEADO",
                                "GERENTE"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/categorias",
                                "/api/categorias/"
                        )
                        .hasAnyRole("EMPLEADO", "GERENTE")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/categorias/**"
                        )
                        .hasAnyRole("EMPLEADO", "GERENTE")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/categorias/**"
                        )
                        .hasRole("GERENTE")

                        .anyRequest()
                        .authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
