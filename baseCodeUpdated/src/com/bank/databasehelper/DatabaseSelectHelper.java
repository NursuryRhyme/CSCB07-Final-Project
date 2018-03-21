package com.bank.databasehelper;

import com.bank.accounts.Account;
import com.bank.accounts.BalanceOwingAccount;
import com.bank.accounts.ChequingAccount;
import com.bank.accounts.RestrictedSavingsAccount;
import com.bank.accounts.SavingsAccount;
import com.bank.accounts.Tfsa;
import com.bank.database.DatabaseSelector;
import com.bank.messages.Message;
import com.bank.users.Admin;
import com.bank.users.Customer;
import com.bank.users.Teller;
import com.bank.users.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSelectHelper extends DatabaseSelector {
  /**
   * Gets the String representation of a role in the database.
   * @param id the id of the role
   * @return the String representation of the role
   */
  public static String getRole(int id) {
    // Return variable
    String role = "";
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      role = DatabaseSelector.getRole(id, connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return role;
  }
  
  /**
   * Gets the password of a user in the database.
   * @param userId ID of the user
   * @return the password
   */
  public static String getPassword(int userId) {
    // Return variable
    String hashPassword = "";
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      hashPassword = DatabaseSelector.getPassword(userId, connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return hashPassword;
  }
  
  /**
   * Returns a list of the user IDs in the database.
   * @return the list
   */
  public static List<Integer> getUsers() {
    // Return variable
    Connection connection = null;
    List<Integer> userIds = new ArrayList<Integer>();
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      ResultSet results = DatabaseSelector.getUsersDetails(connection);
      
      while (results.next()) {
        userIds.add(results.getInt("ID"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return userIds;
  }
  
  /**
   * Get a User object representation of a user in the database.
   * @param userId the ID of the user
   * @return the User
   */
  public static User getUserDetails(int userId) {
    // Return variable
    User user = null;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      ResultSet results = DatabaseSelector.getUserDetails(userId, connection);
      
      // Creating the User
      while (results.next()) {
        switch (DatabaseSelectHelper.getRole(results.getInt("ROLEID"))) {
          case "ADMIN":
            user = new Admin(
                userId, 
                results.getString("NAME"), 
                results.getInt("AGE"), 
                results.getString("ADDRESS"));
            break;
          case "TELLER":
            user = new Teller(
                userId, 
                results.getString("NAME"), 
                results.getInt("AGE"), 
                results.getString("ADDRESS"));
            break;
          case "CUSTOMER":
            user = new Customer(
                userId, 
                results.getString("NAME"), 
                results.getInt("AGE"), 
                results.getString("ADDRESS"));
            break;
          default:
            // SQLException would have been thrown if an ID was invalid
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return user;
  }
 
  /**
   * Gets the account IDs of a user in the database.
   * @param userId the ID of the user
   * @return a list of the account IDs of the user
   */
  public static List<Integer> getAccountIds(int userId) {
    // Return variable
    List<Integer> accountIds = new ArrayList<Integer>();
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // I changed this to getAccountsIds from getUserDetails
      ResultSet results = DatabaseSelector.getAccountIds(userId, connection);
      
      while (results.next()) {
        accountIds.add(results.getInt("ACCOUNTID"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return accountIds;
  }
  
  /**
   * Get an Account object representing an account in the database.
   * @param accountId the ID of the account
   * @return the Account object
   */
  public static Account getAccountDetails(int accountId) {
    Account account = null;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      ResultSet results = DatabaseSelector.getAccountDetails(accountId, connection);

      // Creating the Account object
      while (results.next()) {
        switch (DatabaseSelectHelper.getAccountTypeName(results.getInt("TYPE"))) {
          case "CHEQUING":
            account = new ChequingAccount(
                accountId, 
                results.getString("NAME"), 
                new BigDecimal(results.getString("BALANCE")));
            break;
          case "SAVING":
            account = new SavingsAccount(
                accountId, 
                results.getString("NAME"), 
                new BigDecimal(results.getString("BALANCE")));
            break;
          case "TFSA":
            account = new Tfsa(
                accountId, 
                results.getString("NAME"), 
                new BigDecimal(results.getString("BALANCE")));
            break;
          case "RESTRICTEDSAVING":
            account = new RestrictedSavingsAccount(
                accountId,
                results.getString("NAME"),
                new BigDecimal(results.getString("BALANCE")));
            break;
          case "BALANCEOWING":
            account = new BalanceOwingAccount(
                accountId,
                results.getString("NAME"),
                new BigDecimal(results.getString("BALANCE")));
            break;
          default:
            // SQLException would have been thrown if an ID was invalid
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return account;
  }
  
  /**
  * Returns the balance of an account in the database.
  * @param accountId the ID of the account
  * @return the balance of the account
  */
  public static BigDecimal getBalance(int accountId) {
    BigDecimal balance = null;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Getting the balance
      balance = DatabaseSelector.getBalance(accountId, connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return balance;
  }
 
  /**
   * Gets the interest rate of an account type in the database.
   * @param accountType the ID of the account type
   * @return the interest rate
   */
  public static BigDecimal getInterestRate(int accountType) {
    BigDecimal interestRate = null;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      interestRate = DatabaseSelector.getInterestRate(accountType, connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return interestRate;
  }
  
  /**
   * Returns the account type IDs in the database.
   * @return a list of the account type IDs
   */
  public static List<Integer> getAccountTypesIds() {
    List<Integer> ids = new ArrayList<Integer>();
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      ResultSet results = DatabaseSelector.getAccountTypesId(connection);
      
      while (results.next()) {
        ids.add(results.getInt("ID"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return ids;
  }
  
  /**
   * Gets the name of an account type in the database.
   * @param accountTypeId the ID of the account type
   * @return the name of the account type
   */
  public static String getAccountTypeName(int accountTypeId) {
    String name = "";
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      name = DatabaseSelector.getAccountTypeName(accountTypeId, connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return name;
  }
  
  /**
   * Returns the roles in the database.
   * @return the roles
   */
  public static List<Integer> getRoles() {
    List<Integer> roles = new ArrayList<>();
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      ResultSet results = DatabaseSelector.getRoles(connection);
      
      while (results.next()) {
        roles.add(results.getInt("ID"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return roles;
  }

  /**
   * Returns the account type of an account in the database.
   * @param accountId the ID of the account
   * @return the account type ID if it is in the database, -1 otherwise
   */
  public static int getAccountType(int accountId) {
    int accountType = -1;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      accountType = DatabaseSelector.getAccountType(accountId, connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return accountType;
  }
  
  /**
   * Returns the role of a user in the database.
   * @param userId the ID of the user
   * @return the role of the user if it is in the database, -1 otherwise
   */
  public static int getUserRole(int userId) {

    int role = -1;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      role = DatabaseSelector.getUserRole(userId, connection);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return role;

  }


  /**
   * Returns the name of an account in the database.
   * @param accountId the ID of the account
   * @return the name of the account
   */
  public static String getAccountName(int accountId) {
    String accountName = null;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      ResultSet results = DatabaseSelector.getAccountDetails(accountId, connection);
    
      // Getting the account name from the result set
      while (results.next()) {
        accountName = results.getString("NAME");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return accountName;
  }
  
  /**
   * Get all messages currently available to a user.
   * @param userId the user whose messages are being retrieved.
   * @return a result set containing all messages.
   */
  public static List<Message> getAllMessages(int userId) {
    ArrayList<Message> messages = new ArrayList<>();
    Connection connection = null;
    try {
      // get all messages
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      ResultSet results = DatabaseSelector.getAllMessages(userId, connection);

      // add messages to list
      while (results.next()) {
        Message message = new Message(
            results.getString("MESSAGE"), 
            results.getInt("ID"), 
            results.getInt("USERID"), 
            results.getInt("VIEWED"));
        messages.add(message);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return messages;
  }
  
  /**
   * Get a specific message from the database.
   * @param messageId the id of the message.
   * @return the message from the database as a string.
   */
  public static String getSpecificMessage(int messageId) {
    String message = "";
    Connection connection = null;
    try {
      // get message
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      message = DatabaseSelector.getSpecificMessage(messageId, connection);
    // catch exception and close connection
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return message;
  }
}
