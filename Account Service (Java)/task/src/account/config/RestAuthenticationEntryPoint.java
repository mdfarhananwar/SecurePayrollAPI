package account.config;

import account.model.Employee;
import account.model.Event;
import account.service.EmployeeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import java.util.Base64;
import java.util.Calendar;

@Component("restAuthenticationEntryPoint")
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final EmployeeService userService;

    @Autowired
    public RestAuthenticationEntryPoint(EmployeeService userService) {
        this.userService = userService;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json;charset=UTF-8");
//
//        String jsonResponse = "{\"message\": \"" + authException.getMessage() + "\"}";
//
//        PrintWriter out = response.getWriter();
//        out.write(jsonResponse);
//        out.flush();
//        if (authException instanceof AccessDeniedException) {
//            // 403 Forbidden
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            response.setContentType("application/json;charset=UTF-8");
//            String jsonResponse = "{\"message\": \"Access Denied\"}";
//            response.getWriter().write(jsonResponse);
//        }

        String email = request.getHeader("Authorization");
        if (email != null && email.toLowerCase().startsWith("basic ")) {
            String base64Credentials = email.substring("basic ".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            email = new String(credDecoded, StandardCharsets.UTF_8);
            email = email.split(":")[0];
        }
        System.out.println("Email");
        System.out.println(email);
        System.out.println("Email USER IS NULL OR NOT");
        if (email == null) {
            sendUnauthorizedResponse(response, authException.getMessage());
        }
//        System.out.println("Email found after Decoding");
//        System.out.println(email);
//        System.out.println("I am inside Login Failure Handle");
//        Employee user = userService.findByEmail(email);
//        System.out.println("checking if user is locked");
//
//        System.out.println("Inside user not null");

//        if (user == null) {
//            throw new UserNotFoundException("User not found: " + email);
//        }
//        if (authException instanceof UserNotFoundException) {
//            // Handle UserNotFoundException with a custom JSON response
//            String jsonResponses = "{\"message\": \"User not found: " + authException.getMessage() + "\"}";
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            response.setContentType("application/json;charset=UTF-8");
//            PrintWriter outs = response.getWriter();
//            outs.write(jsonResponses);
//            outs.flush();
//        }

//        if (user != null) {
//            System.out.println("Inside user not null");
//            if (user.isAccountNonLocked()) {
//                System.out.println("Inside if User is not locked");
//                System.out.println("Now Checking No. of failed attempts");
//                System.out.println(user.getFailedAttempt());
//                System.out.println(EmployeeService.MAX_FAILED_ATTEMPTS);
//                System.out.println("Above is max limit");
//
//                if (user.getFailedAttempt() < EmployeeService.MAX_FAILED_ATTEMPTS - 1) {
//                    System.out.println("Limit not exceeded");
//                    userService.increaseFailedAttempts(user);
//                    System.out.println("No login failure event for this user, capturing event");
//                    Event event = new Event(Calendar.getInstance().getTime(), "LOGIN_FAILED",
//                            user.getEmail().toLowerCase(), "/api/empl/payment", "/api/empl/payment");
//                    userService.saveEvent(event);
//                } else {
//                    System.out.println("LIMIT CROSSED");
//                    userService.lock(user);
//                    authException = new LockedException("Your account has been locked due to 5 failed attempts.");
//                }
//            }
//        }
        if (authException.getMessage().startsWith("User not found:")) {
            System.out.println("Entering USER NOT FOUND");
            String[] parts = authException.getMessage().split(": ");
            if (parts.length == 2) {
                System.out.println("Entering USER NOT FOUND length == 2");
                String username = parts[1];
                System.out.println(username);
                Event event = new Event(Calendar.getInstance().getTime(), "LOGIN_FAILED",
                        username.toLowerCase(), "/api/empl/payment", "/api/empl/payment");
                userService.saveEvent(event);
                sendUnauthorizedResponse(response, authException.getMessage());
            }
        }
        Employee user = userService.findByEmail(email);
        System.out.println(user);
        if (user != null) {
            System.out.println("Inside user is not null");
            System.out.println(user.getFailedAttempt());
            if (user.isAccountNonLocked()) {
                System.out.println("USer is Not Locked");
                if (user.getFailedAttempt() < EmployeeService.MAX_FAILED_ATTEMPTS) {
                    System.out.println("Inside user get Failed attempt");
                    userService.increaseFailedAttempts(user);
                    Event event = new Event(Calendar.getInstance().getTime(), "LOGIN_FAILED",
                            user.getEmail().toLowerCase(), "/api/empl/payment", "/api/empl/payment");
                    userService.saveEvent(event);
                    sendUnauthorizedResponse(response, authException.getMessage());
                } else if (user.getFailedAttempt() == EmployeeService.MAX_FAILED_ATTEMPTS ) {
//                    userService.increaseFailedAttempts(user);
                    Event event = new Event(Calendar.getInstance().getTime(), "LOGIN_FAILED",
                            user.getEmail().toLowerCase(), "/api/empl/payment", "/api/empl/payment");
                    userService.saveEvent(event);
                    sendUnauthorizedResponse(response, authException.getMessage());
                    Event eventBrute = new Event(Calendar.getInstance().getTime(), "BRUTE_FORCE",
                            user.getEmail().toLowerCase(), "/api/empl/payment", "/api/empl/payment");
                    userService.saveEvent(eventBrute);
                    String object = "Lock user " + user.getEmail().toLowerCase();
                    Event eventLock = new Event(Calendar.getInstance().getTime(), "LOCK_USER",
                            user.getEmail().toLowerCase(), object, "/api/empl/payment");
                    userService.saveEvent(eventLock);
                    userService.lock(user);

                }

            } else {
                System.out.println("USER IS LOCKED");

//                if (user.getFailedAttempt() > EmployeeService.MAX_FAILED_ATTEMPTS + 1 ) {
//                    Event event = new Event(Calendar.getInstance().getTime(), "LOGIN_FAILED",
//                            user.getEmail().toLowerCase(), "/api/empl/payment", "/api/empl/payment");
//                    userService.saveEvent(event);
//                }
//
//                    userService.increaseFailedAttempts(user);
                sendUnauthorizedResponseForLockUser(response, authException.getMessage(), "Unauthorized", request.getRequestURI());
            }
        }

    }
//    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
//            throws IOException, ServletException {
//        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        response.setContentType("application/json;charset=UTF-8");
//
//        String jsonResponse = "{\"message\": \"Access Denied!\"}";
//
//        PrintWriter out = response.getWriter();
//        out.write(jsonResponse);
//        out.flush();
//
//        // Your additional logic for handling access denied scenarios
//    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String errorMessage) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = "{\"message\": \"" + errorMessage + "\"}";

        PrintWriter out = response.getWriter();
        out.write(jsonResponse);
        out.flush();
    }
    private void sendUnauthorizedResponseForLockUser(HttpServletResponse response, String message, String error, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        int statusCode = response.getStatus();

        //String jsonResponse = "{\"message\": \"" + message + "\"}";
//        String jsonResponse = "{\"error\": \"" + error + "\", \"message\": \"" + message + "\", \"path\": \"" + path + "\", \"status\": \"" +statusCode + "}";
        String jsonResponse = "{\"error\": \"" + error + "\", \"message\": \"" + message + "\", \"path\": \"" + path + "\", \"status\": " + statusCode + "}";

        PrintWriter out = response.getWriter();
        out.write(jsonResponse);
        out.flush();
    }
}
