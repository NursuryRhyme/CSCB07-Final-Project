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
import com.bank.activities.lists.activities.MessageListActivity;
import com.bank.userinterfaces.AdminTerminal;
import com.bank.userinterfaces.Atm;
import com.bank.userinterfaces.TellerTerminal;

public class MessageFunc {
  private String terminalType;
  private AdminTerminal adminTerminal;
  private TellerTerminal tellerTerminal;
  private int userId;
  private String userPw;
  private Context context;
  
  /**
   * Views the messages of an admin.
   * @param context of the activity
   * @param terminal being used
   * @param adminId id of the current Admin
   * @param adminPw pw of the current Admin
   */
  public MessageFunc(Context context, AdminTerminal terminal, int adminId, String adminPw) {
    this.context = context;
    this.terminalType = "admin";
    this.adminTerminal = terminal;
    this.userId = adminId;
    this.userPw = adminPw;
  }
  
  /**
   * Views the messages of a teller.
   * @param context of the activity
   * @param terminal being used
   * @param tellerId id of the current teller
   * @param tellerPw pw of the current teller
   */
  public MessageFunc(Context context, TellerTerminal terminal, int tellerId, String tellerPw) {
    this.context = context;
    terminalType = "teller";
    tellerTerminal = terminal;
    this.userId = tellerId;
    this.userPw = tellerPw;
  }

  /**
   * Views messages of a customer.
   * @param context of the activity
   * @param terminal being used
   * @param customerId id of the customer
   * @param customerPw pw of the customer
   */
  public MessageFunc(Context context, Atm terminal, int customerId, String customerPw) {
    this.context = context;
    terminalType = "customer";
    this.userId = customerId;
    this.userPw = customerPw;
  }

  /**
   * View all messages the current customer has.
   */
  public void listCustomerMessage() {
    Intent intent = new Intent(context, MessageListActivity.class);
    intent.putExtra("action", "listCustomerMessage");
    intent.putExtra("role", "teller");
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    intent.putExtra("customerId", tellerTerminal.getCurrentCustomer().getId());
    context.startActivity(intent);
  }

  /**
   * List all of the messages the current User has.
   */
  public void listMyMessage() {
    Intent intent = new Intent(context, MessageListActivity.class);
    intent.putExtra("action", "listMyMessage");
    intent.putExtra("role", terminalType);
    intent.putExtra("terminalId", userId);
    intent.putExtra("terminalPw", userPw);
    context.startActivity(intent);
  }

  /**
   * Checks if there is a currentAuthenticatedCustomer.
   * @return true if there is a current authenticated customer, false otherwise
   */
  public boolean checkCustomerStatus() {
    boolean result;

    if (tellerTerminal.getCurrentCustomer() != null) {
      result = true;
    }  else {
      Toast toast = Toast.makeText(context, "No customer authenticated", Toast.LENGTH_SHORT);
      toast.show();
      result = false;
    }

    return result;
  }

  /**
   * Views any message in the database.
   */
  public void viewAnyMessage() {
    // Creating the dialog
    LayoutInflater inflater = LayoutInflater.from(context);
    View makeUserForm = inflater.inflate(R.layout.make_user_form, null);
    final EditText idView = (EditText) makeUserForm.findViewById(R.id.user_age_input);
    final EditText nameView = (EditText) makeUserForm.findViewById(R.id.user_name_input);
    final EditText addrView = (EditText) makeUserForm.findViewById(R.id.user_addr_input);
    final EditText passwordView = (EditText) makeUserForm.findViewById(R.id.user_pw_input);
    idView.setHint("Message ID");
    nameView.setVisibility(View.GONE);
    addrView.setVisibility(View.GONE);
    passwordView.setVisibility(View.GONE);
    String title = "View message";

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

    // Finalizing variables to use inside inner methods
    final AdminTerminal fTerminal = adminTerminal;
    final TextView messageText = (TextView) makeUserForm.findViewById(R.id.user_info);

    // Overriding onClickListener so the dialog will close only when I want to
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int messageId;
        String message;

        try {
          // Parsing user input and getting the actual message
          messageId = Integer.parseInt(idView.getText().toString());
          message = fTerminal.viewAnyMessage(messageId);
          if (message != null) {
            // Displaying the message
            messageText.setVisibility(View.VISIBLE);
            messageText.setText("Message:\n" + message);
          } else {
            Toast toast = Toast.makeText(context, "Invalid message",
                Toast.LENGTH_SHORT);
            toast.show();
          }
        } catch (Exception e) {
          Toast toast = Toast.makeText(context, "Invalid message", Toast.LENGTH_SHORT);
          toast.show();
          return;
        }
      }
    });
  }

  /**
   * Leaves a message to a User.
   */
  public void leaveMessage() {
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

    // Getting the input views
    final EditText idView = (EditText) leaveMessageForm.findViewById(R.id.message_userid);
    final EditText messageView = (EditText) leaveMessageForm.findViewById(R.id.message_box);

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

        String message = messageView.getText().toString();
        int messageId = adminTerminal.createMessage(userId, message);
        if (messageId != -1) {
          Toast toast = Toast.makeText(context, "Message "
              + Integer.toString(messageId) + " sent", Toast.LENGTH_SHORT);
          toast.show();
          dialog.dismiss();
        } else {
          Toast toast = Toast.makeText(context, "Invalid ID", Toast.LENGTH_SHORT);
          toast.show();
        }
      }
    });
  }
}
