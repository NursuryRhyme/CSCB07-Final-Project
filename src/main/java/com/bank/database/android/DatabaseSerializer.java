package com.bank.database.android;

import android.content.Context;

import com.bank.database.android.BankData.AccTypeSer;
import com.bank.database.android.BankData.AccountSer;
import com.bank.database.android.BankData.RoleSer;
import com.bank.database.android.BankData.UserAccSer;
import com.bank.database.android.BankData.UserMsgSer;
import com.bank.database.android.BankData.UserSer;
import com.bank.generics.AccountMap;
import com.bank.generics.RoleMap;
import com.bank.users.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;


public class DatabaseSerializer {
  private Context context;
  private DatabaseHelper dbHelper;

  public DatabaseSerializer(Context context) {
    this.context = context;
    this.dbHelper = new DatabaseHelper(context);
  }

  /**
   * Serializes the database into database_copy.
   * @return true if database was serialized, false otherwise
   */
  public boolean serializeDatabase() {
    try {
      ObjectOutputStream outputStream;
      File file = new File(context.getFilesDir(), "database_copy.ser");
      FileOutputStream fileOutput = new FileOutputStream(file);
      outputStream = new ObjectOutputStream(fileOutput);
      BankData outputObj = new BankData(context);
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
   */
  public boolean deserializeDatabase() {
    // Backing up the database
    //copyDatabase("bank", "bank-backup");
    
    try {
      File file = new File(context.getFilesDir(), "database_copy.ser");
      ObjectInputStream inputStream = new ObjectInputStream(
          new FileInputStream(file));
      BankData bankData = (BankData) inputStream.readObject();
      inputStream.close();
      
      // Clearing the existing database
      dbHelper.reinitializeDatabase();
      
      // Adding account types and roles to the database
      for (AccTypeSer accTypeSer : bankData.accountTypes) {
        dbHelper.insertAccountType(accTypeSer.name, accTypeSer.interestRate);
      }
      
      for (RoleSer roleSer : bankData.roles) {
        dbHelper.insertRole(roleSer.name);
      }
      
      // Updating the enum maps
      AccountMap.getInstance(context).updateMap();
      RoleMap.getInstance(context).updateMap();
      
      // Adding users and their password to the database
      for (UserSer userSer : bankData.users) {
        // Getting the role ID of the user's role in case it changed
        int roleId = getNewRoleId(userSer.roleId, bankData);

        dbHelper.insertNewUser(userSer.name, userSer.age, userSer.address,
            roleId, "UNDEFINED");
        dbHelper.updateUserPassword(userSer.password, userSer.id);
      }
  
      // Inserting accounts into the database
      for (AccountSer accSer : bankData.accounts) {
        String name = accSer.name;
        BigDecimal balance = accSer.balance;
        // Getting the type ID of the account's type in case it was changed
        int typeId = getNewTypeId(accSer.type, bankData);
        
        dbHelper.insertAccount(name, balance, typeId);
      }
      
      // Inserting user accounts to the database
      for (UserAccSer userAccSer : bankData.userAccounts) {
        int userId = userAccSer.userId;
        int accountId = userAccSer.accountId;
        
        dbHelper.insertUserAccount(userId, accountId);
      }
      
      // Inserting user messages to the database
      for (UserMsgSer userMsgSer : bankData.userMessages) {
        int userId = userMsgSer.userId;
        String message = userMsgSer.message;
        int viewed = userMsgSer.viewed;
        
        dbHelper.insertMessage(userId, message);
        // Updating the viewed status if it was viewed
        if (viewed == 1) {
          dbHelper.updateUserMessageState(userMsgSer.id);
        }
      }
    } catch (Exception e) {
      //copyDatabase("bank-backup", "bank");
      e.printStackTrace();
      return false;
    } finally {
      /*try {
        Files.delete(Paths.get("bank-backup.db"));
      } catch (Exception e) {
        e.printStackTrace();
      }*/
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
  public int checkAdmin(User user, String password) {
    // Find the admin in the database, and return 0 if he is found
    for (int userId : dbHelper.getUserIds()) {
      if (userId == user.getId() && user.authenticated(password)) {
        return 0;
      }
    }
    
    // Otherwise, insert him into the database
    int newId;

    newId = (int) dbHelper.insertNewUser(user.getName(), user.getAge(), user.getAddress(),
        user.getRoleId(), password);
    
    return newId;
  }
  
  private int getNewRoleId(int oldRoleId, BankData bankData) {
    RoleMap roleMap = RoleMap.getInstance(context);
    String roleName = null;
    
    for (RoleSer roleSer : bankData.roles) {
      if (roleSer.id == oldRoleId) {
        roleName = roleSer.name;
        break;
      }
    }
    
    return roleMap.getRoleId(roleName);
  }
  
  private int getNewTypeId(int oldTypeId, BankData bankData) {
    AccountMap accountMap = AccountMap.getInstance(context);
    String typeName = null;
    
    for (AccTypeSer typeSer : bankData.accountTypes) {
      if (typeSer.id == oldTypeId) {
        typeName = typeSer.name;
        break;
      }
    }
    
    return accountMap.getTypeId(typeName);
  }
}
