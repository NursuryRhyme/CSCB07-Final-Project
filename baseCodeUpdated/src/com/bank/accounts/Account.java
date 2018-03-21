package com.bank.accounts;

import java.math.BigDecimal;

public interface Account {

  public int getId();
  
  public void setId(int id);
  
  public String getName();
  
  public void setName(String name);
  
  public BigDecimal getBalance();
  
  public void setBalance(BigDecimal balance);
  
  public int getType();
  
  public void findAndSetInterestRate();
  
  public void addInterest();
  
  public BigDecimal getInterestRate();
}
