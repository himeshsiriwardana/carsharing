package carsharing;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static void printCompanyList(List<Company> companies){
        System.out.println("Choose a company:");
        int i = 1;
        for(Company company: companies){
            System.out.println(i + ". " + company.toString());
            i++;
        }
        System.out.println("0. Back");
    }

    private static void printCarList(List<Car> cars){
        int i = 1;
        for(Car car: cars){
            System.out.println(i + ". " + car.toString());
            i++;
        }
    }

    private static void printCustomerList(List<Customer> customers){
        int i = 1;
        for(Customer customer: customers){
            System.out.println(i + ". " + customer.toString());
            i++;
        }
    }

    private static void companyCarOperations(List<Company> companies, dbCompanyDao companyDao){

        printCompanyList(companies);
        int chosenCompanyId = scanner.nextInt();
        scanner.nextLine();

        if(chosenCompanyId != 0) {

            Company chosenCompany = companies.get(chosenCompanyId - 1);
            System.out.println(chosenCompany.toString() + " company");
            while (true) {
                System.out.println("1. Car list");
                System.out.println("2. Create a car");
                System.out.println("0. Back");

                int carChoice = scanner.nextInt();
                scanner.nextLine();

                if (carChoice == 1) {
                    List<Car> listCar = companyDao.listCars(chosenCompanyId);
                    System.out.println(listCar);
                    if (listCar == null) {
                        System.out.println("The car list is empty!");
                    } else {
                        System.out.println(chosenCompany.toString() + " cars:");
                        printCarList(listCar);
                    }
                } else if (carChoice == 2) {
                    System.out.println("Enter the car name:");
                    String car = scanner.nextLine();
                    companyDao.createCar(car, chosenCompanyId);

                } else if (carChoice == 0) {
                    break;
                }
            }
        }
    }

    private static void customerCarOperations(List<Customer> customers, List<Company> companies, dbCompanyDao companyDao) throws SQLException {

        while (true) {
            System.out.println("Choose a customer:");
            printCustomerList(customers);
            System.out.println("0. Back");

            int chosenCustomerID = scanner.nextInt();
            scanner.nextLine();

            if (chosenCustomerID == 0) {
                break;

            } else {
                while (true) {
                    System.out.println("1. Rent a car");
                    System.out.println("2. Return a rented car");
                    System.out.println("3. My rented car");
                    System.out.println("0. Back");

                    int customerCarChoice = scanner.nextInt();
                    scanner.nextLine();
                    Integer customerRentedCar = null;
                    Map<String, Object> rentedCar = companyDao.customerRentedCar(chosenCustomerID);
                    if(rentedCar != null) {
                        customerRentedCar = (Integer) rentedCar.get("CAR_ID");
                    }


                    if (customerCarChoice == 0) {
                        break;
                    }
                    else if (customerCarChoice == 1) {

                        if (companies.isEmpty()) {
                            System.out.println("The company list is empty!");

                        } else if (customerRentedCar != null) {
                            System.out.println("You've already rented a car!");

                        } else {
                            printCompanyList(companies);
                            int chosenCompanyId = scanner.nextInt();
                            scanner.nextLine();

                            if(chosenCompanyId != 0) {
                                companyCustomerOperations(chosenCustomerID, chosenCompanyId, companyDao);


                            }

                        }
                    }

                    else if (customerCarChoice == 2) {

                        if ( customerRentedCar == null) {
                            System.out.println("You didn't rent a car!");
                        }

                        else {
                            boolean returnStatus = companyDao.customerReturnsCar(chosenCustomerID);
                            if (returnStatus) {
                                System.out.println("You've returned a rented car!");
                            } else {
                                System.out.println("There was a problem");
                            }
                        }
                    }

                    else if (customerCarChoice == 3) {


                        if(rentedCar.get("CAR_ID") == null){
                                System.out.println("You didn't rent a car!");
                        }
                        else {
                            System.out.println("Your rented car:");
                            System.out.println((String)rentedCar.get("CAR_NAME"));
                            System.out.println("Company");
                            System.out.println((String)rentedCar.get("COMPANY_NAME"));
                            }
                    }

                }
            }

        }
    }

    private static void companyCustomerOperations(int customerId, int companyId, dbCompanyDao companyDao) throws SQLException {

        List<Car> companyAvailableCars = companyDao.companyAvailableCars(companyId);
        Company companyDetails = companyDao.companyDetails(companyId);
        String companyName = companyDetails.toString();

            if (companyAvailableCars.isEmpty()) {
                System.out.printf("No available cars in the '%s' company%n%n", companyName);

            } else {

                System.out.println("Choose a car:");
                printCarList(companyAvailableCars);
                System.out.println("0. Back");

                int customerChosenCar = scanner.nextInt();
                scanner.nextLine();

                if(customerChosenCar != 0) {
                    List<String> customerRentedCar = companyDao.customerRentsCar(customerChosenCar, customerId, companyId);
                    System.out.printf("You rented '%s' %n", customerRentedCar.get(0));
                }

            }

    }


    private static void performUserOperation(dbCompanyDao companyDao) throws SQLException {


        while(true) {
            System.out.println("1. Log in as a manager");
            System.out.println("2. Log in as a customer");
            System.out.println("3. Create a customer");
            System.out.println("0. Exit");
            int login_input = scanner.nextInt();
            scanner.nextLine();

            List<Customer> customerList = companyDao.listCustomers();

            if(login_input == 1){
                while(true){
                    System.out.println("1. Company list");
                    System.out.println("2. Create a company");
                    System.out.println("0. Back");

                    int operation_input = scanner.nextInt();
                    scanner.nextLine();

                    if(operation_input == 1){
                        List<Company> companyList = companyDao.listCompanies();
                        if(companyList == null || companyList.isEmpty()){
                            System.out.println("The company list is empty");
                        }
                        else{
                           companyCarOperations(companyList, companyDao);
                        }
                    }

                    else if (operation_input == 2){
                        System.out.println("Enter the company name:");
                        String company = scanner.nextLine();
                        companyDao.createCompany(company);
                    }

                    else if (operation_input == 0){
                        break;
                    }
                }
            }

            else if (login_input == 2){
                List<Company> companyList = companyDao.listCompanies();
                    if(customerList == null || customerList.isEmpty()){
                        System.out.println("The customer list is empty!");

                    }
                    else{
                        customerCarOperations(customerList, companyList, companyDao);
                }
            }

            else if (login_input == 3) {
                System.out.println("Enter the customer name:");
                String newCustomer = scanner.nextLine();
                companyDao.createCustomer(newCustomer);
            }

            else if (login_input == 4) {
                companyDao.deleteAllCustomers();
            }

            else if (login_input == 5) {
                companyDao.deleteAllCars();
            }

            else if (login_input == 6) {
                companyDao.deleteAllCompanies();
            }

            else if (login_input == 0){
                System.exit(0);
            }
        }

    }

    public static void main(String[] args) throws SQLException {

        String databaseName = "mydatabase";

        for(int i = 0; i < args.length; i++){
            if(args[i].equals("-databaseFileName") && i+1 < args.length){
                databaseName = args[i+1];
                break;
            }
        }

        dbCompanyDao companyDao = new dbCompanyDao(databaseName);

        performUserOperation(companyDao);
    }
}