
package com.bank.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bank.R;
import com.bank.activities.helpers.AccountFunc;
import com.bank.activities.helpers.AdminFunc;
import com.bank.activities.helpers.MessageFunc;
import com.bank.activities.helpers.MoneyFunc;
import com.bank.activities.helpers.TellerFunc;
import com.bank.database.android.DatabaseHelper;
import com.bank.database.android.DatabaseSerializer;
import com.bank.generics.RoleMap;
import com.bank.userinterfaces.AdminTerminal;
import com.bank.userinterfaces.Atm;
import com.bank.userinterfaces.TellerTerminal;
import com.bank.users.User;

public class MenuActivity extends Activity {

  ListView menuList;
  String[] menuItems;
  int userRole;
  int userId;
  String userPw;
  DatabaseHelper dbHelper;
  TellerTerminal tellerTerminal;
  AdminTerminal adminTerminal;
  Atm atm;
  // region CONSTANTS
  static final int ADMIN_CREATE_TELLER = 0;
  static final int ADMIN_CREATE_ADMIN = 1;
  static final int ADMIN_VIEW_USERS = 2;
  static final int ADMIN_VIEW_ACCOUNTS = 3;
  static final int ADMIN_VIEW_TOTAL_MONEY = 4;
  static final int ADMIN_PROMOTE_TELLER = 5;
  static final int ADMIN_SERIALIZE_DATABASE = 6;
  static final int ADMIN_DESERIALIZE_DATABASE = 7;
  static final int ADMIN_VIEW_MESSAGE = 8;
  static final int ADMIN_LEAVE_MESSAGE = 9;
  static final int ADMIN_ANY_MESSAGE = 10;
  static final int ADMIN_USER_TOTAL = 11;


  static final int TT_AUTH_NEW_CUSTOMER = 0;
  static final int TT_MAKE_NEW_CUSTOMER = 1;
  static final int TT_MAKE_NEW_ACCOUNT = 2;
  static final int TT_GIVE_INTEREST = 3;
  static final int TT_MAKE_DEPOSIT = 4;
  static final int TT_MAKE_WITHDRAW = 5;
  static final int TT_CHECK_BALANCE = 6;
  static final int TT_LIST_ACCOUNT = 7;
  static final int TT_UPDATE_CUSTOMER = 8;
  static final int TT_CLOSE_CUSTOMER_SESSION = 9;
  static final int TT_VIEW_MY_MESSAGES = 10;
  static final int TT_VIEW_CUSTOMER_MESSAGE = 11;
  static final int TT_LEAVE_CUSTOMER_MESSAGE = 12;
  static final int TT_CUSTOMER_TOTAL_BALANCE = 13;

  static final int ATM_LIST_ACCOUNTS = 0;
  static final int ATM_MAKE_DEPOSIT = 1;
  static final int ATM_CHECK_BALANCE = 2;
  static final int ATM_MAKE_WITHDRAWAL = 3;
  static final int ATM_VIEW_MESSAGES = 4;
  static final int ATM_TRANSFER_MONEY = 5;
  // endregion

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    userRole = intent.getIntExtra("userRole", -1);
    userId = intent.getIntExtra("userId", -1);
    userPw = intent.getStringExtra("userPw");
    this.dbHelper = new DatabaseHelper(this);

    // Show menu
    setContentView(R.layout.activity_terminal_menu);
    TextView titleView = (TextView) findViewById(R.id.terminal_title);

    // Loading menu content
    Resources resources = this.getResources();

    if (userRole == RoleMap.getInstance(this).getRoleId("ADMIN")) {
      titleView.setText("Admin Terminal");
      adminTerminal = new AdminTerminal(this, userId, userPw);
      menuItems = resources.getStringArray(R.array.adminTerminalItems);
    } else if (userRole == RoleMap.getInstance(this).getRoleId("TELLER")) {
      titleView.setText("Teller Terminal");
      tellerTerminal = new TellerTerminal(this, userId, userPw);
      menuItems = resources.getStringArray(R.array.tellerTerminalItems);

      // Setting subtext
      TextView subtext = (TextView) findViewById(R.id.terminal_sub_text);
      subtext.setVisibility(View.VISIBLE);
      subtext.setText("No customer authenticated");
    } else if (userRole == RoleMap.getInstance(this).getRoleId("CUSTOMER")) {
      titleView.setText("ATM");
      atm = new Atm(this, userId, userPw);
      menuItems = resources.getStringArray(R.array.atmTerminalItems);

      // Setting subtext
      TextView subtext = (TextView) findViewById(R.id.terminal_sub_text);
      TextView subSubtext = (TextView) findViewById(R.id.terminal_sub_text_2);
      subtext.setVisibility(View.VISIBLE);
      subSubtext.setVisibility((View.VISIBLE));
      subtext.setText("Welcome, " + atm.getCustomer().getName() + ".");
      subSubtext.setText("Your address: " + atm.getCustomer().getAddress());
    }

    menuList = (ListView) findViewById(R.id.menuList);
    menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (userRole) {
          case 1:
            handleAdminActions(i);
            break;
          case 2:
            handleTellerActions(i);
            break;
          case 3:
            handleAtmActions(i);
            break;
          default:
            // no other options
        }
        // Toast.makeText(MenuActivity.this, Integer.toString(i)  ,Toast.LENGTH_SHORT).show();
      }
    });
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.activity_terminal_item,
            R.id.textView,  menuItems);
    menuList.setAdapter(arrayAdapter);
  }

  private void handleAtmActions(int i) {
    Log.i("me", "Handle ATM Action " + Integer.toString(i));
    MoneyFunc moneyFunction;
    AccountFunc accountFunction;
    MessageFunc messageFunction;

    switch (i) {
      case ATM_CHECK_BALANCE:
        moneyFunction = new MoneyFunc(this, atm);
        moneyFunction.checkBalance();
        break;
      case ATM_LIST_ACCOUNTS:
        accountFunction = new AccountFunc(this, atm, userId, userPw);
        accountFunction.listAccounts(userId);
        break;
      case ATM_MAKE_DEPOSIT:
        moneyFunction = new MoneyFunc(this, atm);
        moneyFunction.makeDeposit(userId, userPw);
        break;
      case ATM_MAKE_WITHDRAWAL:
        moneyFunction = new MoneyFunc(this, atm);
        moneyFunction.makeWithdrawal(userId, userPw);
        break;
      case ATM_VIEW_MESSAGES:
        messageFunction = new MessageFunc(this, atm, userId, userPw);
        messageFunction.listMyMessage();
        break;
      case ATM_TRANSFER_MONEY:
        moneyFunction = new MoneyFunc(this, atm);
        moneyFunction.transferMoney(userId, userPw);
        break;
      default:
        // no other options
    }
  }

  private void handleAdminActions(int i) {
    AdminFunc adminFunc = new AdminFunc(adminTerminal, this);
    RoleMap roleMap = RoleMap.getInstance(this);
    MessageFunc messageFunctions;
    AccountFunc accountFunctions;
    DatabaseSerializer dbSer;
    Toast toast;
    boolean success;

    switch (i) {
      case ADMIN_CREATE_ADMIN:
        adminFunc.createUser(this, roleMap.getRoleId("ADMIN"), userId, userPw);
        break;
      case ADMIN_CREATE_TELLER:
        adminFunc.createUser(this, roleMap.getRoleId("TELLER"), userId, userPw);
        break;
      case ADMIN_DESERIALIZE_DATABASE:
        User currentAdmin = adminTerminal.getCurrentAdmin();
        toast = Toast.makeText(this, "Deserializing...", Toast.LENGTH_SHORT);
        toast.show();
        dbSer = new DatabaseSerializer(this);
        success = dbSer.deserializeDatabase();
        if (success) {
          // Checking if current admin is in database and adding if not
          int adminCheck = dbSer.checkAdmin(currentAdmin, userPw);
          if (adminCheck == 0) {
            toast = Toast.makeText(this, "Database deserialized", Toast.LENGTH_SHORT);
            toast.show();
          } else {
            // Notifying admin of his new user ID if he was reinserted into the db
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Database deserialized!\nYour new user ID is: " + adminCheck
                    + "\nYour password is unchanged.")
                    .setTitle("Success")
                    .setNegativeButton("OK", null).create();

            AlertDialog dialog = builder.create();
            dialog.show();
          }
        } else {
          toast = Toast.makeText(this, "Deserialization failed", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case ADMIN_ANY_MESSAGE:
        messageFunctions = new MessageFunc(this, adminTerminal, userId, userPw);
        messageFunctions.viewAnyMessage();
        break;
      case ADMIN_PROMOTE_TELLER:
        adminFunc.promoteTeller();
        break;
      case ADMIN_SERIALIZE_DATABASE:
        toast = Toast.makeText(this, "Serializing...", Toast.LENGTH_SHORT);
        toast.show();
        dbSer = new DatabaseSerializer(this);
        success = dbSer.serializeDatabase();
        if (success) {
          toast = Toast.makeText(this, "Database serialized", Toast.LENGTH_SHORT);
          toast.show();
        } else {
          toast = Toast.makeText(this, "Serialization failed", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case ADMIN_VIEW_ACCOUNTS:
        accountFunctions = new AccountFunc(this, adminTerminal, userId, userPw);
        accountFunctions.listAccounts();
        break;
      case ADMIN_VIEW_TOTAL_MONEY:
        adminFunc.viewTotalMoney();
        break;
      case ADMIN_VIEW_USERS:
        adminFunc.viewUsers(userId, userPw);
        break;
      case ADMIN_VIEW_MESSAGE:
        messageFunctions = new MessageFunc(this, adminTerminal, userId, userPw);
        messageFunctions.listMyMessage();
        break;
      case ADMIN_LEAVE_MESSAGE:
        messageFunctions = new MessageFunc(this, adminTerminal, userId, userPw);
        messageFunctions.leaveMessage();
        break;
      case ADMIN_USER_TOTAL:
        adminFunc.userTotalBalance();
        break;
      default:
        // no other options
    }
  }

  private void handleTellerActions(int i) {
    AdminFunc adminFunc = new AdminFunc();
    TellerFunc tellerFunc = new TellerFunc();
    MessageFunc messageFunctions;
    MoneyFunc moneyFunction;

    switch (i) {
      case TT_AUTH_NEW_CUSTOMER:
        tellerFunc.authenticateCustomer(this, tellerTerminal);
        break;
      case TT_CHECK_BALANCE:
        if (tellerTerminal.getCurrentCustomer() != null) {
          tellerFunc.checkBalance(this, tellerTerminal);
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_CLOSE_CUSTOMER_SESSION:
        if (tellerTerminal.getCurrentCustomer() != null) {
          tellerFunc.closeSession(this, tellerTerminal);
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_GIVE_INTEREST:
        if (tellerTerminal.getCurrentCustomer() != null) {
          if (tellerTerminal.getCurrentCustomer() != null) {
            int customerId = tellerTerminal.getCurrentCustomer().getId();
            tellerFunc.giveInterest(this, userId, userPw, customerId);
          } else {
            Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
            toast.show();
          }
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_LEAVE_CUSTOMER_MESSAGE:
        tellerFunc.leaveCustomerMessage(this, tellerTerminal);
        break;
      case TT_LIST_ACCOUNT:
        if (tellerTerminal.getCurrentCustomer() != null) {
          if (tellerTerminal.getCurrentCustomer() != null) {
            int customerId = tellerTerminal.getCurrentCustomer().getId();
            tellerFunc.listAccounts(this, userId, userPw, customerId);
          } else {
            Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
            toast.show();
          }
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_MAKE_DEPOSIT:
        if (tellerTerminal.getCurrentCustomer() != null) {
          int customerId = tellerTerminal.getCurrentCustomer().getId();
          tellerFunc.makeDeposit(this, userId, userPw, customerId);
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_MAKE_NEW_ACCOUNT:
        if (tellerTerminal.getCurrentCustomer() != null) {
          tellerFunc.createAccount(this, tellerTerminal);
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_MAKE_NEW_CUSTOMER:
        int customerRoleId = RoleMap.getInstance(this).getRoleId("CUSTOMER");
        // this returns a user ID, but it's not needed here
        adminFunc.createUser(this, customerRoleId, userId, userPw);
        //Customer newCustomerObj = (Customer) dbHelper.getUserObject(newUserId);
        //tellerTerminal.setCurrentCustomer(newCustomerObj);
        break;
      case TT_MAKE_WITHDRAW:
        if (tellerTerminal.getCurrentCustomer() != null) {
          int customerId = tellerTerminal.getCurrentCustomer().getId();
          tellerFunc.makeWithdrawal(this, userId, userPw, customerId);
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_VIEW_CUSTOMER_MESSAGE:
        messageFunctions = new MessageFunc(this, tellerTerminal, userId, userPw);
        if (messageFunctions.checkCustomerStatus()) {
          messageFunctions.listCustomerMessage();
        }
        break;
      case TT_VIEW_MY_MESSAGES:
        messageFunctions = new MessageFunc(this, tellerTerminal, userId, userPw);
        messageFunctions.listMyMessage();
        break;
      case TT_UPDATE_CUSTOMER:
        if (tellerTerminal.getCurrentCustomer() != null) {
          tellerFunc.updateUserInfo(this, tellerTerminal);
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      case TT_CUSTOMER_TOTAL_BALANCE:
        if (tellerTerminal.getCurrentCustomer() != null) {
          moneyFunction = new MoneyFunc(this, tellerTerminal);
          moneyFunction.checkTotalBalance(userId, userPw);
        } else {
          Toast toast = Toast.makeText(this, "No customer authenticated", Toast.LENGTH_SHORT);
          toast.show();
        }
        break;
      default:
        // no other options
    }
  }
}
