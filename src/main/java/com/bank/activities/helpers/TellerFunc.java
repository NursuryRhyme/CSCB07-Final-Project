package com.bank.activities.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bank.R;
import com.bank.activities.MenuActivity;
import com.bank.activities.lists.activities.AccountListActivity;
import com.bank.database.android.DatabaseHelper;
import com.bank.generics.AccountMap;
import com.bank.userinterfaces.TellerTerminal;
import com.bank.users.Customer;
import com.bank.users.User;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by byron on 2017-07-29.
 */

public class TellerFunc {
  /**
   * Authenticate customer interface.
   * @param context context where this is used
   * @param terminal the TellerTerminal containing functions and variables
   */
  public void authenticateCustomer(Context context, TellerTerminal terminal) {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText idView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    idView.setHint("Customer ID");
    nameView.setVisibility(View.GONE);
    addrView.setVisibility(View.GONE);

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle("Authenticate customer")
        .setView(makeUserForm)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Cancel", null).create();

    dialog.show();

    // Getting input views and finalizing variables to access in inner methods
    final TextView passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    final TellerTerminal fTerminal = terminal;
    final Context fContext = context;
    final DatabaseHelper dbHelper = new DatabaseHelper(context);

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Getting inputs from TextViews
        int userId;

        try {
          userId = Integer.parseInt(idView.getText().toString());
        } catch (Exception e) {
          Toast toast = Toast.makeText(fContext, "Invalid ID", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }

        String password = passwordView.getText().toString();

        // Attempt to authenticate customer
        boolean success = false; // default
        User user = dbHelper.getUserObject(userId);

        if (user != null && user instanceof Customer) {
          fTerminal.setCurrentCustomer((Customer) user);
          success = fTerminal.authenticateCurrentCustomer(password);
        }

        // Exit dialog on success
        if (success) {
          Toast toast = Toast.makeText(fContext, "User " + userId
              + " authenticated", Toast.LENGTH_SHORT);
          toast.show();

          // Setting subtext
          TextView subtext = (TextView)
                  ((MenuActivity) fContext).findViewById(R.id.terminal_sub_text);
          subtext.setVisibility(View.VISIBLE);
          subtext.setText("Current customer: " + fTerminal.getCurrentCustomer().getName()
                  +  " (ID: " + fTerminal.getCurrentCustomer().getId() + ")");

          dialog.dismiss();
        } else {
          fTerminal.deAuthenticateCustomer();
          Toast toast = Toast.makeText(fContext, "Authentication failed", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });
  }

  /**
   * Create account interface.
   * @param context context where this is used
   * @param terminal the TellerTerminal containing functions and variables
   */
  public void createAccount(Context context, TellerTerminal terminal) {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText balView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    final Spinner typeView = (Spinner) makeUserForm.findViewById(R.id.form_dropdown);
    balView.setHint("Balance");
    balView.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
    typeView.setVisibility(View.VISIBLE);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);

    // Populating the spinner with the current account types
    List<String> accountTypes = AccountMap.getInstance(context).getAccountTypes();
    ArrayAdapter<String> typeAdapter
        = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, accountTypes);
    typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    typeView.setAdapter(typeAdapter);

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle("Create an account")
        .setView(makeUserForm)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Cancel", null).create();

    dialog.show();

    // Getting the input views
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final TellerTerminal fTerminal = terminal;
    final Context fContext = context;


    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Getting inputs from TextViews
        BigDecimal balance;

        // Validating and getting balance input
        try {
          String balanceStr = balView.getText().toString();
          balance = new BigDecimal(balanceStr);

          // Prompt user if balance input is incorrect
          if (balance.scale() > 2) {
            String msg = "Please include 2 or fewer decimal digits in balance";
            Toast toast = Toast.makeText(fContext, msg,
                Toast.LENGTH_SHORT);
            toast.show();
            return;
          }
        } catch (Exception e) {
          // If balance is blank
          Toast toast = Toast.makeText(fContext, "Invalid balance", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }

        String name = nameView.getText().toString();
        String type = typeView.getSelectedItem().toString();
        // Getting the ID of the type using the enum map
        int typeId = AccountMap.getInstance(fContext).getTypeId(type);

        // Attempting to create the account
        long newAccountId = -1;

        // Savings account must have a balance of at least 1000
        if (typeId == AccountMap.getInstance(fContext).getTypeId("SAVING")) {
          if (balance.compareTo(new BigDecimal("1000")) == -1) {
            Toast toast = Toast.makeText(fContext, "Savings account balance must be > 1000",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
          }
        }

        // Validate that the type is valid and attempt to create the account
        if (typeId != -1) {
          newAccountId = fTerminal.makeNewAccount(name, balance.setScale(2,
                  BigDecimal.ROUND_HALF_UP), typeId);
        } else {
          Toast toast = Toast.makeText(fContext, "Invalid account type", Toast.LENGTH_SHORT);
          toast.show();
        }

        // Exit dialog on success
        if (newAccountId != -1) {
          Toast toast = Toast.makeText(fContext, "Account " + newAccountId
              + " created", Toast.LENGTH_SHORT);
          toast.show();
          dialog.dismiss();
        } else {
          Toast toast = Toast.makeText(fContext, "Account creation failed", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });
  }

  /**
   * Check balance interface.
   * @param context context where this is used
   * @param terminal the TellerTerminal containing functions and variables
   */
  public void checkBalance(Context context, TellerTerminal terminal) {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText idView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    idView.setHint("Account ID");
    nameView.setVisibility(View.GONE);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);
    String title = "Check balance";

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setView(makeUserForm)
        .setPositiveButton("Check", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Close", null).create();

    dialog.show();

    // Getting the input views
    final TellerTerminal fTerminal = terminal;
    final Context fContext = context;
    final TextView balanceText = (TextView) makeUserForm.findViewById(R.id.user_info);

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Getting inputs from TextViews
        int accountId;
        BigDecimal balance;

        try {
          // Finding the balance of the account and displaying it
          accountId = Integer.parseInt(idView.getText().toString());
          balance = fTerminal.checkBalance(accountId);
          balanceText.setVisibility(View.VISIBLE);
          balanceText.setText("Balance: $" + balance.toString());
        } catch (Exception e) {
          Toast toast = Toast.makeText(fContext, "Invalid account", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }
      }
    });
  }

  /**
   * Close session function.
   * @param context context where this is used
   * @param tellerTerminal the TellerTerminal containing functions and variables
   */
  public void closeSession(Context context, TellerTerminal tellerTerminal) {
    tellerTerminal.deAuthenticateCustomer();

    // Setting subtext
    TextView subtext = (TextView)
            ((MenuActivity) context).findViewById(R.id.terminal_sub_text);
    subtext.setVisibility(View.VISIBLE);
    subtext.setText("No customer authenticated");

    Toast toast = Toast.makeText(context, "Session closed", Toast.LENGTH_SHORT);
    toast.show();
  }

  /**
   * List accounts interface.
   * @param context context where this is used
   * @param userId id of terminal
   * @param userPw password of terminal
   * @param customerId id of customer
   */
  public void listAccounts(Context context, int userId, String userPw, int customerId) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", customerId);
    intent.putExtra("action", "listAccounts");
    context.startActivity(intent);
  }

  /**
   * Give interest interface.
   * @param context context where this is used
   * @param userId id of terminal
   * @param userPw password of terminal
   * @param customerId id of customer
   */
  public void giveInterest(Context context, int userId, String userPw, int customerId) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", customerId);
    intent.putExtra("action", "giveInterest");
    context.startActivity(intent);
  }

  /**
   * Leave customer message interface.
   * @param context context where this is used
   * @param terminal the TellerTerminal containing functions and variables
   */
  public void leaveCustomerMessage(Context context, TellerTerminal terminal) {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View leaveMessageForm = inflater.inflate(R.layout.leave_message_form, null);
    String title = "Leave a message";

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setView(leaveMessageForm)
        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Close", null).create();

    dialog.show();

    // Finalizing variables to use in inner methods
    final TellerTerminal fTerminal = terminal;
    final Context fContext = context;
    final EditText idView = (EditText) leaveMessageForm.findViewById(R.id.message_userid);
    final EditText messageView = (EditText) leaveMessageForm.findViewById(R.id.message_box);

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int customerId;
        try {
          customerId = Integer.parseInt(idView.getText().toString());
        } catch (Exception e) {
          // If input is blank
          Toast toast = Toast.makeText(fContext, "Invalid ID", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }

        // Attempting to create the message
        String message = messageView.getText().toString();
        int messageId = fTerminal.createMessage(customerId, message);
        if (messageId != -1) {
          Toast toast = Toast.makeText(fContext, "Message " + Integer.toString(messageId)
                  + " sent", Toast.LENGTH_SHORT);
          toast.show();
          dialog.dismiss();
        } else {
          Toast toast = Toast.makeText(fContext, "Invalid ID", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });
  }

  /**
   * Make deposit interface.
   * @param context context where this is used
   * @param userId id of terminal
   * @param userPw password of terminal
   * @param customerId id of customer
   */
  public void makeDeposit(Context context, int userId, String userPw, int customerId) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", customerId);
    intent.putExtra("action", "makeDeposit");
    context.startActivity(intent);
  }

  /**
   * Make withdrawal interface.
   * @param context context where this is used
   * @param userId id of terminal
   * @param userPw password of terminal
   * @param customerId id of customer
   */
  public void makeWithdrawal(Context context, int userId, String userPw, int customerId) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", customerId);
    intent.putExtra("action", "makeWithdrawal");
    context.startActivity(intent);
  }

  /**
   * Update user info interface.
   * @param context context where this is used
   * @param terminal the TellerTerminal containing functions and variables
   */
  public void updateUserInfo(Context context, TellerTerminal terminal) {
    final Context fContext = context;

    // Creating the dialog
    String title = "Update user " + terminal.getCurrentCustomer().getRoleId() + " information";
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setView(makeUserForm)
        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Cancel", null).create();

    dialog.show();

    // Getting and finalizing the input views to access during onClick
    final TextView nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final TextView ageView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final TextView addressView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final TextView passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    nameView.setText(terminal.getCurrentCustomer().getName());
    ageView.setText(Integer.toString(terminal.getCurrentCustomer().getAge()));
    addressView.setText(terminal.getCurrentCustomer().getAddress());
    final TellerTerminal fTerminal = terminal;

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Getting inputs from TextViews
        String name = nameView.getText().toString();
        int age;
        try {
          age = Integer.parseInt(ageView.getText().toString());
        } catch (Exception e) {
          Toast toast = Toast.makeText(fContext, "Invalid age", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }
        String address = addressView.getText().toString();
        String password = passwordView.getText().toString();

        if (password.isEmpty()) {
          Toast toast = Toast.makeText(fContext, "Invalid password", Toast.LENGTH_SHORT);
          toast.show();
          return;
        } else if (name.isEmpty()) {
          Toast toast = Toast.makeText(fContext, "Invalid name", Toast.LENGTH_SHORT);
          toast.show();
          return;
        } else if (address.isEmpty()) {
          Toast toast = Toast.makeText(fContext, "Invalid address", Toast.LENGTH_SHORT);
          toast.show();
        }

        // Updating user info
        boolean done = fTerminal.updateCustomerInfo(name, age, address, password);

        // Exit dialog on success
        if (done) {
          Toast.makeText(fContext, "Customer info updated", Toast.LENGTH_LONG).show();

          // Setting subtext
          TextView subtext = (TextView)
                  ((MenuActivity) fContext).findViewById(R.id.terminal_sub_text);
          subtext.setVisibility(View.VISIBLE);
          subtext.setText("Current customer: " + fTerminal.getCurrentCustomer().getName()
                  +  " (ID: " + fTerminal.getCurrentCustomer().getId() + ")");
          dialog.dismiss();
        } else {
          Toast toast = Toast.makeText(fContext, "Invalid input", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });
  }
}
