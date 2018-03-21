package com.bank.users;

import android.content.Context;

public class Admin extends UserImpl {
  // The name of the role of this user
  private final String role = "ADMIN";
  
  /**
   * Creates an Admin object.
   * @param id the ID of the user
   * @param name the name of the user
   * @param age the age of the user
   * @param address the address of the user
   */
  public Admin(Context context, int id, String name, int age, String address) {
    super(context, id, name, age, address);
    this.setRoleId(role);
  }
  
  /**
   * Creates an Admin object.
   * @param id the ID of the user
   * @param name the name of the user
   * @param age the age of the user
   * @param address the address of the user
   * @param authenticated true if the user is authenticated, false otherwise
   */
  public Admin(Context context, int id, String name, int age,
      String address, boolean authenticated) {
    super(context, id, name, age, address, authenticated);
    this.setRoleId(role);
  }
}
