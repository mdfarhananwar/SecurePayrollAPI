package account.config;

import account.model.Employee;
import account.model.Event;
import account.service.EmployeeService;
import account.service.LoginAttemptService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;

@Component
public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final EmployeeService userService;


//    @Autowired
//    public CustomLoginFailureHandler(EmployeeService userService, LoginAttemptService loginAttemptService, MessageSource messages) {
//        this.userService = userService;
//        this.loginAttemptService = loginAttemptService;
//        this.messages = messages;
//    }


    private HttpServletRequest request;
    private final LoginAttemptService loginAttemptService;
    private final MessageSource messages;


//    private final LocaleResolver localeResolver;
    @Autowired
    public CustomLoginFailureHandler(EmployeeService userService, HttpServletRequest request, LoginAttemptService loginAttemptService, MessageSource messages) {
        this.userService = userService;
        this.request = request;
        this.loginAttemptService = loginAttemptService;
        this.messages = messages;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        System.out.println(request.getRequestURI());
        System.out.println(request.getContextPath());
        System.out.println(request.getServletPath());
        String email = request.getHeader("Authorization");
        if (email != null && email.toLowerCase().startsWith("basic ")) {
            String base64Credentials = email.substring("basic ".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            email = new String(credDecoded, StandardCharsets.UTF_8);
            email = email.split(":")[0];
        }
        System.out.println("Email found after Decoding");
        System.out.println(email);
        System.out.println("I am inside Login Failure Handle");
        Employee user = userService.findByEmail(email);

        System.out.println(user.toString());
        System.out.println(user.isAccountNonLocked());
        System.out.println("checking if user is locked");
        if (user != null) {
            System.out.println("Inside user not null");
            if (user.isAccountNonLocked()) {
                System.out.println("Inside if User is locked");
                System.out.println("Now Chceking No. of failed attempts");
                System.out.println(user.getFailedAttempt());
                System.out.println( EmployeeService.MAX_FAILED_ATTEMPTS);
                System.out.println("Above is max limit");
                if (user.getFailedAttempt() < EmployeeService.MAX_FAILED_ATTEMPTS - 1) {
                    System.out.println("Limit not exceeded");
                    userService.increaseFailedAttempts(user);
                    Event event = new Event( Calendar.getInstance().getTime(),"LOGIN_FAILED",
                            user.getEmail().toLowerCase(),"/api/empl/payment","/api/empl/payment");
                    userService.saveEvent(event);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Bad credentials");
                } else {
                    System.out.println("LIMIT CROSSED");
                    userService.lock(user);
                    exception = new LockedException("Your account has been locked due to 5 failed attempts.");
                }
            }

        }
        System.out.println("I am inside Login Failure Handle");
        //Locale locale = LocaleContextHolder.getLocale();
        //System.out.println("Locale: " + locale);  // Debug line
//        String errorMessage = messages.getMessage("message.badCredentials", null, locale);
//        String errorMessage = messages.getMessage("message.badCredentials", null, Locale.getDefault());
//
//        System.out.println("Error Message: " + errorMessage);  // Debug line
//
//        if (loginAttemptService.isBlocked()) {
//            errorMessage = messages.getMessage("auth.message.blocked", null, Locale.getDefault());
//        }
//
//        if (exception.getMessage().equalsIgnoreCase("blocked")) {
//            errorMessage = messages.getMessage("auth.message.blocked", null, Locale.getDefault());
//        }
//        super.onAuthenticationFailure(request, response, exception);
    }
}

