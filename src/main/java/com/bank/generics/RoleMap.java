package com.bank.generics;

import android.content.Context;

import com.bank.database.android.DatabaseHelper;
import java.util.EnumMap;
import java.util.List;

public class RoleMap {
  private static EnumMap<Roles, Integer> map = new EnumMap<>(Roles.class);
  private static RoleMap instance = new RoleMap();
  private static Context myContext = null;
  private static boolean initialized = false;
  
  private RoleMap() {}
  
  /**
   * Gets an instance of the RoleMap.
   * @param context of the activity
   * @return an instance of the rolemap
   */
  public static RoleMap getInstance(Context context) {
    myContext = context;

    if (!initialized) {
      initializeMap();
      initialized = true;
    }

    return instance;
  }

  private static void initializeMap() {
    // Resetting the current map
    map.clear();
    DatabaseHelper dbHelper = new DatabaseHelper(myContext);

    // Grabbing a list of all the role IDs from the database
    List<Integer> roleIds = dbHelper.getRoleIds();

    for (int roleId : roleIds) {
      // Getting the matching key from the Roles enumerator
      String keyName = dbHelper.getRoleName(roleId);
      Roles typeKey = Roles.valueOf(keyName);

      // Inserting into map
      map.put(typeKey, roleId);
    }
  }

  /**
   * Updates this map to match the roles and role IDs in the database.
   */
  public void updateMap() {
    // Resetting the current map
    map.clear();
    DatabaseHelper dbHelper = new DatabaseHelper(myContext);

    // Grabbing a list of all the role IDs from the database
    List<Integer> roleIds = dbHelper.getRoleIds();

    for (int roleId : roleIds) {
      // Getting the matching key from the Roles enumerator
      String keyName = dbHelper.getRoleName(roleId);
      Roles typeKey = Roles.valueOf(keyName);
      
      // Inserting into map
      map.put(typeKey, roleId);
    }
  }
  
  /**
   * Returns the role ID of a role in the database.
   * @param roleName the name of the role
   * @return the id of the role
   */
  public int getRoleId(String roleName) {
    int roleId = -1;
    
    try {
      roleId = map.get(Roles.valueOf(roleName.toUpperCase()));
    } catch (IllegalArgumentException e) {
      // Invalid role, method will return -1
    }
    
    return roleId;
  }
  
  public boolean containsRoleId(int roleId) {
    return map.containsValue(roleId);
  }
}



