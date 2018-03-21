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
import com.bank.activities.lists.activities.AccountListActivity;
import com.bank.userinterfaces.Atm;
import com.bank.userinterfaces.TellerTerminal;

import java.math.BigDecimal;

/**
 * Created by byron on 2017-07-30.
 */

public class MoneyFunc {
  private Context context;
  private Atm atm;
  private TellerTerminal tellerTerminal;

  public MoneyFunc(Context context, Atm terminal) {
    this.context = context;
    this.atm = terminal;
  }

  public MoneyFunc(Context context, TellerTerminal terminal) {
    this.context = context;
    this.tellerTerminal = terminal;
  }

  /**
   * Check balance interface.
   */
  public void checkBalance() {
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
    final TextView balanceText = (TextView) makeUserForm.findViewById(R.id.user_info);

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // Getting inputs from TextViews
        int accountId;
        BigDecimal balance;
        try {
          accountId = Integer.parseInt(idView.getText().toString());
          balance = atm.checkBalance(accountId);
          balanceText.setVisibility(View.VISIBLE);
          balanceText.setText("Balance: $" + balance.toString());
        } catch (Exception e) {
          Toast toast = Toast.makeText(context, "Invalid account", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }
      }
    });
  }

  /**
   * Make deposit interface.
   * @param userId id of terminal user
   * @param userPw password of terminal user
   */
  public void makeDeposit(int userId, String userPw) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", userId);
    intent.putExtra("action", "makeDeposit");
    context.startActivity(intent);
  }

  /**
   * Make withdrawal interface.
   * @param userId id of terminal user
   * @param userPw password of terminal user
   */
  public void makeWithdrawal(int userId, String userPw) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", userId);
    intent.putExtra("action", "makeWithdrawal");
    context.startActivity(intent);
  }

  /**
   * Transfer money interface.
   * @param userId id of terminal user
   * @param userPw password of terminal user
   */
  public void transferMoney(int userId, String userPw) {
    Intent intent = new Intent(context, AccountListActivity.class);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", userId);
    intent.putExtra("action", "sendMoney");
    context.startActivity(intent);
  }

  /**
   * Check customer total balance.
   * @param userId id of terminal user
   * @param userPw password of terminal user
   */
  public void checkTotalBalance(int userId, String userPw) {
    BigDecimal totalBalance = tellerTerminal.customerTotalBalance();
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage("Customer's total balance: $" + totalBalance)
            .setTitle("Total balance")
            .setNegativeButton("OK", null).create();

    AlertDialog dialog = builder.create();
    dialog.show();
  }
}
