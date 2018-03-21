package com.bank.generics;

import com.bank.databasehelper.DatabaseSelectHelper;
import java.util.EnumMap;
import java.util.List;

public class AccountMap {
  private EnumMap<AccountTypes, Integer> map = new EnumMap<>(AccountTypes.class);
  private static AccountMap instance = new AccountMap();

  private AccountMap() {
    updateMap();
  }
  
  public static AccountMap getInstance() {
    return instance;
  }
  
  /**
   * Updates this map to match the account types and account type IDs in the database.
   */
  public void updateMap() {
    // Resetting the current map
    map.clear();

    // Grabbing a list of all the account type IDs from the database
    List<Integer> accTypeIds = DatabaseSelectHelper.getAccountTypesIds();

    for (int typeId : accTypeIds) {
      // Getting the matching key from the AccountTypes enumerator
      String keyName = DatabaseSelectHelper.getAccountTypeName(typeId);
      AccountTypes typeKey = AccountTypes.valueOf(keyName);
      
      // Inserting into map
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
}
