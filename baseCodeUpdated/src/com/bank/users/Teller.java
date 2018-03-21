package com.bank.users;

public class Teller extends UserImpl {
  // The name of the role of this user
  private final String role = "TELLER";
  
  /**
   * Creates a Teller object.
   * @param id the ID of the user
   * @param name the name of the user
   * @param age the age of the user
   * @param address the address of the user
   */
  public Teller(int id, String name, int age, String address) {
    super(id, name, age, address);
    this.setRoleId(role);
  }
  
  /**
   * Creates a Teller object.
   * @param id the ID of the user
   * @param name the name of the user
   * @param age the age of the user
   * @param address the address of the user
   * @param authenticated true if user is authenticated, false otherwise
   */
  public Teller(int id, String name, int age, String address, boolean authenticated) {
    super(id, name, age, address, authenticated);
    this.setRoleId(role);
  }
}
