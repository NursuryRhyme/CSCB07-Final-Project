package com.bank.activities.lists.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;

import com.bank.R;
import com.bank.activities.lists.adapters.UserListAdapter;
import com.bank.userinterfaces.AdminTerminal;
import com.bank.users.User;

import java.util.ArrayList;

/**
 * Activity for the list of users.
 */
public class UserListActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_list);
    Intent intent = getIntent();
    int roleId = intent.getIntExtra("roleId", 0);
    int terminalId = intent.getIntExtra("terminalId", 0);
    String terminalPw = intent.getStringExtra("terminalPw");
    AdminTerminal terminal = new AdminTerminal(this, terminalId, terminalPw);

    // Getting an ArrayList of the users of this role
    ArrayList<User> users = (ArrayList<User>)  terminal.listUsersByRole(roleId);

    // Setting title
    String roleTitle = terminal.getRoleName(roleId) + "S";
    ((TextView) findViewById(R.id.user_list_title)).setText(roleTitle);

    // Converting list using adapter and setting it
    UserListAdapter adapter = new UserListAdapter(this, 0, users);
    ListView listView = (ListView) findViewById(R.id.user_list);
    listView.setAdapter(adapter);
  }
}
