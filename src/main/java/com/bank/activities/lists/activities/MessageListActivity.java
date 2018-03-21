package com.bank.activities.lists.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.bank.R;
import com.bank.activities.lists.adapters.MessageListAdapter;
import com.bank.messages.Message;
import com.bank.userinterfaces.AdminTerminal;
import com.bank.userinterfaces.Atm;
import com.bank.userinterfaces.TellerTerminal;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Activity for the list of messages.
 */
public class MessageListActivity extends AppCompatActivity {

  private String action;
  private String role;
  private int terminalId;
  private String terminalPw;
  private int customerId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ArrayList<Message> messages = null;

    setContentView(R.layout.activity_message_list);

    Intent intent = getIntent();
    action = intent.getStringExtra("action");
    role = intent.getStringExtra("role");
    terminalId = intent.getIntExtra("terminalId", 0);
    terminalPw = intent.getStringExtra("terminalPw");
    customerId = intent.getIntExtra("customerId", 0);


    switch (role) {
      case "teller":
        TellerTerminal terminal = new TellerTerminal(this, terminalId, terminalPw);
        if (action.equals("listCustomerMessage")) {
          messages = (ArrayList<Message>) terminal.listMessages(customerId);
        } else if (action.equals("listMyMessage")) {
          messages = (ArrayList<Message>) terminal.listMessages(terminalId);
        }
        break;
      case "admin":
        AdminTerminal adminTerminal = new AdminTerminal(this, terminalId, terminalPw);
        messages = (ArrayList<Message>) adminTerminal.listMessages(terminalId);
        break;
      case "customer":
        Atm atm = new Atm(this, terminalId, terminalPw);
        messages = (ArrayList<Message>) atm.listMyMessages();
        break;
      default:
        // No default case allowed
    }

    Collections.reverse(messages);
    MessageListAdapter adapter = new MessageListAdapter(this, 0, messages);
    ListView listView = (ListView) findViewById(R.id.message_listview);
    listView.setAdapter(adapter);
  }
}
