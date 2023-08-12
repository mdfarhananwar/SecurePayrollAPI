package account.repository;

import account.model.Payrolls;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRepository extends JpaRepository<Payrolls, Long> {
    boolean existsByEmployee(String employee);
    Payrolls findByEmployeeAndPeriod(String employee, String period);

    List<Payrolls> findByEmployee(String email);

    boolean existsByEmployeeAndPeriod(String email, String period);
}
