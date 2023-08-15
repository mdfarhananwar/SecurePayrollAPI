package account.util;

import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponseFactory {

    public static Map<String, Object> createErrorResponse() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorResponse.put("path", "/api/auth/signup");
        return errorResponse;
    }


    public static Map<String, Object> createErrorForRegisteredUser(HttpStatus status, String message, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }

    public static Map<String, Object> createUnauthorizedResponse() {
        Map<String, Object> unauthorizedResponse = new HashMap<>();
        unauthorizedResponse.put("timestamp", new Date());
        unauthorizedResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        unauthorizedResponse.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        unauthorizedResponse.put("message", "");
        unauthorizedResponse.put("path", "/api/empl/payment");
        return unauthorizedResponse;
    }
}
