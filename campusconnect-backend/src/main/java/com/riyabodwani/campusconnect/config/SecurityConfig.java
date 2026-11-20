package com.riyabodwani.campusconnect.config;

import com.riyabodwani.campusconnect.filter.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/hello").permitAll()
                .requestMatchers(HttpMethod.POST, "/students/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/students/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/recruiters/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/recruiters/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/students").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/students/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.PUT, "/students/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/students/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.POST, "/applications").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/applications/my").hasRole("STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/applications/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.GET, "/recruiter/applications", "/recruiters/applications").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.PATCH, "/recruiter/applications/*/status", "/recruiters/applications/*/status").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.GET, "/recruiter/placement-drives", "/recruiters/placement-drives").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.POST, "/recruiter/placement-drives", "/recruiters/placement-drives").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.PUT, "/recruiter/placement-drives/**", "/recruiters/placement-drives/**").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.DELETE, "/recruiter/placement-drives/**", "/recruiters/placement-drives/**").hasRole("RECRUITER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
