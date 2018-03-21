package com.bank.databasehelper;

import com.bank.databasehelper.BankData.AccTypeSer;
import com.bank.databasehelper.BankData.AccountSer;
import com.bank.databasehelper.BankData.RoleSer;
import com.bank.databasehelper.BankData.UserAccSer;
import com.bank.databasehelper.BankData.UserMsgSer;
import com.bank.databasehelper.BankData.UserSer;
import com.bank.exceptions.ConnectionFailedException;
import com.bank.exceptions.IllegalAgeException;
import com.bank.generics.AccountMap;
import com.bank.generics.RoleMap;
import com.bank.users.User;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;


public class DatabaseSerializer {
  /**
   * Serializes the database into database_copy.
   * @return true if database was serialized, false otherwise
   */
  public static boolean serializeDatabase() {
    try {
      ObjectOutputStream outputStream;
      outputStream = new ObjectOutputStream(new FileOutputStream("database_copy.ser"));
      BankData outputObj = new BankData();
      outputStream.writeObject(outputObj);
      outputStream.close();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    
    return true;
  }
  
  /**
   * Deserializes database_copy.ser
   * @throws FileNotFoundException if database_copy.ser cannot be found
   * @throws IOException if an I/O error occurs
   * @throws ClassNotFoundException if class of a serialized object cannot be found.
   * @throws ConnectionFailedException if connection to the database fails
   */
  public static boolean deserializeDatabase() {
    // Backing up the database
    copyDatabase("bank", "bank-backup");
    
    try {
      ObjectInputStream inputStream = new ObjectInputStream(
          new FileInputStream("database_copy.ser"));
      BankData bankData = (BankData) inputStream.readObject();
      inputStream.close();
      
      // Clearing the existing database
      Connection connection = null;
      try {
        connection = DatabaseDriverHelper.reInitialize();
      } finally {
        connection.close();
      }
      
      // Adding account types and roles to the database
      for (AccTypeSer accTypeSer : bankData.accountTypes) {
        DatabaseInsertHelper.insertAccountType(accTypeSer.name, accTypeSer.interestRate);
      }
      
      for (RoleSer roleSer : bankData.roles) {
        DatabaseInsertHelper.insertRole(roleSer.name);
      }
      
      // Updating the enum maps
      AccountMap.getInstance().updateMap();
      RoleMap.getInstance().updateMap();
      
      // Adding users and their password to the database
      for (UserSer userSer : bankData.users) {
        try { 
          // Getting the role ID of the user's role in case it changed
          int roleId = getNewRoleId(userSer.roleId, bankData);
          
          DatabaseInsertHelper.insertNewUser(userSer.name, userSer.age, userSer.address, 
              roleId, "UNDEFINED");
          DatabaseUpdateHelper.updateUserPassword(userSer.password, userSer.id);
        } catch (IllegalAgeException e) {
          // age is always valid because it came from another database
        }
      }
  
      // Inserting accounts into the database
      for (AccountSer accSer : bankData.accounts) {
        String name = accSer.name;
        BigDecimal balance = accSer.balance;
        // Getting the type ID of the account's type in case it was changed
        int typeId = getNewTypeId(accSer.type, bankData);
        
        DatabaseInsertHelper.insertAccount(name, balance, typeId);
      }
      
      // Inserting user accounts to the database
      for (UserAccSer userAccSer : bankData.userAccounts) {
        int userId = userAccSer.userId;
        int accountId = userAccSer.accountId;
        
        DatabaseInsertHelper.insertUserAccount(userId, accountId);
      }
      
      // Inserting user messages to the database
      for (UserMsgSer userMsgSer : bankData.userMessages) {
        int userId = userMsgSer.userId;
        String message = userMsgSer.message;
        int viewed = userMsgSer.viewed;
        
        DatabaseInsertHelper.insertMessage(userId, message);
        // Updating the viewed status if it was viewed
        if (viewed == 1) {
          DatabaseUpdateHelper.updateUserMessageState(userMsgSer.id);
        }
      }
    } catch (Exception e) {
      copyDatabase("bank-backup", "bank");
      e.printStackTrace();
      return false;
    } finally {
      try {
        Files.delete(Paths.get("bank-backup.db"));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return true;
  }
  
  /**
   * Inserts the admin into the database and returns his/her ID if he was not in the database.
   * Returns 0 if he/she was already in the database, and -1 if he/she could not be inserted.
   * @param user the admin
   * @param password the password of the admin
   * @return new ID of the admin, -1 if insertion failed, 0 if already in database
   */
  public static int checkAdmin(User user, String password) {
    // Find the admin in the database, and return 0 if he is found
    for (int userId : DatabaseSelectHelper.getUsers()) {
      if (userId == user.getId()) {
        return 0;
      }
    }
    
    // Otherwise, insert him into the database
    int newId = -1;
    
    try {
      newId = DatabaseInsertHelper.insertNewUser(user.getName(), user.getAge(), user.getAddress(),
          user.getRoleId(), password);
    } catch (IllegalAgeException e) {
      // Age is legal because it came from another bank database
    }
    
    return newId;
  }
  
  private static int getNewRoleId(int oldRoleId, BankData bankData) {
    RoleMap roleMap = RoleMap.getInstance();
    String roleName = null;
    
    for (RoleSer roleSer : bankData.roles) {
      if (roleSer.id == oldRoleId) {
        roleName = roleSer.name;
        break;
      }
    }
    
    return roleMap.getRoleId(roleName);
  }
  
  private static int getNewTypeId(int oldTypeId, BankData bankData) {
    AccountMap accountMap = AccountMap.getInstance();
    String typeName = null;
    
    for (AccTypeSer typeSer : bankData.accountTypes) {
      if (typeSer.id == oldTypeId) {
        typeName = typeSer.name;
        break;
      }
    }
    
    return accountMap.getTypeId(typeName);
  }
  
  private static boolean copyDatabase(String dbName, String target) {
    Path path = Paths.get(dbName + ".db");
    Path newPath = Paths.get(target + ".db");
    try {
      Files.copy(path, newPath, StandardCopyOption.REPLACE_EXISTING);
      return false;
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return true;
  }
}
