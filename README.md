# SafeSip

SafeSip is a Java desktop application that manages alcohol sales based on consumption tracking. It estimates each customer's Blood Alcohol Content (BAC) from their recent drinks and blocks a sale if the projected BAC reaches the safe limit (0.08%).

Mini Project - I (23CS354)

## Tools Used
- JDK 17
- NetBeans IDE
- MySQL (backend database)
- Java Swing (UI) and JDBC (database connectivity)

## Features
- Role-based login (Admin, Staff, Customer)
- Product and stock listing
- Customers log their consumption and see their estimated BAC
- Staff process sales with an automatic BAC check
- Sales, sale items and inventory stored in MySQL

## Project Structure
```
SafeSip/
  src/safesip/db/DBConnection.java       JDBC connection
  src/safesip/logic/BACCalculator.java   Widmark BAC estimation
  src/safesip/ui/LoginFrame.java         Login screen (run this)
  src/safesip/ui/MainFrame.java          Products, consumption and sale screens
  database/safesip.sql                   Schema and sample data
  screenshots/                           Application screenshots
```

## How to Run
1. Install JDK, NetBeans and MySQL.
2. Run `database/safesip.sql` in MySQL Workbench to create the `safesip` database.
3. Create a NetBeans Java project, copy the `src` files in, and add the MySQL Connector/J JAR under Libraries.
4. Set your MySQL password in `DBConnection.java`.
5. Run `LoginFrame.java`.

## Sample Logins
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Staff | staff1 | staff123 |
| Customer | ravi | ravi123 |

## Screenshots
Add your screenshots to the `screenshots/` folder and list them here.

## Note
The BAC value is an estimate for academic purposes only and is not a medical tool.
