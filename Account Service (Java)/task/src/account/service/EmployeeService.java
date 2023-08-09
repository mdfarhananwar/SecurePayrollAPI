package account.service;

import account.model.Employee;
import account.model.EmployeeDTO;
import account.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmployeeService {

    private EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public ResponseEntity<EmployeeDTO> registerEmployee(Employee employee) {
        System.out.println("Enter");
        if (isNotValidEmployee(employee)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        EmployeeDTO employeeDTO = new EmployeeDTO(employee);
        return ResponseEntity.ok(employeeDTO);
    }

    public boolean isNotValidEmployee(Employee employee) {
        return employee == null ||
                employee.getEmail() == null ||
                employee.getEmail().isEmpty() ||
                !employee.getEmail().endsWith("@acme.com") ||
                employee.getName() == null ||
                employee.getName().isEmpty() ||
                employee.getLastname() == null ||
                employee.getLastname().isEmpty() ||
                employee.getPassword() == null ||
                employee.getPassword().isEmpty();

    }
    public Employee findByEmail(String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        return optionalEmployee.orElse(null);
    }


}
