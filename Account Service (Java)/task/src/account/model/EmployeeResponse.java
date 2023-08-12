package account.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class EmployeeResponse {

    private String name;
    private String lastname;
    private String period;
    private String salary;

    public EmployeeResponse() {
    }

    public EmployeeResponse(String name, String lastname, String period, String salary) {
        this.name = name;
        this.lastname = lastname;
        this.period = period;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String convertToNameOfMonthFormat(String inputDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM-yyyy");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy");
        YearMonth yearMonth = YearMonth.parse(inputDate, inputFormatter);
        LocalDate localDate = yearMonth.atDay(1); // Get the first day of the month
        return localDate.format(outputFormatter);
    }

    public String convertToFormattedString(long salary) {
        long dollars = salary / 100; // Extract dollars portion
        long cents = salary % 100;   // Extract cents portion

        String dollarsText = dollars + " dollar(s)";
        String centsText = cents + " cent(s)";

        // Combine dollars and cents portions
        String formattedSalary = dollarsText + " " + centsText;
        return formattedSalary;
    }
}
