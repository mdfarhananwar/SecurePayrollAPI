package account.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User not found!")
public class UserNotFoundException extends RuntimeException {
    private final String username;



    private final String path;
    public UserNotFoundException(String message, String username, String path) {
        super(message);
        this.username = username;
        this.path = path;
    }

    public String getUsername() {
        return username;
    }
    public String getPath() {
        return path;
    }
}
