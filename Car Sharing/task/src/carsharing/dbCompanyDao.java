package carsharing;

import org.h2.jdbcx.JdbcDataSource;

import javax.xml.transform.Result;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dbCompanyDao implements CompanyDao {

    private static final String CREATE_COMPANY_TABLE = "CREATE TABLE IF NOT EXISTS COMPANY (ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR UNIQUE NOT NULL);";
    private static final String CREATE_CAR_TABLE = "CREATE TABLE IF NOT EXISTS CAR (ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR UNIQUE NOT NULL, COMPANY_ID INT NOT NULL, CONSTRAINT COMPANY_ID FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID));";
    private static final String CREATE_CUSTOMER_TABLE = "CREATE TABLE IF NOT EXISTS CUSTOMER (ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR UNIQUE NOT NULL, RENTED_CAR_ID INT, CONSTRAINT RENTED_CAR_ID FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID));";
    private static final String SELECT_ALL = "SELECT * FROM %s";
    private static final String SELECT_ALL_CARS = "SELECT * FROM CAR WHERE COMPANY_ID = %d;";
    private static final String SELECT_COMPANY = "SELECT * FROM COMPANY WHERE ID = %d;";
    private static final String SELECT_CAR = "SELECT * FROM CAR WHERE ID = %d";
    private static final String SELECT_CAR_OF_COMPANY = "SELECT CAR.ID AS CAR_ID, CAR.NAME AS CAR_NAME, COMPANY.NAME AS COMPANY_NAME FROM CAR LEFT JOIN COMPANY ON CAR.COMPANY_ID = COMPANY.ID WHERE COMPANY_ID = %d;";
    private static final String SELECT_CUSTOMER_CHOSEN_CAR = "SELECT CUSTOMER.ID AS CUSTOMER_ID, CUSTOMER.NAME AS CUSTOMER_NAME, CAR.ID AS CAR_ID, CAR.NAME AS CAR_NAME, COMPANY.ID AS COMPANY_ID, COMPANY.NAME AS COMPANY_NAME FROM CUSTOMER LEFT JOIN CAR ON CAR.ID = CUSTOMER.RENTED_CAR_ID LEFT JOIN COMPANY ON CAR.COMPANY_ID = COMPANY.ID WHERE CUSTOMER.ID = %d;";
    private static final String ADD_COMPANY = "INSERT INTO COMPANY (NAME) VALUES ('%s');";
    private static final String ADD_CAR = "INSERT INTO CAR (NAME, COMPANY_ID) VALUES ('%s', '%d')";
    private static final String ADD_CUSTOMER = "INSERT INTO CUSTOMER (NAME) VALUES ('%s');";
    private static final String SELECT_AVAILABLE_CARS_OF_A_COMPANY = "SELECT CAR.ID, CAR.NAME, CUSTOMER.RENTED_CAR_ID FROM CAR LEFT JOIN CUSTOMER ON CAR.ID = CUSTOMER.RENTED_CAR_ID WHERE CUSTOMER.RENTED_CAR_ID IS NULL AND CAR.COMPANY_ID = %d ;";
    private static final String CUSTOMER_RENTS_CAR = "UPDATE CUSTOMER SET RENTED_CAR_ID = %d WHERE ID = %d;";
    private static final String CUSTOMER_RETURNS_CAR = "UPDATE CUSTOMER SET RENTED_CAR_ID = NULL WHERE ID = %d;";
    private static final String DELETE_ALL_CUSTOMERS = "DELETE FROM CUSTOMER; ALTER TABLE CUSTOMER ALTER COLUMN ID RESTART WITH 1;";
    private static final String DELETE_ALL_CARS = "DELETE FROM CAR; ALTER TABLE CAR ALTER COLUMN ID RESTART WITH 1;";


    private final dbClient dbClient;

    public dbCompanyDao(String databaseName){
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:file:../task/src/carsharing/db/" + databaseName);

        dbClient = new dbClient(dataSource);
        dbClient.run(CREATE_COMPANY_TABLE);
        dbClient.run(CREATE_CAR_TABLE);
        dbClient.run(CREATE_CUSTOMER_TABLE);

    }

    public List<Company> listCompanies() {
        List<Company> companyList = dbClient.selectForList(String.format(SELECT_ALL, "company"), new CompanyMapper());
        if(companyList.isEmpty()){
            return null;
        }
        else{
            return companyList;
        }
    }


    public List<Car> listCars(int companyId) {
        List<Car> carList = dbClient.selectForList(String.format(SELECT_ALL_CARS, companyId ), new CarMapper());
        if(carList.isEmpty()){
            return null;
        }
        else{
            return carList;
        }
    }

    public List<Customer> listCustomers() {
        List<Customer> customerList = dbClient.selectForList(String.format(SELECT_ALL, "CUSTOMER"), new CustomerMapper());
        if(customerList.isEmpty()){
            return null;
        }
        else{
            return customerList;
        }
    }

    public List<Map<String, Object>> lisAllCars() throws SQLException {
        return dbClient.extractData(String.format(SELECT_ALL, "CAR"));
    }

    public Company companyDetails (int companyId) {
        return dbClient.select(String.format(SELECT_COMPANY, companyId), new CompanyMapper());
    }

    public Map<String, Object> customerRentedCar(int customerId) throws SQLException {
       List<Map<String, Object>> rentedCar = dbClient.extractData(String.format(SELECT_CUSTOMER_CHOSEN_CAR, customerId));
       if(rentedCar != null && !rentedCar.isEmpty()){
           return rentedCar.get(0);

       }
       else {
           System.out.println("There was a problem");
           return null;
       }
    }

    public List<Car> companyAvailableCars (int companyId) {
        return dbClient.selectForList(String.format(SELECT_AVAILABLE_CARS_OF_A_COMPANY, companyId), new CarMapper());
    }

    public List<String> customerRentsCar (int pseudoCarId, int customerId, int companyId) throws SQLException {

        List<Map<String, Object>> carListOfCompany = dbClient.extractData(String.format(SELECT_CAR_OF_COMPANY, companyId));
        List<String> carDetails = new ArrayList<>();

        carDetails.add((String) carListOfCompany.get(pseudoCarId - 1).get("CAR_NAME"));
        carDetails.add((String) carListOfCompany.get(pseudoCarId - 1).get("COMPANY_NAME"));

        int realCarId = (Integer) carListOfCompany.get(pseudoCarId - 1).get("CAR_ID");

        dbClient.run(String.format(CUSTOMER_RENTS_CAR, realCarId, customerId));


        return carDetails;

    }

    public boolean customerReturnsCar (int customerId) {
        return dbClient.run(String.format(CUSTOMER_RETURNS_CAR, customerId));
    }


    @Override
    public void createCompany(String companyName) {
        Company company = new Company(companyName);
         boolean status = dbClient.run(String.format(ADD_COMPANY, company));
         if(status){
             System.out.println("The company was created!");
         }
         else{
             System.out.println("There was a problem");
         }
    }

    public void createCar(String carName, int companyId){
        Car car = new Car(carName);
        boolean status = dbClient.run(String.format(ADD_CAR, car, companyId));
        if(status){
            System.out.println("The car was added!");
        }
        else{
            System.out.println("There was a problem");
        }
    }

    public void createCustomer(String customerName) {
        List<Customer> customers = listCustomers();
        Customer customer = new Customer(customerName);
        if(customerName.isEmpty()){
            System.out.println("Customer name cannot be empty");
        }
        else if (customers != null && customers.contains(customer)){
            System.out.println("Customer name is already taken!");
        }
        else {
            boolean status = dbClient.run(String.format(ADD_CUSTOMER, customerName));
            if (status) {
                System.out.println("The customer was added!");
            } else {
                System.out.println("There was a problem");
            }
        }
    }

    public void deleteAllCustomers() {
        boolean status = dbClient.run(DELETE_ALL_CUSTOMERS);

        if(status){
            System.out.println("All customers were deleted");
        }
        else{
            System.out.println("There was a problem");
        }
    }

    public void deleteAllCars() {
        boolean status = dbClient.run(DELETE_ALL_CARS);

        if(status){
            System.out.println("All cars were deleted");
        }
        else{
            System.out.println("There was a problem");
        }
    }


}
