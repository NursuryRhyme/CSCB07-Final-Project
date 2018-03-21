package com.bank.activities.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bank.R;
import com.bank.activities.lists.activities.UserListActivity;
import com.bank.database.android.DatabaseHelper;
import com.bank.generics.RoleMap;
import com.bank.userinterfaces.AdminTerminal;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by byron on 2017-07-28.
 */

public class AdminFunc {
  private int newUserId;
  private AdminTerminal terminal;
  private Context context;

  public AdminFunc() {}

  public AdminFunc(AdminTerminal terminal, Context context) {
    this.terminal = terminal;
    this.context = context;
  }
  
  /**
   * Creates a new User.
   * @param context of the activity
   * @param newUserRole role of the new user
   * @param adminId id of the current authenticated Admin
   * @param adminPw pw of the current authenticated Admin
   * @return a new user
   */
  public int createUser(Context context, int newUserRole, int adminId, String adminPw) {
    final AdminTerminal terminal = new AdminTerminal(context, adminId, adminPw);
    final Context fContext = context;

    // Creating the dialog
    String title = "Make a new " + terminal.getRoleName(newUserRole).toLowerCase();
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setView(makeUserForm)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Cancel", null).create();

    dialog.show();

    // Getting the input views
    final int roleId = newUserRole;
    final TextView nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final TextView ageView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final TextView addressView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final TextView passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);

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

        // Creating the user
        int newUserId = terminal.makeNewUser(name, age, address, roleId, password);
        setUserRet(newUserId);

        // Exit dialog on success
        if (newUserId != -1) {
          Toast.makeText(fContext, "New user's ID: "
              + Integer.toString(newUserId), Toast.LENGTH_LONG).show();
          dialog.dismiss();
        } else {
          Toast toast = Toast.makeText(fContext, "Invalid input", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });

    return this.newUserId;
  }

  private void setUserRet(int userId) {
    this.newUserId = userId;
  }

  /**
   * View all current users of the bank.
   * @param userId id of the user
   * @param userPw pw of the user
   */
  public void viewUsers(int userId, String userPw) {
    // Converting variables to final to use in inner methods
    final int terminalId = userId;
    final String terminalPw = userPw;

    // Getting the list of roles and putting it into an array
    DatabaseHelper dbHelper = new DatabaseHelper(context);
    ArrayList<String> roles = (ArrayList<String>) dbHelper.getRoleList();
    final String[] rolesArr = roles.toArray(new String[0]);

    // Creating the dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle("User type")
        .setItems(rolesArr, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Starting the user list activity
            int roleId = RoleMap.getInstance(context).getRoleId(rolesArr[which]);
            Intent intent = new Intent(context, UserListActivity.class);
            intent.putExtra("terminalId", terminalId);
            intent.putExtra("terminalPw", terminalPw);
            intent.putExtra("roleId", roleId);
            context.startActivity(intent);
          }
        });

    builder.show();
  }
  
  /**
   * Promotes a Teller to Admin.
   */
  public void promoteTeller() {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText idView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    idView.setHint("Teller ID");
    nameView.setVisibility(View.GONE);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);
    String title = "Promote teller";

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setView(makeUserForm)
        .setPositiveButton("Promote", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Cancel", null).create();

    dialog.show();

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int tellerId;
        boolean success;

        try {
          tellerId = Integer.parseInt(idView.getText().toString());
          success = terminal.promoteTeller(tellerId);
        } catch (Exception e) {
          Toast toast = Toast.makeText(context, "Invalid ID", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }

        if (success) {
          Toast toast = Toast.makeText(context, "Teller promoted", Toast.LENGTH_SHORT);
          toast.show();
          dialog.dismiss();
        } else {
          Toast toast = Toast.makeText(context, "Not a teller", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });
  }
  
  /**
   * Views the total money the bank owns.
   */
  public void viewTotalMoney() {
    // Creating a dialog to display the total money
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    BigDecimal money = terminal.getTotalMoney();
    String msg = "Total money in bank:\n$" + money;
    builder.setMessage(msg)
        .setTitle("Bank")
        .setNegativeButton("OK", null).create();

    AlertDialog dialog = builder.create();
    dialog.show();
  }

  /**
   * Interface for viewing a customer's total balance.
   */
  public void userTotalBalance() {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText idView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    idView.setHint("User ID");
    nameView.setVisibility(View.GONE);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);
    String title = "Check total balance";

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
    final TextView balanceText = (TextView) makeUserForm.findViewById(R.id.user_info);

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Getting inputs from TextViews
        int userId;
        BigDecimal balance;
        try {
          userId = Integer.parseInt(idView.getText().toString());
          balance = terminal.userTotalBalance(userId);
          balanceText.setVisibility(View.VISIBLE);
          balanceText.setText("Total balance: $" + balance.toString());
        } catch (Exception e) {
          Toast toast = Toast.makeText(context, "Invalid customer ID", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }
      }
    });
  }
}
