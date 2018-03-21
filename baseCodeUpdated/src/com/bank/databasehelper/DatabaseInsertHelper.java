package com.bank.databasehelper;

import com.bank.accounts.Account;
import com.bank.database.DatabaseInsertException;
import com.bank.database.DatabaseInserter;
import com.bank.exceptions.IllegalAgeException;
import com.bank.exceptions.IllegalAmountException;
import com.bank.generics.AccountMap;
import com.bank.generics.AccountTypes;
import com.bank.generics.RoleMap;
import com.bank.generics.Roles;
import com.bank.users.User;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;


public class DatabaseInsertHelper extends DatabaseInserter {
  /**
   * Inserts an account into the database.
   * @param name the name of the account
   * @param balance the balance of the account with 2 decimal places
   * @param typeId a valid type ID from one of the AccountTypes
   * @return the accountId of the account if it was inserted, -1 otherwise
   */
  public static int insertAccount(String name, BigDecimal balance, int typeId) {
    // Return variable
    int ret = -1;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      if (!name.isEmpty() && balance.scale() == 2 
          && AccountMap.getInstance().containsTypeId(typeId)) {
        ret = DatabaseInserter.insertAccount(name, balance, typeId, connection);
      }
    } catch (Exception e) {
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
    
    return ret;
  }
  
  /**
   * Inserts an account type in the database.
   * @param name the name of the account type from the AccountType enumerator
   * @param interestRate the interest rate of the account type between 0 and 1
   * @return the id of the account type, -1 otherwise
   */
  public static int insertAccountType(String name, BigDecimal interestRate) {
    // Return variable
    int ret = -1;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      // Will throw an exception if name isn't in AccountTypes enumerator
      AccountTypes.valueOf(name.toUpperCase());
      
      // Checking if the interest rate is valid and the account type isn't already in the database
      if (interestRate.compareTo(new BigDecimal("1")) == -1 
          && interestRate.compareTo(new BigDecimal("0")) >= 0 && !hasDuplicateType(name)) {
        try {
          ret = DatabaseInserter.insertAccountType(name.toUpperCase(), interestRate, connection);
        } catch (DatabaseInsertException e) {
          e.printStackTrace();
        }
      }
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid role name");
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return ret;
  }
  
  /**
   * Inserts a new user into the database.
   * @param name name of the user
   * @param age integer age of the user
   * @param address address of the user with a 100 character limit
   * @param roleId an ID from the role table
   * @param password user password
   * @return the ID of the user if it was inserted, -1 otherwise
   */
  public static int insertNewUser(String name, int age, String address, int roleId,
      String password) throws IllegalAgeException {
    // Return variable
    int ret = -1;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      //List<Integer> roleIds = DatabaseSelectHelper.getRoles();
      
      // Verifying valid input values
      if (age < 0) {
        throw new IllegalAgeException();
      }
      
      if (!name.isEmpty() && address.length() <= 100 
          && RoleMap.getInstance().containsRoleId(roleId) && !password.isEmpty()) {
        ret = DatabaseInserter.insertNewUser(name, age, address, roleId, password, connection);
      }
    } catch (IllegalAgeException e) {
      throw e;
    } catch (Exception e) {
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
    
    return ret;
  }
  
  /**
   * Inserts a role into the database.
   * @param role a role from the Roles enumerator
   * @return the role id of the new role, -1 otherwise
   */
  public static int insertRole(String role) {
    int ret = -1;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      // Will throw an exception if role is not in the Roles enumerator
      Roles.valueOf(role.toUpperCase());
      
      // Inserting the user if exception was not thrown and the role is not already in the database
      if (!hasDuplicateRole(role)) {
        try {
          ret = DatabaseInserter.insertRole(role.toUpperCase(), connection);
        } catch (DatabaseInsertException e) {
          e.printStackTrace();
        }
      }
    } catch (IllegalArgumentException e) {
      System.out.println("Invalid role name");
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return ret;
  }
  
  /**
   * Inserts a user account into the database.
   * @param userId the ID of a user in the Users table of the database
   * @param accountId the ID of an account in the Accounts table in the database
   * @return the ID of the new UserAccount, -1 otherwise
   */
  public static int insertUserAccount(int userId, int accountId) {
    int ret = -1;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      // Storing the user and account object to check if it exists later
      User user = DatabaseSelectHelper.getUserDetails(userId);
      Account account = DatabaseSelectHelper.getAccountDetails(accountId);
      
      // Checking if the user and account exists
      if (user != null && account != null) {
        List<Integer> accountIds = DatabaseSelectHelper.getAccountIds(userId);
        // Insert if the account is not already in the user account
        if (!accountIds.contains(accountId)) {
          ret = DatabaseInserter.insertUserAccount(userId, accountId, connection);
        }
      }
    } catch (Exception e) {
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
    
    return ret;
  }

  /**
   * Insert a new message into the database.
   * @param userId the id of the user whom the message is for.
   * @return the id of the inserted message.
   * @throws IllegalAmountException thrown if the length of the massage is over 512.
   */
  public static int insertMessage(int userId, String message) throws IllegalAmountException {
    // check if the length is at max 512
    if (message.length() > 512) {
      throw new IllegalAmountException();
    }
    // insert a new message
    Connection connection = null;
    int id = -1;
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      id = DatabaseInsertHelper.insertMessage(userId, message, connection);
      
    // catch if it threw exception and close connection
    } catch (DatabaseInsertException e) {
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
    return id;
  }
  
  private static boolean hasDuplicateRole(String role) {
    boolean ret = false;
    String roleStr = "";
    
    for (int roleId : DatabaseSelectHelper.getRoles()) {
      roleStr = DatabaseSelectHelper.getRole(roleId);
      if (roleStr.equals(role.toUpperCase())) {
        ret = true;
      }
    }
    
    return ret;
  }
  
  private static boolean hasDuplicateType(String type) {
    boolean ret = false;
    String typeStr = "";
    
    for (int typeId : DatabaseSelectHelper.getAccountTypesIds()) {
      typeStr = DatabaseSelectHelper.getAccountTypeName(typeId);
      if (typeStr.equals(type.toUpperCase())) {
        ret = true;
      }
    }
    
    return ret;
  }
}
