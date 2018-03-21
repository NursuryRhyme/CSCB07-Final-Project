package com.bank.activities.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bank.R;
import com.bank.activities.lists.activities.AccountListActivity;
import com.bank.userinterfaces.AdminTerminal;
import com.bank.userinterfaces.Atm;

/**
 * Created by byron on 2017-07-30.
 */

public class AccountFunc {
  private AdminTerminal adminTerminal;
  private Context context;
  private int userId;
  private String userPw;
  //private Atm atm;

  /**
   * Check accounts of a given user.
   * @param context of the activity
   * @param adminTerminal the terminal being used
   * @param userId id of the user
   * @param userPw pw of the user
   */
  public AccountFunc(Context context, AdminTerminal adminTerminal, int userId, String userPw) {
    this.context = context;
    this.userId = userId;
    this.userPw = userPw;
    this.adminTerminal = adminTerminal;
  }

  /**
   * Check Accounts of a given user.
   * @param context of the activity
   * @param terminal the terminal being used
   * @param userId id of the user
   * @param userPw pw of the user
   */
  public AccountFunc(Context context, Atm terminal, int userId, String userPw) {
    this.context = context;
    this.userId = userId;
    this.userPw = userPw;
    //this.atm = terminal;
  }

  /**
   * List all accounts a customer owns.
   * @param customerId id of the customer
   */
  public void listAccounts(int customerId) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", customerId);
    intent.putExtra("action", "listAccounts");
    context.startActivity(intent);
  }

  /**
   * Lists all the accounts.
   */
  public void listAccounts() {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText idView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    idView.setHint("Customer ID");
    nameView.setVisibility(View.GONE);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);
    String title = "View accounts";

    final AlertDialog dialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setView(makeUserForm)
        .setPositiveButton("View", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Overridden later
          }
        })
        .setNegativeButton("Close", null).create();

    dialog.show();

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int userId;

        try {
          userId = Integer.parseInt(idView.getText().toString());
        } catch (Exception e) {
          Toast toast = Toast.makeText(context, "Invalid ID", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }

        if (adminTerminal.isCustomer(userId)) {
          listAccounts(userId);
        } else {
          Toast toast = Toast.makeText(context, "Not a customer", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });
  }
}
