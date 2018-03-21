package com.bank.bank;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseSerializer;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.IllegalAgeException;
import com.bank.exceptions.InsufficientPermissionException;
import com.bank.exceptions.InsuffiecintFundsException;
import com.bank.generics.AccountMap;
import com.bank.generics.RoleMap;
import com.bank.messages.Message;
import com.bank.security.PasswordHelpers;
import com.bank.userinterfaces.AdminTerminal;
import com.bank.userinterfaces.Atm;
import com.bank.userinterfaces.TellerTerminal;
import com.bank.users.Customer;
import com.bank.users.User;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;


public class Bank {
  /**
   * This is the main method to run your entire program! Follow the Candy Cane instructions to
   * finish this off.
   * @param argv unused.
   */
  public static void main(String[] argv) {
    try {

      //This is for first run only!
      if (argv[0].equals("-1")) {
        try {
          Connection connection = DatabaseDriverExtender.connectOrCreateDataBase();
          DatabaseDriverExtender.initialize(connection);
          // Inserting roles
          DatabaseInsertHelper.insertRole("ADMIN");
          DatabaseInsertHelper.insertRole("TELLER");
          DatabaseInsertHelper.insertRole("CUSTOMER");
          // Inserting Account Types
          DatabaseInsertHelper.insertAccountType("CHEQUING", new BigDecimal("0.01"));
          DatabaseInsertHelper.insertAccountType("SAVING", new BigDecimal("0.02"));
          DatabaseInsertHelper.insertAccountType("TFSA", new BigDecimal("0.03"));
          DatabaseInsertHelper.insertAccountType("RESTRICTEDSAVING", new BigDecimal("0.02"));
          DatabaseInsertHelper.insertAccountType("BALANCEOWING", new BigDecimal("0.04"));
          // Inserting first admin user
          DatabaseInsertHelper.insertNewUser("Admin", 19, "123 Admin Street", 1, "admin");
          connection.close();
        } catch (Exception e) {
          System.out.println("Connection already closed");
        }
      }
      
      /*If it is 1 - the user must first login with a valid admin account
      * This will allow the user to create new Teller's.  At this point, this is
      * all the admin can do.
      */
      if (argv[0].equals("1")) {
        adminMode();
      }
      
      //If anything else - including nothing
      if (!argv[0].equals("-1") && !argv[0].equals("1")) {
        mainMenu();
      }
    } catch (Exception e) {
      // Catches are handled in private function methods below
      System.out.println("Input error");
    }
  }
  
  // Admin Mode
  private static void adminMode() {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    int adminId;
    String adminPw;
    
    while (true) {
      try {
        // Reading user input
        System.out.print("ADMIN MODE (Default Admin ID = 1)\nUserID: ");
        adminId = Integer.parseInt(bfReader.readLine());
        System.out.print("Password: ");
        adminPw = bfReader.readLine();
        
        // Verifying that the user and password are correct
        User user = DatabaseSelectHelper.getUserDetails(adminId);
        if (user == null || user.getRoleId() != 1 || !user.authenticated(adminPw)) {
          // Ask for input again otherwise
          System.out.println("Login failed");
          continue;
        }
      } catch (Exception e) {
        // Ask again for input otherwise
        System.out.println("Login failed");
        continue;
      }
      
      break;
    }
    
    AdminTerminal terminal = new AdminTerminal(adminId, adminPw);
    
    while (true) {
      try {
        // Admin Terminal
        System.out.println("");
        System.out.println("Admin Interface");
        System.out.println("1: Make new Teller");
        System.out.println("2: Make new Admin");
        System.out.println("3: View all Admins");
        System.out.println("4: View all Tellers");
        System.out.println("5: View all Customers");
        System.out.println("6: View all Customer's Account(s) Info");
        System.out.println("7: View Total Money in the Bank");
        System.out.println("8: Promote Teller");
        System.out.println("9: Serialize Database");
        System.out.println("10: Deserialize Database");
        System.out.println("11: View any user's messages");
        System.out.println("12: View personal messages");
        System.out.println("13: Send message");
        System.out.println("14: Exit");
        int selection = Integer.parseInt(bfReader.readLine());
        
        if (selection == 1) {
          // Creating a new Teller
          System.out.print("New Teller Info\nTeller name: ");
          String tellerName = bfReader.readLine();
          System.out.print("Age: ");
          int tellerAge = Integer.parseInt(bfReader.readLine());
          System.out.print("Address: ");
          String tellerAddress = bfReader.readLine();
          System.out.print("Password: ");
          String tellerPw = bfReader.readLine();
          
          int tellerRoleId = RoleMap.getInstance().getRoleId("TELLER");

          // Inserting user
          try {
            int tellerId = terminal.makeNewUser(tellerName, tellerAge, tellerAddress, 
                tellerRoleId, tellerPw);
            System.out.print("Teller created with UserID " + tellerId + "\n");
          } catch (Exception e) {
            System.out.println("Invalid Age");
          }

        } else if (selection == 2) {
          // Creating a new Admin
          System.out.print("New Admin Info\nAdmin name: ");
          String newadminName = bfReader.readLine();
          System.out.print("Age: ");
          int newadminAge = Integer.parseInt(bfReader.readLine());
          System.out.print("Address: ");
          String newadminAddress = bfReader.readLine();
          System.out.print("Password: ");
          String newadminPw = bfReader.readLine();
          
          int adminRoleId = RoleMap.getInstance().getRoleId("ADMIN");
          // Inserting user
          try {
            int newadminId = terminal.makeNewUser(newadminName, newadminAge, newadminAddress, 
                adminRoleId, newadminPw);
            System.out.print("Admin created with UserID " + newadminId + "\n");
          } catch (IllegalAgeException e) {
            System.out.println("Invalid Age");
          }
        } else if (selection == 3) {
          // View all Admins
          System.out.println(terminal.listAdmins());
        } else if (selection == 4) {
          // View all Tellers
          System.out.println(terminal.listTellers());
        } else if (selection == 5) {
          // View all Customers
          System.out.println(terminal.listCustomers());
        } else if (selection == 6) {
          // View All Accounts Balance of a given customer
          listCustomerAccountsInterface();
        } else if (selection == 7) {
          // View How Much Money the Bank is Currently Trafficking
          BigDecimal dosh = terminal.bankBalance();
          System.out.println("The amount of money our bank is trafficking: $" + dosh);
        } else if (selection == 8) {
          // Promote Teller to Admin
          promoteTeller();
        } else if (selection == 9) {
          // Serialize Database
          if (DatabaseSerializer.serializeDatabase()) {
            System.out.println("Database serialized and saved");
          } else {
            System.out.println("Database serialization failed");
          }
        } else if (selection == 10) {
          // Check if there is even a backup database to restore from
          File f = new File("database_copy.ser");
          if (f.exists()) {
            // Deserialize Database
            if (DatabaseSerializer.deserializeDatabase()) {
              System.out.println("Database deserialized, original overwritten");
              
              // Checking if the admin is in the database
              int adminCheck = DatabaseSerializer.checkAdmin(terminal.getCurrentAdmin(), adminPw);
              
              if (adminCheck > 0) {
                System.out.println(
                    "You were not in the deserialized database, but have now been added");
                System.out.println("Your new user ID is " + adminCheck);
              } else if (adminCheck < 0) {
                System.out.println("We could not add you to the deserialized database");
              }
            } else {
              System.out.println("Database deserialization failed");
              System.out.println("Original database restored");
            }
          } else {
            System.out.println("There is no backup database to restore to");
          }
        } else if (selection == 11) {
          // view any message
          viewAnyMessage(terminal);
        } else if (selection == 12) {
          // view personal messages
          viewMessages(adminId);
        } else if (selection == 13) {
          // send messages
          sendMessage(adminId, adminPw);
        } else if (selection == 14) {
          // Exit
          break;
        }
      } catch (Exception e) {
        System.out.println("Invalid input");
        continue;
      }
    }
  }
  
  // Main Menu
  private static void mainMenu() {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    while (true) {
      try {
        // Reading user input
        System.out.println("1 - TELLER Interface");
        System.out.println("2 - ATM Interface");
        System.out.println("0 - Exit");
        System.out.print("Enter Selection: ");
        int selection = Integer.parseInt(bfReader.readLine());
        
        if (selection == 1) {
          tellerInterface();
        } else if (selection == 2) {
          atmInterface();
        } else if (selection == 0) {
          break;
        }
      } catch (Exception e) {
        System.out.println("Selection failed");
        continue;
      }
    }
  }
  
  //If the user entered 1
  private static void tellerInterface() {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    int tellerId;
    String tellerPw;
    boolean customerSet = false;
    
    try {
      // Reading user input
      System.out.print("Teller ID: ");
      tellerId = Integer.parseInt(bfReader.readLine());
      System.out.print("Password: ");
      tellerPw = bfReader.readLine();
      
      // Verifying teller and password
      User user = DatabaseSelectHelper.getUserDetails(tellerId);
      if (user == null || !DatabaseSelectHelper.getRole(user.getRoleId()).equals("TELLER")
          || !user.authenticated(tellerPw)) {
        System.out.println("Login failed");
        return;
      }
    } catch (Exception e) {
      System.out.println("Login failed");
      return;
    }
    
    // Creating terminal object with the verified teller
    TellerTerminal terminal = new TellerTerminal(tellerId, tellerPw);
    
    while (true) {
      try {
        // Reading user input
        System.out.println("");
        if (customerSet) {
          System.out.print("Current customer: " + terminal.getCurrentCustomer().getName());
          System.out.println(" (ID: " + terminal.getCurrentCustomer().getId() + ")");
          System.out.println("Address: " + terminal.getCurrentCustomer().getAddress());
        }
        System.out.println("TELLER INTERFACE");
        System.out.println("1: Authenticate new Customer");
        System.out.println("2: Make new Customer");
        System.out.println("3: Make new account");
        System.out.println("4: Give interest");
        System.out.println("5: Make a deposit");
        System.out.println("6: Make a withdrawal");
        System.out.println("7: Check balance of an account");
        System.out.println("8: List accounts");
        System.out.println("9: Update Customer Information");
        System.out.println("10: Close customer session");
        System.out.println("11: View my messages");
        System.out.println("12: View customer's messages");
        System.out.println("13: Leave message for customer");
        System.out.println("14: Exit");
        int selection = Integer.parseInt(bfReader.readLine());
        
        if (selection == 1) {
          // Authenticate a new customer
          customerSet = authenticateUserInterface(terminal);
        } else if (selection == 2) {
          // Creating a new customer
          customerSet = newUserInterface(terminal);
        } else if (selection == 3) {
          // Check that a customer is authenticated
          if (customerSet) {
            newAccountInterface(terminal);
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 4) {
          // Check that a customer is authenticated
          if (customerSet) {
            while (true) {
              System.out.println("");
              System.out.println("Apply Interest to:");
              System.out.println("1: One Account");
              System.out.println("2: All Accounts");
              int choice = Integer.parseInt(bfReader.readLine());
              if (choice == 1) {
                giveInterestInterface(terminal);
                break;
              } else if (choice == 2) {
                giveAllInterestInterface(terminal);
                break;
              }
            }
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 5) {
          // Check that a customer is authenticated
          if (customerSet) {
            makeDeposit(terminal);
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 6) {
          // Check that a customer is authenticated
          if (customerSet) {
            makeWithdrawal(terminal);
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 7) {
          // View balance of a single account
          // Check that a customer is authenticated
          if (customerSet) {
            checkBalance(terminal);
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 8) {
          if (customerSet) {
            listAccountsInterface(terminal);
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 9) {
          // Update Customer info
          if (customerSet) {
            updateUserInterface(terminal);
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 10) {
          // Check that a customer is authenticated
          if (customerSet) {
            terminal.deAuthenticateCustomer();
            customerSet = false;
            System.out.println("Customer session closed");
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 11) {
          // view teller's messages
          viewMessages(tellerId);
        } else if (selection == 12) {
          if (customerSet) {
            // view current customers messages
            viewMessages(terminal.getCurrentCustomer().getId());
          } else {
            System.out.println("No customer authenticated");
          }
        } else if (selection == 13) {
          // leave a message for the current customer
          leaveMessage(terminal);
        } else if (selection == 14) {
          // Exit
          break;
        }
      } catch (Exception e) {
        System.out.println("Selection failed");
        continue;
      }
    }
  }
  
  //If the user entered 2
  private static void atmInterface() {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    int customerId;
    String customerPw;
    
    try {
      // Reading user input
      System.out.print("Customer ID: ");
      customerId = Integer.parseInt(bfReader.readLine());
      System.out.print("Password: ");
      customerPw = bfReader.readLine();
      
      // Verifying customer and password
      User user = DatabaseSelectHelper.getUserDetails(customerId);
      if (user == null || !DatabaseSelectHelper.getRole(user.getRoleId()).equals("CUSTOMER")
          || !user.authenticated(customerPw)) {
        System.out.println("Login failed");
        return;
      }
    } catch (Exception e) {
      System.out.println("Login failed");
      return;
    }
    
    // Creating a new Atm object with the verified customer
    Atm atm = new Atm(customerId, customerPw);
    
    while (true) {
      try {
        // Reading user input
        User user = DatabaseSelectHelper.getUserDetails(customerId);
        System.out.println("");
        System.out.println("Welcome " + user.getName());
        System.out.println("Your address: " + user.getAddress());
        listAccountsInterface(atm);
        System.out.println("");
        System.out.println("ATM INTERFACE");
        System.out.println("1: List Accounts and balances");
        System.out.println("2: Make Deposit");
        System.out.println("3: Check balance");
        System.out.println("4: Make withdrawal");
        System.out.println("5: View messages");
        System.out.println("6: Exit");
        int selection = Integer.parseInt(bfReader.readLine());
        
        if (selection == 1) {
          listAccountsInterface(atm);
        } else if (selection == 2) {
          makeDeposit(atm);
        } else if (selection == 3) {
          checkBalance(atm);
        } else if (selection == 4) {
          makeWithdrawal(atm);
        } else if (selection == 5) {
          viewMessages(customerId);
        } else if (selection == 6) {
          break;
        }
      } catch (Exception e) {
        System.out.println("Selection failed");
        continue;
      }
    }
  }

  // Helper Functions
  private static boolean authenticateUserInterface(TellerTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    boolean authenticated = false;
    
    try {
      // Reading user input
      System.out.print("Customer ID: ");
      int customerId = Integer.parseInt(bfReader.readLine());
      System.out.print("Password: ");
      String customerPw = bfReader.readLine();
      
      // Verifying customer and password
      User user = DatabaseSelectHelper.getUserDetails(customerId);
      if (user == null || user.getRoleId() != RoleMap.getInstance().getRoleId("CUSTOMER")) {
        System.out.println("Invalid customer");
      } else if (!user.authenticated(customerPw)) {
        System.out.println("Authentication failed");
      } else {
        terminal.setCurrentCustomer((Customer)user);
        terminal.authenticateCurrentCustomer(customerPw);
        authenticated = true;
        System.out.println("Customer Authenticated");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid ID");
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return authenticated;
  }
  
  private static boolean newUserInterface(TellerTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    boolean completed = false;
    
    try {
      // Reading user input
      System.out.print("Customer Name: ");
      String customerName = bfReader.readLine();
      System.out.print("Age: ");
      int customerAge = Integer.parseInt(bfReader.readLine());
      System.out.print("Address: ");
      String customerAddress = bfReader.readLine();
      System.out.print("Password: ");
      String customerPw = bfReader.readLine();
      
      // Creating the new user
      int customerUserId = terminal.makeNewUser(customerName,
          customerAge, customerAddress, customerPw);
      
      if (customerUserId != -1) {
        completed = true;
        System.out.println("User created and authenticated with ID " + customerUserId);
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid Age");
    } catch (IllegalAgeException e) {
      System.out.println("Invalid Age");
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return completed;
  }
  
  private static boolean updateUserInterface(TellerTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    boolean completed = false;
    
    try {
      // Reading user input
      int customerId = terminal.getCurrentCustomer().getId();
      System.out.print("New Name: ");
      String customerName = bfReader.readLine();
      System.out.print("New Age: ");
      int customerAge = Integer.parseInt(bfReader.readLine());
      System.out.print("New Address: ");
      String customerAddress = bfReader.readLine();
      System.out.print("New Password: ");
      String customerPw = bfReader.readLine();
      
      DatabaseUpdateHelper.updateUserName(customerName, customerId);
      DatabaseUpdateHelper.updateUserAge(customerAge, customerId);
      DatabaseUpdateHelper.updateUserAddress(customerAddress, customerId);
      String hashedPw = PasswordHelpers.passwordHash(customerPw);
      DatabaseUpdateHelper.updateUserPassword(hashedPw, customerId);
      
      Customer customer = (Customer) DatabaseSelectHelper.getUserDetails(customerId);
      terminal.setCurrentCustomer(customer);
      
    } catch (NumberFormatException e) {
      System.out.println("Invalid Age");
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return completed;
  }
  
  private static void newAccountInterface(TellerTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    
    try {
      // Reading user input
      System.out.print("Account Name: ");
      String accountName = bfReader.readLine();
      System.out.print("Balance (including two decimal places): ");
      BigDecimal accountBalance = new BigDecimal(bfReader.readLine());
      System.out.print("Account Type (name): ");
      String accountType = bfReader.readLine();
      
      int accountTypeId = -1;
      AccountMap accountMap = AccountMap.getInstance();
      accountTypeId = accountMap.getTypeId(accountType.toUpperCase());
      
      // Verifying that the account type is valid
      if (accountTypeId == -1) {
        System.out.println("Invalid account type");
        String accountTypeNames = "Available account types: ";
        for (int typeId : DatabaseSelectHelper.getAccountTypesIds()) {
          accountTypeNames += DatabaseSelectHelper.getAccountTypeName(typeId) + ", ";
        }
        System.out.println(accountTypeNames.substring(0, accountTypeNames.length() - 2));
      }
      
      if (accountTypeId == AccountMap.getInstance().getTypeId("SAVING")
          && accountBalance.floatValue() < 1000.00) {
        System.out.println("Savings accounts require a minimum $1000 deposit");
      } else if (terminal.makeNewAccount(accountName, accountBalance, accountTypeId) != -1) {
        System.out.println("Account created");
        String accountsString = "";
        System.out.println("Customer account list:");
        // Creating string of customer's accounts
        if (terminal.listAccounts() != null) {
          for (Account account : terminal.listAccounts()) {
            accountsString += account.getName() + " (ID: " + account.getId() + ")\n";
          }
        }
        System.out.println(accountsString.substring(0, accountsString.length() - 1));
      } else {
        System.out.println("Account creation failed");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid Balance");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void giveInterestInterface(TellerTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    
    try {
      // Reading user input
      System.out.print("Give interest to which account (ID)?: ");
      int accountId = Integer.parseInt(bfReader.readLine());
      
      // Verifying that the account is of the current customer's
      boolean accountMatch = false;
      for (Account account : terminal.listAccounts()) {
        if (account.getId() == accountId) {
          accountMatch = true;
          break;
        }
      }
      
      // Proceeding if the account ID is in the customer's accounts
      if (accountMatch) {
        terminal.giveInterest(accountId);
        System.out.println("Interest added");
        System.out.println("New balance: " + DatabaseSelectHelper.getBalance(accountId));
      } else {
        System.out.println("Invalid account ID");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid account ID");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void giveAllInterestInterface(TellerTerminal terminal) {
    try {
      for (Account customeraccounts : terminal.listAccounts()) {
        customeraccounts.findAndSetInterestRate();
        customeraccounts.addInterest();
      }
      
      System.out.println("Interest added");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // ListAccountsInterface
  private static void listAccountsInterface(Atm atm) {
    String accountsString = "";
    System.out.println("Accounts:");
    // Creating string of the customer's accounts
    if (atm.listAccounts() != null) {
      for (Account account : atm.listAccounts()) {
        accountsString += account.getName() + ", Balance: " + account.getBalance() 
          + ", ID: " + account.getId() + ", Type: " 
            + DatabaseSelectHelper.getAccountTypeName(account.getType()) + "\n";
      }
      
      if (!accountsString.isEmpty()) {
        System.out.println(accountsString.substring(0, accountsString.length() - 1));
      } else {
        System.out.println("No accounts found");
      }
    }
  }

  private static void listAccountsInterface(TellerTerminal terminal) {
    String accountsString = "";
    System.out.println("Accounts:");
    // Creating string of the customer's accounts
    if (terminal.listAccounts() != null) {
      for (Account account : terminal.listAccounts()) {
        accountsString += account.getName() + ", Balance: " + account.getBalance() 
            + ", ID: " + account.getId() + ", Type: " 
              + DatabaseSelectHelper.getAccountTypeName(account.getType()) + "\n";
      }
      
      if (!accountsString.isEmpty()) {
        System.out.println(accountsString.substring(0, accountsString.length() - 1));
      } else {
        System.out.println("No accounts found");
      }
    }
  }

  private static void listCustomerAccountsInterface() {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    boolean validcustomer = false;
    int customerId = -1;
    
    try {
      // Reading user input
      System.out.print("Customer ID: ");
      customerId = Integer.parseInt(bfReader.readLine());
      
      // Verifying customer and password
      User user = DatabaseSelectHelper.getUserDetails(customerId);
      if (user == null || user.getRoleId() != 3) {
        System.out.println("Invalid customer");
      } else {
        validcustomer = true;
        
      }
      
      if (validcustomer) {
        Customer customer = (Customer) DatabaseSelectHelper.getUserDetails(customerId);
        System.out.println("Selected Customer: " + customer.getName());
        String accountsString = "";
        System.out.println("Accounts:");
        // Creating string of the customer's accounts
        if (customer.getAccounts() != null) {
          for (Account account : customer.getAccounts()) {
              accountsString += account.getName() + ", Balance: " + account.getBalance() 
                + ", ID: " + account.getId() + ", Type: " 
                  + DatabaseSelectHelper.getAccountTypeName(account.getType()) + "\n";
          }
          
          if (!accountsString.isEmpty()) {
            System.out.println(accountsString.substring(0, accountsString.length() - 1));
          } else {
            System.out.println("No accounts found");
          }
        }
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid ID");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void makeDeposit(Atm atm) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    
    try {
      // Reading user input
      System.out.print("Account to deposit to (ID): ");
      int accountId = Integer.parseInt(bfReader.readLine());
      System.out.print("Amount to deposit (including 2 decimal places): ");
      BigDecimal depositAmount = new BigDecimal(bfReader.readLine());
      
      // Check that the amount has 2 or fewer decimal places
      if (depositAmount.scale() > 2) {
        System.out.println("Invalid amount (2 decimal places maximum)");
        return;
      }
      
      // Attempting to find the account ID in the customer's accounts
      boolean accountMatch = false;
      for (Account account : atm.listAccounts()) {
        if (account.getId() == accountId) {
          accountMatch = true;
          break;
        }
      }
      
      // Proceeding if a match was found
      if (accountMatch) {
        if (atm.makeDeposit(depositAmount, accountId)) {
          System.out.println("Deposit made");
          System.out.println("New balance: " + DatabaseSelectHelper.getBalance(accountId));
        } else {
          System.out.println("Deposit failed");
        }
      } else {
        System.out.println("Invalid account ID");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid input");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void makeWithdrawal(Atm atm) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    
    try {
      // Reading user input
      System.out.print("Account to withdraw from (ID): ");
      int accountId = Integer.parseInt(bfReader.readLine());
      System.out.print("Amount to withdraw (including two decimal places): ");
      BigDecimal withdrawAmount = new BigDecimal(bfReader.readLine());
      
      // Check that the amount has 2 or fewer decimal places
      if (withdrawAmount.scale() > 2) {
        System.out.println("Invalid amount (2 decimal places maximum)");
        return;
      }
      
      // Attempting to find the account ID in the customer's account
      boolean accountMatch = false;
      
      for (Account account : atm.listAccounts()) {
        if (account.getId() == accountId) {
          accountMatch = true;
          break;
        }
      }
      Account type = DatabaseSelectHelper.getAccountDetails(accountId);
      // Proceeding if a match was found
      if (accountMatch) {
        if (atm.makeWithdrawal(withdrawAmount, accountId)) {
          System.out.println("Withdrawal made");
          System.out.println("New balance: " + DatabaseSelectHelper.getBalance(accountId));
          if (type.getType() == AccountMap.getInstance().getTypeId("SAVING")
              && DatabaseSelectHelper.getBalance(accountId).floatValue() < 1000.00f) {
            DatabaseUpdateHelper.updateAccountType(AccountMap.getInstance().getTypeId("CHEQUING"),
                accountId);
          }
        } else {
          System.out.println("Withdrawal failed");
        }
      } else {
        System.out.println("Invalid account ID");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid input");
    } catch (InsuffiecintFundsException e) {
      System.out.println("Insufficient funds");
    } catch (InsufficientPermissionException e) {
      System.out.println("You require a Teller to authenticate this action");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void checkBalance(Atm atm) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    
    try {
      // Reading user input
      System.out.print("Account to check (ID): ");
      int accountId = Integer.parseInt(bfReader.readLine());
      
      // Attempting to find the account ID in the customer's account
      boolean accountMatch = false;
      for (Account account : atm.listAccounts()) {
        if (account.getId() == accountId) {
          accountMatch = true;
          break;
        }
      }
      
      // Proceeding if a match was found
      if (accountMatch) {
        System.out.println("Balance: " + atm.checkBalance(accountId));
      } else {
        System.out.println("Invalid account ID");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid account ID");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static void promoteTeller() {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);
    
    try {
      // Reading user input
      System.out.print("Teller ID that you wish to promote: ");
      int tellerId = Integer.parseInt(bfReader.readLine());
      
      // Verifying customer and password
      User user = DatabaseSelectHelper.getUserDetails(tellerId);
      if (user == null || user.getRoleId() != RoleMap.getInstance().getRoleId("TELLER")) {
        System.out.println("Invalid Teller");
      } else {
        DatabaseUpdateHelper.updateUserRole(RoleMap.getInstance().getRoleId("ADMIN"), tellerId);
        System.out.println("Congratulations " + user.getName() + ", you have been promoted!");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid ID");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static void viewAnyMessage(AdminTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);

    try {
      // Get messageId
      System.out.print("User's ID: ");
      int userId = Integer.parseInt(bfReader.readLine());
      // Get messages
      List<Message> messages = DatabaseSelectHelper.getAllMessages(userId);
      if (messages.isEmpty()) {
        System.out.println("No messages");
      }
      for (Message message : messages) {
        if (message.getViewed() != 1) {
          System.out.print("(unread) ");
        }
        
        System.out.println(message.getMessage());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void viewMessages(int userId) {
    try {
      // Get messages
      List<Message> messages = DatabaseSelectHelper.getAllMessages(userId);
      if (messages.isEmpty()) {
        System.out.println("No messages");
      }
      
      System.out.println("Messages: ");
      for (Message message : messages) {
        if (message.getViewed() != 1) {
          System.out.print("(unread) ");
          
          // Updating viewed status since it was viewed
          DatabaseUpdateHelper.updateUserMessageState(message.getMessageid());
        }
        
        System.out.println(message.getMessage());
      }
    } catch (Exception e) {
      System.out.println("Invalid input");
    }
  }
  
  private static void sendMessage(int id, String password) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);

    try {
      AdminTerminal terminal = new AdminTerminal(id, password);
      // get recipient
      System.out.print("ID of recipient: ");
      int userId = Integer.parseInt(bfReader.readLine());
      // get message
      System.out.print("Message: ");
      String message = bfReader.readLine();

      int messageId = terminal.createMessage(userId, message);

      // tell the user if the message was successful
      if (messageId != -1) {
        System.out.println("Message sent");
      } else {
        System.out.println("Message could not be sent");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  // Reusing other viewMessages method instead
  /*private static void viewMessages(TellerTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);

    try {
      System.out.print("Message ID:");
      int messageId = Integer.parseInt(bfReader.readLine());
      // Get message
      System.out.println(terminal.viewMessage(messageId));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }*/
  
  private static void leaveMessage(TellerTerminal terminal) {
    InputStreamReader isReader = new InputStreamReader(System.in);
    BufferedReader bfReader = new BufferedReader(isReader);

    try {
      // get recipient
      //int userId = terminal.getCurrentCustomer().getId();
      System.out.print("ID of recipient: ");
      int userId = Integer.parseInt(bfReader.readLine());
      // get message
      System.out.print("Message: ");
      String message = bfReader.readLine();

      // send message
      int messageId = terminal.createMessage(userId, message);

      // tell the user if the message was successful
      if (messageId != -1) {
        System.out.println("Message sent");
      } else {
        System.out.println("Message could not be sent");
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid ID");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
