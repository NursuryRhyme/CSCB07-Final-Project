package com.bank.users;

public interface User {

  public int getId();
  
  public void setId(int id);
  
  public String getName();
  
  public void setName(String name);
  
  public int getAge();
  
  public void setAge(int age);
  
  public void setAddress(String address);
  
  public String getAddress();
  
  public int getRoleId() ;
  
  /**
   * Authenticate user using a password.
   * @param password the input password
   * @return true if the password matches the user's password, false otherwise
   */
  public boolean authenticated(String password);
}
