package account.config;

import account.repository.UserDetailsServiceImp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;


@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserDetailsServiceImp userDetailsService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    @Autowired
    private CustomLoginFailureHandler loginFailureHandler;


    private CustomBasicAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    public SecurityConfiguration(UserDetailsServiceImp userDetailsService, CustomAccessDeniedHandler customAccessDeniedHandler, CustomBasicAuthenticationEntryPoint authenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailsService)
//            throws Exception {
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//                .userDetailsService(userDetailsService)
//                .passwordEncoder(bCryptPasswordEncoder)
//                .and()
//                .build();
//    }
@Bean
public CustomAuthenticationFailureHandler accessDeniedHandler() {
    return new CustomAuthenticationFailureHandler("Access Denied!");
}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .requestMatchers("/api/acct/payments").hasRole("ACCOUNTANT")
                        .requestMatchers("/api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
                        .requestMatchers("/api/auth/changepass").hasAnyRole("ADMINISTRATOR", "USER", "ACCOUNTANT")
                        .requestMatchers("/api/admin/user/**").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/security/events/").hasRole("AUDITOR")
                        .anyRequest().authenticated()

                )
//                .exceptionHandling(exceptionHandling -> exceptionHandling
//                        .accessDeniedHandler(customAccessDeniedHandler)
//                )
//                .formLogin(formLogin -> formLogin
//                        .failureHandler(loginFailureHandler())
//                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(customAccessDeniedHandler)

                )
//                .formLogin(formLogin -> formLogin
//                        .failureHandler(loginFailureHandler)
//                        .successHandler(loginSuccessHandler)
//                )
//                .addFilterBefore(customBasicAuthenticationFilter(), BasicAuthenticationFilter.class) // Add custom filter
//                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .userDetailsService(userDetailsService)
                .csrf(AbstractHttpConfigurer::disable)

                .headers(headers -> headers.frameOptions().disable())
                .httpBasic(httpBasicConfigurer -> httpBasicConfigurer
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
//
//    @Bean
//    public CustomAuthenticationFailureHandler authenticationFailureHandler() {
//        String customErrorMessage = "Authentication failed. Please check your credentials.";
//        return new CustomAuthenticationFailureHandler(customErrorMessage);
//    }
    @Bean
    public LocaleResolver localeResolver() {
        return new AcceptHeaderLocaleResolver();
    }
//    @Bean
//    public MessageSource messageSource() {
//        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
//        messageSource.setBasename("messages"); // Without .properties extension
//        messageSource.setDefaultEncoding("UTF-8");
//        return messageSource;
//    }
//    @Bean
//    public CustomBasicAuthenticationFilter customBasicAuthenticationFilter() throws Exception {
//        CustomBasicAuthenticationFilter filter = new CustomBasicAuthenticationFilter(authenticationManager(), customAuthenticationFailureHandler());
//        filter.setAuthenticationEntryPoint(new BasicAuthenticationEntryPoint()); // Set custom entry point if needed
//        return filter;
//    }
//    @Bean
//    public CustomAuthenticationFailureHandler customAuthenticationFailureHandler() {
//        return new CustomAuthenticationFailureHandler();
//    }




}

