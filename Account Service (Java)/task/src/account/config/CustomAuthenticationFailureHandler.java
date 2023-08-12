package account.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationFailureHandler
        implements AuthenticationFailureHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
    private String customErrorMessage;
    public CustomAuthenticationFailureHandler(String customErrorMessage) {
        this.customErrorMessage = customErrorMessage;
    }
//
//    @Override
//    public void onAuthenticationFailure(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            AuthenticationException exception)
//            throws IOException, ServletException {
//
//        response.setStatus(HttpStatus.UNAUTHORIZED.value());
//        Map<String, Object> data = new HashMap<>();
//        data.put(
//                "timestamp",
//                Calendar.getInstance().getTime());
//        data.put(
//                "exception",
//                exception.getMessage());
//
//        response.getOutputStream()
//                .println(objectMapper.writeValueAsString(data));
//    }
private void sendErrorResponse(HttpServletResponse response, String errorMessage, int httpStatus) throws IOException {
    response.setStatus(httpStatus);
    Map<String, Object> data = new HashMap<>();
    data.put("timestamp", Calendar.getInstance().getTime());
    data.put("message", errorMessage);

    response.getOutputStream().println(objectMapper.writeValueAsString(data));
}

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        sendErrorResponse(response, customErrorMessage, HttpStatus.UNAUTHORIZED.value());
    }


    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        sendErrorResponse(response, "Access Denied!", HttpStatus.FORBIDDEN.value());
    }
}
