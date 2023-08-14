package account.config;

import account.model.Event;
import account.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Calendar;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    private EmployeeService userService;
    @Autowired
    public CustomExceptionHandler(EmployeeService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        String errorMessage = "User not found: " + ex.getMessage();
        String path = ex.getPath();
        String email = ex.getUsername();
        Event event = new Event(Calendar.getInstance().getTime(), "LOGIN_FAILED",
                email, path, path);
        userService.saveEvent(event);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }
}
