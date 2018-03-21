package com.bank.generics;

import com.bank.databasehelper.DatabaseSelectHelper;
import java.util.EnumMap;
import java.util.List;

public class RoleMap {
  private EnumMap<Roles, Integer> map = new EnumMap<>(Roles.class);
  private static RoleMap instance = new RoleMap();
  
  private RoleMap() {
    updateMap();
  }
  
  public static RoleMap getInstance() {
    return instance;
  }

  /**
   * Updates this map to match the roles and role IDs in the database.
   */
  public void updateMap() {
    // Resetting the current map
    map.clear();

    // Grabbing a list of all the role IDs from the database
    List<Integer> roleIds = DatabaseSelectHelper.getRoles();

    for (int roleId : roleIds) {
      // Getting the matching key from the Roles enumerator
      String keyName = DatabaseSelectHelper.getRole(roleId);
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



