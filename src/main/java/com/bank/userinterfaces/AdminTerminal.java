package com.bank.userinterfaces;

import android.content.Context;

import com.bank.database.android.DatabaseHelper;
import com.bank.exceptions.IllegalAgeException;
import com.bank.generics.RoleMap;
import com.bank.messages.Message;
import com.bank.users.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AdminTerminal {
  private User currentAdmin = null;
  private boolean currentAdminAuthenticated = false;
  private RoleMap roleMap;
  private DatabaseHelper dbHelper;
  
  /**
   * Constructor.
   * @param adminId Admin's Id
   * @param adminPw Admin's Password
   */
  public AdminTerminal(Context context, int adminId, String adminPw) {
    this.roleMap = RoleMap.getInstance(context);
    dbHelper = new DatabaseHelper(context);
    this.currentAdmin = dbHelper.getUserObject(adminId);
    
    if (this.currentAdmin != null) {
      this.currentAdminAuthenticated = currentAdmin.authenticated(adminPw);
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
  public int makeNewUser(String name, int age, String address, int roleId, String password) {
    int userId = -1;
    
    if (this.currentAdminAuthenticated) {
      userId = (int) dbHelper.insertNewUser(name, age, address, roleId, password);
      this.currentAdmin = dbHelper.getUserObject(userId);
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
    
    int adminRoleId = roleMap.getRoleId("ADMIN");
    // Grab all users in the database and sort them
    try {
      while ((users = dbHelper.getUserObject(i)) != null) {
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
    
    int tellerRoleId = roleMap.getRoleId("TELLER");
    
    // Grab all users in the database and sort them
    try {
      while ((users = dbHelper.getUserObject(i)) != null) {
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
  public List<User> listUsersByRole(int roleId) {
    List<User> users = new ArrayList<>();

    for (int userId : dbHelper.getUserIds()) {
      if (dbHelper.getUserRole(userId) == roleId) {
        users.add(dbHelper.getUserObject(userId));
      }
    }
    
    return users;
  }

  /**
   * Get a list of messages from the given userId.
   * @param userId of the person you wanna see messages from
   * @return list of messages
   */
  public List<Message> listMessages(int userId) {
    List<Message> messages = dbHelper.getMessageList((userId));

    if (messages != null) {
      for (Message message : messages) {
        dbHelper.updateUserMessageState(message.getId());
      }
    }

    return messages;
  }

  /**
   * View the message with the given messageId.
   * @param messageId the id of the message
   * @return the contents of the message
   */
  public String viewMessage(int messageId) {
    // Check authentication
  
    String specificMessage = dbHelper.getSpecificMessage(messageId);
    String result = "";
    // Get all the Admin's messages
    List<Message> messages = dbHelper.getMessageList(this.currentAdmin.getId());
    // Go through the Admin's messages
    for (Message message : messages) {
      // Check that the message is the Admin's
      if (message.getMessage().equals(specificMessage)) {
        // update viewed status and return the message
        dbHelper.updateUserMessageState(messageId);
        result = specificMessage;
      }
    }
    return result;
  }

  /**
   * View any message with the give messageId.
   * @param messageId the id of the message
   * @return the contents of the message
   */
  public String viewAnyMessage(int messageId) {
    return dbHelper.getSpecificMessage(messageId);
  }

  /**
   * Promotes the given teller to an Admin.
   * @param tellerId id of the teller that is getting promoted
   * @return true if successfull, false otherwise
   */
  public boolean promoteTeller(int tellerId) {
    boolean success = false;

    if (dbHelper.getUserRole(tellerId) == roleMap.getRoleId("TELLER")) {
      success = dbHelper.updateUserRole(tellerId, roleMap.getRoleId("ADMIN"));
    }

    if (success) {
      createMessage(tellerId, "You have been promoted to an administrator.");
    }

    return success;
  }

  /**
   * Creates a message to be sent to the recipient.
   * @param recipientId the id of the recipient
   * @param message the message for the recipient
   * @return the messageid of this message
   */
  public int createMessage(int recipientId, String message) {
    int messageId = -1;
    try {
      messageId = (int)dbHelper.insertMessage(recipientId, message);
    } catch (Exception e) {
        // method will return -1
    }

    return messageId;
  }

  public String getRoleName(int roleId) {
    return dbHelper.getRoleName(roleId);
  }

  public boolean isCustomer(int userId) {
    return (dbHelper.getUserRole(userId) == roleMap.getRoleId("CUSTOMER"));
  }

  /**
   * Gets the total mamount of money in the bank.
   * @return BigDecimal, total amount of money in the bank
   */
  public BigDecimal getTotalMoney() {
    BigDecimal total = new BigDecimal("0");

    for (int userId : dbHelper.getUserIds()) {
      for (int accountId : dbHelper.getUsersAccIds(userId)) {
        BigDecimal accBalance = dbHelper.getBalance(accountId);
        total = total.add(accBalance);
      }
    }

    return total;
  }

  /**
   * Returns the total balance of this customer.
   * @param userId the customer's ID
   * @return the total balance
   */
  public BigDecimal userTotalBalance(int userId) {
    BigDecimal total = new BigDecimal("0");
    List<Integer> accountIds = dbHelper.getUsersAccIds(userId);

    if (accountIds != null) {
      for (int accountId : accountIds) {
        total = total.add(dbHelper.getBalance(accountId));
      }
    }

    return total;
  }
}
