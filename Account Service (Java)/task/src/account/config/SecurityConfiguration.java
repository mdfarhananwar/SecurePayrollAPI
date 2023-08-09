package account.config;

import account.repository.UserDetailsServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
//@EnableWebSecurity
//public class SecurityConfiguration {
//
//    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
//    private final UserDetailsServiceImp userDetailsService;
//    @Autowired
//    public SecurityConfiguration(RestAuthenticationEntryPoint restAuthenticationEntryPoint, UserDetailsServiceImp userDetailsService) {
//        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
//        this.userDetailsService = userDetailsService;
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailsService)
//            throws Exception {
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//                .userDetailsService(userDetailsService)
//                .passwordEncoder(bCryptPasswordEncoder)
//                .and()
//                .build();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .httpBasic(Customizer.withDefaults())
//                .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint)) // Handle auth errors
//                .csrf(csrf -> csrf.disable()) // For Postman
//                .headers(headers -> headers.frameOptions().disable()) // For the H2 console
//                .authorizeHttpRequests(auth -> auth  // manage access
//                                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
//                                .requestMatchers("/actuator/shutdown").permitAll()
//                                .anyRequest().authenticated()
//                )
//                .sessionManagement(sessions -> sessions
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
//                );
//
//        return http.build();
//    }
//
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//
//}
@Configuration
@EnableWebSecurity
public class SecurityConfiguration  {

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }

    private final UserDetailsServiceImp userDetailsService;
    @Autowired
    public SecurityConfiguration(UserDetailsServiceImp userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .requestMatchers("/api/empl/payment").authenticated()

                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .userDetailsService(userDetailsService)
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions().disable())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

//        @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

}

