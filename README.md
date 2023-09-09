
# Account Service - Payroll Management System

Companies often send out payrolls to employees using corporate mail, which can have security and usability disadvantages. In this project, we aim to improve this process by developing an API-based payroll management system using Java and the Spring Framework. This system will allow employees to access their payrolls securely through the corporate website.

## About

**Account Service (Java)** is a project focusing on core topics related to Spring Security for Java Backend Developers. The project is structured into several stages, each building upon the previous one. 
## Stages

### 1st Stage - Create the service structure (API)

#### Description

In the first stage, we will plan the architecture of our service and define the API endpoints. These endpoints include authentication, business functionality, and service functionality.

#### Objectives

- **Create and Run a Spring Boot Application on Port 28852**:
  - Set up a Spring Boot application and configure it to run on port 28852.

- **Create the POST api/auth/signup Endpoint**:
  - Implement the `POST api/auth/signup` endpoint.
  - Validate incoming data, and if the data is correct, return an HTTP OK (200) response with the user's information (excluding the password).
  - If the data is incorrect, return an HTTP Bad Request (400) response with an error message.
Here are some examples of requests and responses for the `POST api/auth/signup` endpoint:

**Example 1: Successful Registration**

- **Request**:

  ```json
  {
     "name": "John",
     "lastname": "Doe",
     "email": "johndoe@acme.com",
     "password": "secret"
  }
*
   **Response**:

  ```json
  {
     "name": "John",
     "lastname": "Doe",
     "email": "johndoe@acme.com"
  }



**Example 2: Incomplete Request Data**

- **Request**:
  ```json
  {
     "lastname": "Doe",
     "email": "johndoe@acme.com",
     "password": "secret"
  }
*
  **Response**:

  ```json
  {
     "timestamp": "<date>",
     "status": 400,
     "error": "Bad Request",
     "message": "Validation failed"
  }


### 2nd Stage - The authentication

#### Description

In the second stage, we will set up authentication for our service using Spring Security. We'll use JDBC implementations of UserDetailService for user management and add an endpoint for user registration.

### Authentication Setup

To ensure secure authentication, we will use the Spring Security module, a reliable and tested solution. Here are the steps to implement authentication:

1. **HTTP Basic Authentication**: We will provide HTTP Basic authentication for our REST service. This will be done using JDBC implementations of `UserDetailsService` for user management.

2. **User Registration Endpoint**:
   - Endpoint: `POST api/auth/signup`
   - Description: This endpoint allows users to register on our service. Users can submit their information in JSON format, including name, last name, email, and password.
   - Validation: The service will validate the provided information.
   - Response (HTTP OK - 200):
     ```json
     {
        "id": "<Long value, not empty>",   
        "name": "<String value, not empty>",
        "lastname": "<String value, not empty>",
        "email": "<String value, not empty>"
     }
     ```
   - Response (HTTP Bad Request - 400) in case of duplicate email or other errors.

3. **Testing Authentication**:
   - Endpoint: `GET api/empl/payment/`
   - Description: This endpoint will be available only to authenticated users. It will return a response in JSON format representing the user who sent the request.
   - Response (HTTP OK - 200):
     ```json
     {
        "id": "<Long value, not empty>",   
        "name": "<String value, not empty>",
        "lastname": "<String value, not empty>",
        "email": "<String value, not empty>"
     }
     ```
   - Response (HTTP Unauthorized - 401) in case of non-authenticated or wrong user.

4. **Database Configuration**:
   - We will use an H2 database for persistence.
   - Configure the application.properties file with `spring.datasource.url=jdbc:h2:file:../service_db` for database URL.
   
5. **Customization**:
   - As we are implementing a REST architecture without sessions, configure HTTP Basic authentication and handle unauthorized access attempts.
   - Configure access for the API using `HttpSecurity` object.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .httpBasic(Customizer.withDefaults())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/signup").permitAll()
                    // Add other matchers as needed
            )
            .sessionManagement(sessions -> sessions
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

    return http.build();
}
```
#### Objectives

- Implement HTTP Basic authentication.
- Create the `GET api/empl/payment` endpoint, accessible only for authenticated users.
- Use an H2 database for user persistence.
- Update the POST api/auth/signup endpoint to handle user registration and authentication.

##### Examples
Here are some examples of requests and responses for the POST api/auth/signup and GET api/empl/payment/ endpoints:

User Registration (POST api/auth/signup)
**Example 1: Successful Registration**

- **Request**:
```json
{
   "name": "John",
   "lastname": "Doe",
   "email": "JohnDoe@acme.com",
   "password": "secret"
}
```
- **Response(HTTP OK - 200)**:
```json
{
   "id": 1,
   "name": "John",
   "lastname": "Doe",
   "email": "JohnDoe@acme.com"
}
```
**Example 2: Duplicate Email**

- **Request**:
```json
{
   "name": "John",
   "lastname": "Doe",
   "email": "johndoe@acme.com",
   "password": "secret"
}
```

- **Response (HTTP Bad Request - 400)**:
```json
{
    "timestamp": "<data>",
    "status": 400,
    "error": "Bad Request",
    "message": "User exist!",
    "path": "/api/auth/signup"
}
```

Testing Authentication (GET api/empl/payment/)

**Example 1: Successful Authentication**

- **Response (HTTP OK - 200)**:
```json
{
    "id": 1,
    "name": "John",
    "lastname": "Doe",
    "email": "JohnDoe@acme.com"
}
```

**Example 2: Unauthorized Access**

- **Response (HTTP Unauthorized - 401)**:
```json
{
    "timestamp": "<data>"
}
```



### 3rd Stage - Security first!

#### Description

The third stage focuses on enhancing the security of our authentication process based on OWASP recommendations. We'll implement password security requirements and allow users to change their passwords.

## Password Security Requirements

Ensure the following password security requirements are met during user registration and password change:

- Passwords contain at least 12 characters; if a password fails this check, respond with a 400 Bad Request and the following JSON body:

  ```json
  {
      "timestamp": "<data>",
      "status": 400,
      "error": "Bad Request",
      "message": "The password length must be at least 12 chars!",
      "path": "<api>"
  }
  ```

  - Store passwords securely to resist offline attacks. Use `BCryptPasswordEncoder` with a strength of at least 13 to store passwords in the database.

### Breached Password Check

- Check submitted passwords against a set of breached passwords. If the password is in the list of breached passwords, respond with a 400 Bad Request and the following JSON body:

```json
{
    "timestamp": "<data>",
    "status": 400,
    "error": "Bad Request",
    "message": "The password is in the hacker's database!",
    "path": "<api>"
}
```
Password Change Endpoint
Implement the POST api/auth/changepass endpoint for changing passwords. The API must be available for authenticated users and accept data in the JSON format:

```json
{
   "new_password": "<String value, not empty>"
}
```


If successful, respond with HTTP OK status (200) and the body like this:
```json
{
   "email": "<String value, not empty>",
   "status": "The password has been updated successfully"
}
```

Update the password for the current user in the database. If the new password fails security checks, respond accordingly, as stated above. If a new password is the same as the current password, respond with 400 Bad Request and the following JSON body:
```json
{
    "timestamp": "<data>",
    "status": 400,
    "error": "Bad Request",
    "message": "The passwords must be different!",
    "path": "<api>"
}
```

#### Objectives

- Implement the `POST api/auth/changepass` endpoint for changing user passwords.
- Enforce password requirements, including length and checking against breached passwords.
- Securely store passwords using BCrypt with an appropriate work factor.


**Examples**
Here are some examples of requests and responses for the password security measures and the POST api/auth/changepass endpoint:

**Example 1**: A POST request for api/auth/signup
- **Request body**:
```json
{
   "name": "John",
   "lastname": "Doe",
   "email": "johndoe@acme.com",
   "password": "secret"
}
```

- **Response(400 Bad Request)**:
```json
{
    "timestamp": "<data>",
    "status": 400,
    "error": "Bad Request",
    "message": "The password length must be at least 12 chars!",
    "path": "/api/auth/signup"
}
```

Ensure your authentication service follows these security measures to protect user passwords effectively.


### 4th Stage - Attention to business

#### Description

In the fourth stage, we begin implementing the business functions of our application, which involve managing employee salaries. This includes uploading and updating payroll information.
# Employee Salary Management Service

## Description

It's time to start with the business functions of our application! Our service provides users with information about employee salaries for a selected period. Additionally, accountants can upload salary information for employees. The following endpoints are available:

- **POST api/acct/payments**: Uploads payrolls.
- **PUT api/acct/payments**: Changes the salary of a specific user.
- **GET api/empl/payment**: Provides access to the payroll of an employee.

Salary information is transmitted as an array of JSON objects. This operation must be transactional. That is, if an error occurs during an update, perform a rollback to the original state. The following requirements are imposed on the data:

- An employee must be among the users of our service.
- The period for which the salary is paid must be unique for each employee (for POST).
- Salary is calculated in cents and cannot be negative.


#### Objectives

1. **POST api/acct/payments**:
   - Available to unauthorized users.
   - Accepts data in JSON format for uploading salary information.
   - Performs a transactional operation.
 
     ```json
     [
         {
             "employee": "<user email>",
             "period": "<mm-YYYY>",
             "salary": <Long>
         },
         {
             "employee": "<user1 email>",
             "period": "<mm-YYYY>",
             "salary": <Long>
         },
         ...
     ]
     ```
   
   - Successful response:
   
     ```json
     {
        "status": "Added successfully!"
     }
     ```
   
   - Error response (HTTP Bad Request - 400):
   
     ```json
     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "<error message>",
         "path": "/api/acct/payments"
     }
     ```

2. **PUT api/acct/payments**:
   - Available to unauthorized users.
   - Accepts data in JSON format to update an employee's salary.
   
     ```json
     {
         "employee": "<user email>",
         "period": "<mm-YYYY>",
         "salary": <Long>
     }
     ```
   
   - Successful response:
   
     ```json
     {
        "status": "Updated successfully!"
     }
     ```
   
   - Error response (HTTP Bad Request - 400):
   
     ```json
     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "<error message>",
         "path": "/api/acct/payments"
     }
     ```

3. **GET api/empl/payment**:
   - Available only to authenticated users.
   - Accepts the `period` parameter to specify the period (month and year).
   - Provides salary information based on the specified period.
   - If `period` is not specified, it returns salary information for all periods in descending order.
   - The response format:

     ```json
     {
        "name": "<user name>",
        "lastname": "<user lastname>",
        "period": "<name of month-YYYY>",
        "salary": "X dollar(s) Y cent(s)"
     }
     ```

   - Error response for unauthorized or incorrect users (HTTP Unauthorized - 401):
   
     ```json
     {
         "timestamp": "<date>",
         "status": 401,
         "error": "Unauthorized",
         "message": "<error message>",
         "path": "api/empl/payment"
     }
     ```

   - Error response for incorrect `period` format (HTTP Bad Request - 400):
   
     ```json
     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "<error message>",
         "path": "api/empl/payment"
     }
     ```



### 5th Stage - The authorization

#### Description

The fifth stage focuses on authorization and role management. We'll implement role-based access control for our service, dividing users into administrative and business groups.

The ACME Security Department has imposed new security requirements based on the ASVS (Application Security Verification Standard). Your task is to implement the following access control verification requirements:

1. Verify that all user and data attributes and policy information used by access controls cannot be manipulated by end-users unless specifically authorized.
2. Verify the principle of least privilege, where users can only access resources for which they possess specific authorization.
3. Verify the principle of deny by default, meaning new users/roles start with minimal or no permissions and only gain access to features explicitly assigned.

To implement these requirements, we need to define roles and create service endpoints to manage user roles:

- **PUT api/admin/user/role**: Sets user roles.
- **DELETE api/admin/user**: Deletes users.
- **GET api/admin/user**: Obtains information about all users.
- **POST api/auth/signup**: Modified response with user roles.

Roles are divided into two groups: administrative (Administrator) and business users (Accountant, User). Users can belong to either the administrative or business group, but not both.

#### Objectives

1. **Authorization**:
   - Implement authorization based on roles.
   - First registered user receives the Administrator role, others receive User roles.
   - Respond with HTTP Forbidden status (403) for unauthorized access.

   - Forbidden response body:
     ```json
     {
       "timestamp": "<date>",
       "status": 403,
       "error": "Forbidden",
       "message": "Access Denied!",
       "path": "/api/admin/user/role"
     }
     ```

2. **Modified Response**:
   - Change the response for the **POST api/auth/signup** endpoint.
   - Respond with HTTP OK status (200) and a JSON object containing user information and roles in ascending order.

   - Modified response body:
     ```json
     {
        "id": "<Long value, not empty>",
        "name": "<String value, not empty>",
        "lastname": "<String value, not empty>",
        "email": "<String value, not empty>",
        "roles": "<[User roles]>"
     }
     ```

3. **GET api/admin/user**:
   - Respond with an array of user objects sorted by ID in ascending order.
   - Return an empty JSON array if no information is available.

   - Example response:
     ```json
     [
         {
             "id": "<user1 id>",
             "name": "<user1 name>",
             "lastname": "<user1 last name>",
             "email": "<user1 email>",
             "roles": "<[user1 roles]>"
         },
         ...
     ]
     ```

4. **DELETE api/admin/user/{user email}**:
   - Delete the specified user.
   - Respond with HTTP OK status (200) and a success message.
   - If the user is not found, respond with HTTP Not Found status (404) and an error message.
   - If the Administrator tries to delete themselves, respond with HTTP Bad Request status (400) and an error message.

   - Success response body:
     ```json
     {
        "user": "<user email>",
        "status": "Deleted successfully!"
     }
     ```

   - Not Found error response body:
     ```json
     {
         "timestamp": "<date>",
         "status": 404,
         "error": "Not Found",
         "message": "User not found!",
         "path": "<api + parameter>"
     }
     ```

   - Bad Request error response body:
     ```json
     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "Can't remove ADMINISTRATOR role!",
         "path": "<api + path>"
     }
     ```

5. **PUT api/admin/user/role**:
   - Change user roles based on the provided JSON body.
   - Respond with the updated user information and roles.
   - Handle various error scenarios, such as user not found, role not found, attempting to remove the only existing role, and attempting to remove the Administrator role.

   - Example JSON body:
     ```json
     {
        "user": "<String value, not empty>",
        "role": "<User role>",
        "operation": "<[GRANT, REMOVE]>"
     }
     ```

   - Successful response body:
     ```json
     {
        "id": "<Long value, not empty>",
        "name": "<String value, not empty>",
        "lastname": "<String value, not empty>",
        "email": "<String value, not empty>",
        "roles": "[<User roles>]"
     }
     ```

   - Error response examples (User not found, Role not found, Removing the only role, Removing Administrator role, Combining administrative and business roles):
     ```json
     {
         "timestamp": "<date>",
         "status": 404,
         "error": "Not Found",
         "message": "User not found!",
         "path": "/api/admin/user/role"
     }
     
     {
         "timestamp": "<date>",
         "status": 404,
         "error": "Not Found",
         "message": "Role not found!",
         "path": "/api/admin/user/role"
     }

     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "The user does not have a role!",
         "path": "/api/admin/user/role"
     }

     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "The user must have at least one role!",
         "path": "/api/admin/user/role"
     }

     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "Can't remove ADMINISTRATOR role!",
         "path": "/api/admin/user/role"
     }

     {
         "timestamp": "<date>",
         "status": 400,
         "error": "Bad Request",
         "message": "The user cannot combine administrative and business roles!",
         "path": "/api/admin/user/role"
     }
     ```


### 6th Stage - Logging events

#### Description

In the sixth stage, we enhance security by implementing event logging and incident detection. We'll log security events and detect brute-force attacks.

The security event fields consist of the following information:

- **Date**: Date and time of the event.
- **Action**: Event name from the provided table.
- **Subject**: The user who performed the action (or "Anonymous" if the user is not determined).
- **Object**: The object on which the action was performed.
- **Path**: The API endpoint related to the event.

Events are crucial for identifying incidents, and they are collected for use in Security Information and Event Management systems (SIEM).

Additionally, we introduce a new role called "Auditor" who is responsible for analyzing security events and identifying incidents. The Auditor is part of the business group and has access to all security events.

To detect brute force attacks, we implement a simple rule: if there are more than 5 consecutive failed login attempts, we log the events LOGIN_FAILED -> BRUTE_FORCE -> LOCK_USER and block the user.

#### Objectives

1. **Logging Security Events**:
   - Implement logging of security events in the application as described in the provided table.
   - Respond with HTTP Forbidden status (403) for unauthorized access attempts.
   - Use "Anonymous" as the subject for unidentified users.
   - Store security events persistently, such as in a database.

2. **Brute Force Attack Detection**:
   - Implement a mechanism to block users after 5 consecutive failed login attempts.
   - Log events for failed logins, brute force attacks, and user locks.
   - Reset the failed login attempt counter after a successful login.

3. **User Lock/Unlock**:
   - Add the **PUT api/admin/user/access** endpoint to lock/unlock users.
   - Accept a JSON body to determine whether the user will be locked or unlocked.
   - Respond with the appropriate success or error messages.
   - Ensure the Administrator cannot be locked for safety reasons.

4. **Auditor Role**:
   - Introduce the "Auditor" role in the role model.
   - The Auditor is part of the business group and has access to all security events.

5. **GET api/security/events**:
   - Implement the endpoint to retrieve an array of security events sorted by ID in ascending order.
   - Return an empty JSON array if no events are found.

## Role Model Update

Update the role model as follows:

- **Anonymous**: Can access public endpoints.
- **User**: Can change passwords, access employee payment information.
- **Accountant**: Can manage salary payments.
- **Administrator**: Can manage users and roles, cannot be locked.
- **Auditor**: Part of the business group, has access to all security events.

## Examples

Here are some example scenarios and their corresponding responses:

### Example 1: GET Request for api/auth/signup under the Auditor role

- **Request**: GET api/auth/signup (Auditor role)
- **Response**: 200 OK

```json
[
  {
    "id": 1,
    "date": "<date>",
    "action": "CREATE_USER",
    "subject": "Anonymous",
    "object": "johndoe@acme.com",
    "path": "/api/auth/signup"
  },
  {
    "id": 6,
    "date": "<date>",
    "action": "LOGIN_FAILED",
    "subject": "maxmustermann@acme.com",
    "object": "/api/empl/payment",
    "path": "/api/empl/payment"
  },
  {
    "id": 9,
    "date": "<date>",
    "action": "GRANT_ROLE",
    "subject": "johndoe@acme.com",
    "object": "Grant role ACCOUNTANT to petrpetrov@acme.com",
    "path": "/api/admin/user/role"
  },
  {
    "id": 10,
    "date": "<date>",
    "action": "REMOVE_ROLE",
    "subject": "johndoe@acme.com",
    "object": "Remove role ACCOUNTANT from petrpetrov@acme.com",
    "path": "/api/admin/user/role"
  },
  {
    "id": 11,
    "date": "<date>",
    "action": "DELETE_USER",
    "subject": "johndoe@acme.com",
    "object": "petrpetrov@acme.com",
    "path": "/api/admin/user"
  },
  {
    "id": 12,
    "date": "<date>",
    "action": "CHANGE_PASSWORD",
    "subject": "johndoe@acme.com",
    "object": "johndoe@acme.com",
    "path": "/api/auth/changepass"
  },
  {
    "id": 16,
    "date": "<date>",
    "action": "ACCESS_DENIED",
    "subject": "johndoe@acme.com",
    "object": "/api/acct/payments",
    "path": "/api/acct/payments"
  },
  {
    "id": 25,
    "date": "<date>",
    "action": "BRUTE_FORCE",
    "subject": "maxmustermann@acme.com",
    "object": "/api/empl/payment",
    "path": "/api/empl/payment"
  },
  {
    "id": 26,
    "date": "<date>",
    "action": "LOCK_USER",
    "subject": "maxmustermann@acme.com",
    "object": "Lock user maxmustermann@acme.com",
    "path": "/api/empl/payment"
  },
  {
    "id": 27,
    "date": "<date>",
    "action": "UNLOCK_USER",
    "subject": "johndoe@acme.com",
    "object": "Unlock user maxmustermann@acme.com",
    "path": "/api/admin/user/access"
  }
]
```

### Example 2: POST Request for /api/admin/user/role

- **Request**:
```json
{
  "user": "administrator@acme.com",
  "role": "AUDITOR",
  "operation": "GRANT"
}
```
- **Response(400 Bad Request)**:
```json
{
  "timestamp": "<date>",
  "status": 400,
  "error": "Bad Request",
  "message": "The user cannot combine administrative and business roles!",
  "path": "/api/admin/user/role"
}
```

### Example 3: PUT Request for PUT api/admin/user/access

- **Request**:
```json
{
  "user": "administrator@acme.com",
  "operation": "LOCK"
}
```
- **Response(400 Bad Request)**:
```json
{
  "timestamp": "<date>",
  "status": 400,
  "error": "Bad Request",
  "message": "Can't lock the ADMINISTRATOR!",
  "path
}
```



### 7th Stage - Securing connection

#### Description

The final stage ensures the overall security of our service by implementing HTTPS for secure communication. We'll generate a self-signed certificate and add HTTPS support to our Spring Boot application.

#### Objectives

- Generate a self-signed certificate with CN=accountant_service.
- Enable HTTPS support in the application using the generated certificate.

## Getting Started

To get started with this project, follow the instructions provided in each stage's documentation. Ensure you have the necessary prerequisites, such as Java, Spring Boot, and any required libraries.

