package com.bank.activities.lists.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bank.R;
import com.bank.accounts.Account;
import com.bank.activities.lists.adapters.AccountListAdapter;
import com.bank.database.android.DatabaseHelper;
import com.bank.exceptions.InsufficientFundsException;
import com.bank.exceptions.InsufficientPermissionException;
import com.bank.userinterfaces.TellerTerminal;
import com.bank.users.Customer;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Activity for the list of accounts.
 */
public class AccountListActivity extends AppCompatActivity {
  private ArrayList<Account> accounts;
  private String action;
  private TellerTerminal terminal;
  private ListView listView;
  private AccountListAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_account_list);
    Intent intent = getIntent();
    int terminalId = intent.getIntExtra("terminalId", 0);
    String terminalPw = intent.getStringExtra("terminalPw");
    int customerId = intent.getIntExtra("customerId", 0);
    action = intent.getStringExtra("action");
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    terminal = new TellerTerminal(this, terminalId, terminalPw);
    Customer customer = (Customer) dbHelper.getUserObject(customerId);
    terminal.setCurrentCustomer(customer);
    accounts = (ArrayList<Account>) terminal.listAccounts();

    // Setting the title of the list
    TextView titleView = (TextView) findViewById(R.id.account_list_title);
    switch (action) {
      case "listAccounts":
        titleView.setText(customer.getName() + "'s Accounts");
        findViewById(R.id.account_list_btn).setVisibility(View.GONE);
        break;
      case "giveInterest":
        titleView.setText("Select account to give interest to");
        break;
      case "makeDeposit":
        titleView.setText("Select account to make deposit to");
        findViewById(R.id.account_list_btn).setVisibility(View.GONE);
        break;
      case "makeWithdrawal":
        titleView.setText("Select account to withdraw from");
        findViewById(R.id.account_list_btn).setVisibility(View.GONE);
        break;
      case "sendMoney":
        titleView.setText("Select account to send from");
        findViewById(R.id.account_list_btn).setVisibility(View.GONE);
        break;
      default:
        // no other options
    }

    // Converting list using adapter
    adapter = new AccountListAdapter(this, 0, accounts);
    listView = (ListView) findViewById(R.id.accounts_list);

    // Button at the bottom of the list (can be used dynamically)
    Button accountListBtn = (Button) findViewById(R.id.account_list_btn);
    accountListBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleButtonClick();
      }
    });

    // Defining onClick events for list items
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        handleRowClick(i);
      }
    });

    listView.setAdapter(adapter);
  }

  private void handleRowClick(int i) {
    Account account = accounts.get(i);

    // Opening user interfaces according to the action from the intent
    switch (action) {
      case "giveInterest":
        account.findAndSetInterestRate();
        account.addInterest();
        Toast toast = Toast.makeText(this, "Interest given", Toast.LENGTH_SHORT);
        toast.show();
        updateList();
        break;
      case "makeDeposit":
      case "makeWithdrawal":
        updateBalance(account.getId());
        break;
      case "sendMoney":
        sendMoney(account.getId());
        break;
      default:
        // no other options
    }
  }

  private void sendMoney(int accountId) {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(this);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText idView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText amountView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    idView.setHint("Send to account ID");
    idView.setInputType(InputType.TYPE_CLASS_NUMBER);
    amountView.setHint("Amount");
    amountView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);

    String title = "Send money";
    final int finAccountId = accountId;

    final AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(title)
            .setView(makeUserForm)
            .setPositiveButton("Send", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                int toAccountId;

                try {
                  toAccountId = Integer.parseInt(idView.getText().toString());
                } catch (Exception e) {
                  Toast toast = Toast.makeText(AccountListActivity.this, "Invalid ID",
                          Toast.LENGTH_SHORT);
                  toast.show();
                  return;
                }

                BigDecimal amount;

                try {
                  amount = new BigDecimal(amountView.getText().toString());
                } catch (Exception e) {
                  Toast toast = Toast.makeText(AccountListActivity.this, "Invalid amount",
                          Toast.LENGTH_SHORT);
                  toast.show();
                  return;
                }

                if (amount.scale() > 2) {
                  String msg = "Please include 2 or fewer decimal digits";
                  Toast toast = Toast.makeText(AccountListActivity.this, msg,
                          Toast.LENGTH_SHORT);
                  toast.show();
                  return;
                }

                boolean success = false;

                try {
                  success = terminal.sendMoney(finAccountId, toAccountId, amount);

                  Toast toast = Toast.makeText(AccountListActivity.this,
                          "Transaction complete", Toast.LENGTH_SHORT);
                  toast.show();
                } catch (InsufficientPermissionException e) {
                  Toast toast = Toast.makeText(AccountListActivity.this,
                          "Teller authorization required", Toast.LENGTH_SHORT);
                  toast.show();
                } catch (InsufficientFundsException e) {
                  Toast toast = Toast.makeText(AccountListActivity.this,
                          "Insufficient funds", Toast.LENGTH_SHORT);
                  toast.show();
                } catch (Exception e) {
                  // account cannot be invalid
                }

                if  (!success) {
                  Toast toast = Toast.makeText(AccountListActivity.this,
                          "Invalid account", Toast.LENGTH_SHORT);
                  toast.show();
                }

                updateList();
              }
            })
            .setNegativeButton("Cancel", null).create();

    dialog.show();
  }

  private void updateBalance(int accountId) {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(this);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText amountView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    amountView.setHint("Amount");
    amountView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
    nameView.setVisibility(View.GONE);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);

    String title = "";
    String posBtnText = "";
    switch (action) {
      case "makeDeposit":
        title = "Deposit";
        posBtnText = "Deposit";
        break;
      case "makeWithdrawal":
        title = "Withdraw";
        posBtnText = "Withdraw";
        break;
      default:
        // will always be set
    }

    final int fAccountId = accountId;

    final AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(title)
        .setView(makeUserForm)
        .setPositiveButton(posBtnText, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            BigDecimal amount;

            try {
              amount = new BigDecimal(amountView.getText().toString());
            } catch (Exception e) {
              Toast toast = Toast.makeText(AccountListActivity.this, "Invalid amount",
                      Toast.LENGTH_SHORT);
              toast.show();
              return;
            }

            if (amount.scale() > 2) {
              String msg = "Please include 2 or fewer decimal digits";
              Toast toast = Toast.makeText(AccountListActivity.this, msg,
                  Toast.LENGTH_SHORT);
              toast.show();
              return;
            }

            try {
              if (action.equals("makeDeposit")) {
                terminal.makeDeposit(amount, fAccountId);
              } else if (action.equals("makeWithdrawal")) {
                terminal.makeWithdrawal(amount, fAccountId);
              }

              Toast toastTwo = Toast.makeText(AccountListActivity.this,
                      "Transaction complete", Toast.LENGTH_SHORT);
              toastTwo.show();
            } catch (InsufficientPermissionException e) {
              Toast toast = Toast.makeText(AccountListActivity.this,
                      "Teller authorization required", Toast.LENGTH_SHORT);
              toast.show();
            } catch (InsufficientFundsException e) {
              Toast toast = Toast.makeText(AccountListActivity.this,
                  "Insufficient funds", Toast.LENGTH_SHORT);
              toast.show();
            } catch (Exception e) {
              // account cannot be invalid
            }

            updateList();
            dialog.dismiss();
          }
        })
        .setNegativeButton("Cancel", null).create();

    dialog.show();
  }

  private void handleButtonClick() {
    if (action.equals("giveInterest")) {
      terminal.giveAllInterest();
      Toast toast = Toast.makeText(this, "Interest given", Toast.LENGTH_SHORT);
      toast.show();
      updateList();
    }
  }

  private void updateList() {
    accounts = (ArrayList<Account>) terminal.listAccounts();
    adapter = new AccountListAdapter(this, 0, accounts);;
    listView.setAdapter(adapter);
  }
}
