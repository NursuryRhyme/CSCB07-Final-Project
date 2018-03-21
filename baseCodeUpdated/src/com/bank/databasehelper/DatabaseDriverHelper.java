package com.bank.databasehelper;

import com.bank.database.DatabaseDriver;
import com.bank.exceptions.ConnectionFailedException;

import java.sql.Connection;

public class DatabaseDriverHelper extends DatabaseDriver {
  protected static Connection connectOrCreateDataBase() {
    return DatabaseDriver.connectOrCreateDataBase();
  }
  
  public static Connection reInitialize() throws ConnectionFailedException {
    return DatabaseDriver.reInitialize();
  }
}
