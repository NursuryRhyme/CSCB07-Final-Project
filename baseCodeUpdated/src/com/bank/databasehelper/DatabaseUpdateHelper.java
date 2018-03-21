package com.bank.databasehelper;

import com.bank.database.DatabaseUpdater;
import com.bank.generics.AccountTypes;
import com.bank.generics.Roles;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;


public class DatabaseUpdateHelper extends DatabaseUpdater {
  
  /**
   * Updates the name of a role in the database.
   * @param name the new role name
   * @param id the ID of the existing role
   * @return true if the name was updated, false otherwise
   */
  public static boolean updateRoleName(String name, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      // Will throw an exception if role is not in the Roles enumerator
      Roles.valueOf(name.toUpperCase());
      
      if (!hasDuplicateRole(name)) {
        complete = DatabaseUpdater.updateRoleName(name, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the name of a user in the database.
   * @param name the new name
   * @param id the ID of the user
   * @return true if the name was updated, false otherwise
   */
  public static boolean updateUserName(String name, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      if (!name.isEmpty()) {
        complete = DatabaseUpdater.updateUserName(name, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the age of a user in the database.
   * @param age the new age
   * @param id the ID of the user
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateUserAge(int age, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      complete = DatabaseUpdater.updateUserAge(age, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the role of a user in the database.
   * @param roleId the ID of the new role
   * @param id the ID of the user
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateUserRole(int roleId, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      List<Integer> roleIds = DatabaseSelectHelper.getRoles();
      
      if (roleIds.contains(roleId)) {
        complete = DatabaseUpdater.updateUserRole(roleId, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the address of a user in the database.
   * @param address the new address
   * @param id the ID of the user
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateUserAddress(String address, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      if (!address.isEmpty()) {
        complete = DatabaseUpdater.updateUserAddress(address, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the password of a user in the database.
   * @param password the new password
   * @param id the ID of the user
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateUserPassword(String password, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      if (!password.isEmpty()) {
        complete = DatabaseUpdater.updateUserPassword(password, id, connection);
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
    
    return complete;
  }

  /**
   * Updates the name of an account in the database.
   * @param name the new account name
   * @param id the ID of the account
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateAccountName(String name, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      if (!name.isEmpty()) {
        complete = DatabaseUpdater.updateAccountName(name, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the account balance of an account in the database.
   * @param balance the new balance
   * @param id the ID of the account
   * @return true if the update was successful, false otherwise
   */
  
  // TODO check if savings less than 1000 and change type to chequing
  public static boolean updateAccountBalance(BigDecimal balance, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      complete = DatabaseUpdater.updateAccountBalance(balance, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the type of an account in the database.
   * @param typeId the ID of the new account type
   * @param id the ID of the account
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateAccountType(int typeId, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      List<Integer> accTypesId = DatabaseSelectHelper.getAccountTypesIds();
      
      // Checking that the account type is in the AccountTypes table
      if (accTypesId != null && accTypesId.contains(typeId)) {
        // get old account type and name
        String oldType = DatabaseSelectHelper.getAccountTypeName(typeId);
        String accountname = DatabaseSelectHelper.getAccountName(id);
        complete = DatabaseUpdater.updateAccountType(typeId, id, connection);

        // get new type of the account
        String newType = DatabaseSelectHelper.getAccountTypeName(typeId);
        // check that it is a different type
        if (!(newType.equals(oldType))) {
          // create message
          String message = "Your " + oldType + " account " + accountname
              + " has been changed to a " + newType;
          // get the user of the account
          List<Integer> users = DatabaseSelectHelper.getUsers();
          for (int userId: users) {
            if (DatabaseSelectHelper.getAccountIds(userId).contains(id)) {
              // leave message for the user
              DatabaseInsertHelper.insertMessage(userId, message);
            }
          }
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
    
    return complete;
  }
  
  /**
   * Updates the name of an account type in the database.
   * @param name the new name
   * @param id the ID of the account type
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateAccountTypeName(String name, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      
      // Will throw exception if name is not in the AccountTypes enumerator
      AccountTypes.valueOf(name.toUpperCase());

      complete = DatabaseUpdater.updateAccountTypeName(name, id, connection);
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
    
    return complete;
  }
  
  /**
   * Updates the interest rate for an account type in the database.
   * @param interestRate the new interest rate
   * @param id the ID of the account type
   * @return true if the update was successful, false otherwise
   */
  public static boolean updateAccountTypeInterestRate(BigDecimal interestRate, int id) {
    boolean complete = false;
    Connection connection = null;
    
    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      // Checking if the interest rate is between 0 and 1
      if (interestRate.compareTo(new BigDecimal("1")) == -1 
          && interestRate.compareTo(new BigDecimal("0")) >= 0) {
        complete = DatabaseUpdater.updateAccountTypeInterestRate(interestRate, id, connection);
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
    
    return complete;
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
  
  /**
   * Update the state of the user message to viewed.
   * @param id the id of the message that has been viewed.
   * @return true if successful, false o/w.
   */
  public static boolean updateUserMessageState(int id) {
    boolean complete = false;
    Connection connection = null;

    try {
      connection = DatabaseDriverHelper.connectOrCreateDataBase();
      complete = updateUserMessageState(id, connection);
    } finally {
      try {
        if (connection != null) {
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return complete;
  }
}
