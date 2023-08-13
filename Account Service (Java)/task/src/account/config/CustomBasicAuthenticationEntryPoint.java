package account.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    private final CustomLoginFailureHandler loginFailureHandler;

    public CustomBasicAuthenticationEntryPoint(CustomLoginFailureHandler loginFailureHandler) {
        setRealmName("MyRealm");
        this.loginFailureHandler = loginFailureHandler;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Handle authentication failure using your CustomLoginFailureHandler
        System.out.println("CUSTOM BASIC AUTHENTICATION");
        try {
            loginFailureHandler.onAuthenticationFailure(request, response, authException);
        } catch (ServletException e) {
            e.printStackTrace();
        }
    }
}
