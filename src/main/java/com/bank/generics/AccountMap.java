package com.bank.generics;

import android.content.Context;

import com.bank.database.android.DatabaseHelper;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class AccountMap {
  private static EnumMap<AccountTypes, Integer> map = new EnumMap<>(AccountTypes.class);
  private static AccountMap instance = new AccountMap();
  private static Context myContext;
  private static boolean initialized = false;
  private static List<String> typeNames = new ArrayList<>();

  private AccountMap() {}
  
  /**
   * Gets an instance of the AccountMap.
   * @param context of the activity
   * @return an instance of the AccountMap
   */
  public static AccountMap getInstance(Context context) {
    myContext = context;

    if (!initialized) {
      initializeMap();
      initialized = true;
    }

    return instance;
  }
  
  /**
   * Updates this map to match the account types and account type IDs in the database.
   */
  public static void initializeMap() {
    // Resetting the current map
    map.clear();
    typeNames.clear();

    // Connecting to database
    DatabaseHelper dbHelper = new DatabaseHelper(myContext);

    // Grabbing a list of all the account type IDs from the database
    List<Integer> accTypeIds = dbHelper.getAccountTypeIds();

    for (int typeId : accTypeIds) {
      // Getting the matching key from the AccountTypes enumerator
      String keyName = dbHelper.getAccountTypeName(typeId);
      AccountTypes typeKey = AccountTypes.valueOf(keyName);
      
      // Inserting into map and name list
      typeNames.add(keyName);
      map.put(typeKey, typeId);
    }
  }

  /**
   * Updates this map to match the account types and account type IDs in the database.
   */
  public void updateMap() {
    // Resetting the current map
    map.clear();
    typeNames.clear();

    // Connecting to database
    DatabaseHelper dbHelper = new DatabaseHelper(myContext);

    // Grabbing a list of all the account type IDs from the database
    List<Integer> accTypeIds = dbHelper.getAccountTypeIds();

    for (int typeId : accTypeIds) {
      // Getting the matching key from the AccountTypes enumerator
      String keyName = dbHelper.getAccountTypeName(typeId);
      AccountTypes typeKey = AccountTypes.valueOf(keyName);

      // Inserting into map and name list
      typeNames.add(keyName);
      map.put(typeKey, typeId);
    }
  }


  /**
   * Returns the type ID of an account type in the database.
   * @param typeName the account type
   * @return the type ID of the account type
   */
  public int getTypeId(String typeName) {
    int typeId = -1;
    
    try {
      typeId = map.get(AccountTypes.valueOf(typeName.toUpperCase()));
    } catch (IllegalArgumentException e) {
      // Invalid account type, method will return -1
    }
    
    return typeId;
  }
  
  public boolean containsTypeId(int typeId) {
    return map.containsValue(typeId);
  }

  public List<String> getAccountTypes() {
    return typeNames;
  }
}
