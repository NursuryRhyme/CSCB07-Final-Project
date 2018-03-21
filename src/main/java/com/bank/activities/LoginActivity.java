package com.bank.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bank.R;
import com.bank.database.android.DatabaseHelper;
import com.bank.users.User;


public class LoginActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    setupButtons();
    checkInitialized();
  }

  private void checkInitialized() {
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    String msg;
    String title;

    if (dbHelper.getRoleIds().isEmpty() && dbHelper.getUserIds().isEmpty()) {
      msg = "The database may not be initialized.\n"
              + "You can initialize the database from the options menu on the top right.";
      title = "Hello";

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(msg)
              .setTitle(title)
              .setNegativeButton("OK", null).create();

      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }

  private void setupButtons() {
    Button loginBtn = (Button) findViewById(R.id.login_btn);
    loginBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LoginActivity.this.handleLoginBtnClick((Button) v);
      }
    });
  }

  private void handleLoginBtnClick(Button v) {
    // Retrieving input
    String userIdStr = ((EditText) findViewById(R.id.login_id)).getText().toString();
    int userId;
    try {
      userId = Integer.parseInt(userIdStr);
    } catch (Exception e) {
      Toast toast = Toast.makeText(this, "Please enter an ID", Toast.LENGTH_SHORT);
      toast.show();
      return;
    }

    String password = ((EditText) findViewById(R.id.login_password)).getText().toString();

    DatabaseHelper dbHelper = new DatabaseHelper(this);
    User user = dbHelper.getUserObject(userId);
    if (user != null && user.authenticated(password)) {
      Intent intent = new Intent(this, MenuActivity.class);
      intent.putExtra("userRole", user.getRoleId());
      intent.putExtra("userId", userId);
      intent.putExtra("userPw", password);
      startActivity(intent);
    } else if (dbHelper.getRoleIds().isEmpty()) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      String msg = "Database may not have been initialized. "
              + "You can initialize the database from the options menu.";
      builder.setMessage(msg)
          .setTitle("Error")
          .setNegativeButton("OK", null).create();

      AlertDialog dialog = builder.create();
      dialog.show();
    } else {
      Toast toast = Toast.makeText(this, "Invalid ID or password", Toast.LENGTH_SHORT);
      toast.show();
    }
  }

  private void handleInitBtnClick() {
    DatabaseHelper dbHelper = new DatabaseHelper(this);

    // Prompting the user to set the admin password
    if (dbHelper.getRoleIds().isEmpty() && dbHelper.getUserIds().isEmpty()) {
      // Creating the dialog
      LayoutInflater inflater = LayoutInflater.from(this);
      View changePasswordForm = inflater.inflate(R.layout.change_password_form, null);

      final AlertDialog dialog = new AlertDialog.Builder(this)
              .setTitle("Set admin password")
              .setView(changePasswordForm)
              .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                  // Overridden later
                }
              }).create();

      dialog.show();

      // Finding the input
      final EditText passwordView;
      passwordView = (EditText) changePasswordForm.findViewById(R.id.change_password_input);

      // Overriding onClickListener so the dialog will close only when I want to
      dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          // Getting inputs from TextViews
          String newPassword = passwordView.getText().toString();

          if (newPassword.isEmpty()) {
            Toast toast = Toast.makeText(LoginActivity.this, "Please enter a password",
                    Toast.LENGTH_SHORT);
            toast.show();
          } else {
            // Initializing the database
            initDbWithPassword(newPassword);
            dialog.dismiss();
          }
        }
      });
    } else {
      String msg = "Database already initialized.";
      String title = "Error";
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setMessage(msg)
              .setTitle(title)
              .setNegativeButton("OK", null).create();

      AlertDialog dialog = builder.create();
      dialog.show();
    }
  }

  private void initDbWithPassword(String password) {
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    int adminId = dbHelper.initializeDatabase(password);
    Toast toast = Toast.makeText(LoginActivity.this, "Your admin ID is: " + adminId,
            Toast.LENGTH_LONG);
    toast.show();
  }

  private void handleReinitBtnClick() {
    DatabaseHelper dbHelper = new DatabaseHelper(this);
    dbHelper.reinitializeDatabase();
    Toast toast = Toast.makeText(this, "Database cleared", Toast.LENGTH_SHORT);
    toast.show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.login_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.initialize_menu_btn:
        handleInitBtnClick();
        break;
      case R.id.clear_database_btn:
        handleReinitBtnClick();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Removing text in inputs on resume
    findViewById(R.id.login_id).requestFocus();
    ((EditText) findViewById(R.id.login_id)).setText("");
    ((EditText) findViewById(R.id.login_password)).setText("");
  }
}
