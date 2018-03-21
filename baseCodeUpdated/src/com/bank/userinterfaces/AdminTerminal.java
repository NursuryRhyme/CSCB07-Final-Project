package com.bank.userinterfaces;

import com.bank.accounts.Account;
import com.bank.databasehelper.DatabaseInsertHelper;
import com.bank.databasehelper.DatabaseSelectHelper;
import com.bank.databasehelper.DatabaseUpdateHelper;
import com.bank.exceptions.IllegalAgeException;
import com.bank.exceptions.IllegalAmountException;
import com.bank.generics.RoleMap;
import com.bank.messages.Message;
import com.bank.users.Customer;
import com.bank.users.User;

import java.math.BigDecimal;
import java.util.List;

public class AdminTerminal {
  private User currentAdmin = null;
  private boolean currentAdminAuthenticated = false;
  private RoleMap rolemap = RoleMap.getInstance();
  
  /**
   * Constructor.
   * @param adminId Admin's Id
   * @param adminpw Admin's Password
   */
  public AdminTerminal(int adminId, String adminpw) {
    this.currentAdmin = DatabaseSelectHelper.getUserDetails(adminId);
    
    if (this.currentAdmin != null) {
      this.currentAdminAuthenticated = currentAdmin.authenticated(adminpw);
    }
  }
  
  
  public void setCurrentAdmin(User admin) {
    this.currentAdmin = admin;
  }
  
  public User getCurrentAdmin() {
    return this.currentAdmin;
  }
  
  /**
   * Creates a new User Object.
   * @param name of the new User
   * @param age of the new User
   * @param address of the new User
   * @param roleId of the new User
   * @param password of the new User
   * @return the userId of the new User
   * @throws IllegalAgeException thrown if a negative age is entered
   */
  public int makeNewUser(String name, int age, String address,
      int roleId, String password) throws IllegalAgeException {
    int userId = -1;
    
    if (this.currentAdminAuthenticated) {
      userId = DatabaseInsertHelper.insertNewUser(name, age, address, roleId, password);
      this.currentAdmin = DatabaseSelectHelper.getUserDetails(userId);
    }
    
    return userId;
  }
  
  /**
   * Lists all admins in the database.
   * @return String representation of all admins
   */
  public String listAdmins() {
    User users;
    int i = 1;
    String alladmins = "Current Admins: ";
    
    int adminRoleId = rolemap.getRoleId("ADMIN");
    // Grab all users in the database and sort them
    try {
      while ((users = DatabaseSelectHelper.getUserDetails(i)) != null) {
        if (users.getRoleId() == adminRoleId) {
          alladmins += " " + users.getName() + " (ID: " + users.getId() + "),";
        }
        i ++;
      }
    } catch (Exception e) {
      i ++;
    }
    return alladmins.substring(0, alladmins.length() - 1);
  }
  
  /**
   * Lists all tellers in the database.
   * @return String representation of all tellers
   */
  public String listTellers() {
    User users;
    int i = 1;
    
    String alltellers = "Current Tellers: ";
    
    int tellerRoleId = rolemap.getRoleId("TELLER");
    
    // Grab all users in the database and sort them
    try {
      while ((users = DatabaseSelectHelper.getUserDetails(i)) != null) {
        if (users.getRoleId() == tellerRoleId) {
          alltellers += " " + users.getName() + " (ID: " + users.getId() + "),";
        }
        i ++;
      }
    } catch (Exception e) {
      i ++;
    }
    return alltellers.substring(0, alltellers.length() - 1);
  }
  
  /**
   * Lists all customers in the database.
   * @return String representation of all customers
   */
  public String listCustomers() {
    User users;
    int i = 1;
    
    String allcustomers = "Current Customers: ";
    
    int customerRoleId = rolemap.getRoleId("CUSTOMER");
    // Grab all users in the database and sort them
    try {
      while ((users = DatabaseSelectHelper.getUserDetails(i)) != null) {
        if (users.getRoleId() == customerRoleId) {
          allcustomers += " " + users.getName() + " (ID: " + users.getId() + "),";
        }
        i ++;
      }
    } catch (Exception e) {
      i ++;
    }
    return allcustomers.substring(0, allcustomers.length() - 1);
  }

  /**
   * Reads the message which has id of messageId.
   * @param messageId the id of the message
   * @return the contents of the message
   */
  public String viewMessage(int messageId) {
    // Check authentication
  
    String specificMessage = DatabaseSelectHelper.getSpecificMessage(messageId);
    String result = "";
    // Get all the Admin's messages
    List<Message> messages = DatabaseSelectHelper.getAllMessages(this.currentAdmin.getId());
    // Go through the Admin's messages
    for (Message message : messages) {
      // Check that the message is the Admin's
      if (message.getMessage().equals(specificMessage)) {
        // update viewed status and return the message
        DatabaseUpdateHelper.updateUserMessageState(messageId);
        result = specificMessage;
      }
    }
    return result;
  }

  /**
   * Creates a message to be left to a user.
   * @param userid the id of the recipient
   * @param message the message for the recipient
   * @return the messageid of this message
   */
  public int createMessage(int userid, String message) {
    int messageid = -1;
    try {
      messageid = DatabaseInsertHelper.insertMessage(userid, message);
    } catch (IllegalAmountException e) {
      System.out.println("Message must not be over 512 characters");
    }
    return messageid;
  }
  
  /**
   * Returns the total amount of money in the bank.
   * @return BigDecimal amount of money in the bank
   */
  public BigDecimal bankBalance() {
    User users;
    int i = 1;
    RoleMap rolemap = RoleMap.getInstance();
    BigDecimal dosh = new BigDecimal("0");
    int customerRoleId = rolemap.getRoleId("CUSTOMER");
    // Grab all users in the database and sort them
    try {
      while ((users = DatabaseSelectHelper.getUserDetails(i)) != null) {
        if (users.getRoleId() == customerRoleId) {
          Customer customer = (Customer) users;
          for (Account accounts : customer.getAccounts()) {
            dosh = dosh.add(accounts.getBalance());
          }
        }
        i ++;
      }
    } catch (Exception e) {
      i ++;
    }
    return dosh;
  }
}
