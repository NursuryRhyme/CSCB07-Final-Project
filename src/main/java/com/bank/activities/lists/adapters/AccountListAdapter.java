package com.bank.activities.lists.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bank.R;
import com.bank.accounts.Account;
import com.bank.database.android.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by byron on 2017-07-29.
 */

public class AccountListAdapter extends ArrayAdapter<Account> {
  public AccountListAdapter(Context context, int resource, ArrayList<Account> accounts) {
    super(context, resource, accounts);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // Finding layout view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_account_item,
          parent, false);
    }

    // Finding TextViews
    TextView accountId = (TextView) convertView.findViewById(R.id.acc_id);
    TextView name = (TextView) convertView.findViewById(R.id.acc_name);
    TextView type = (TextView) convertView.findViewById(R.id.acc_type);

    DatabaseHelper dbHelper = new DatabaseHelper(this.getContext());

    // Getting user object and setting TextView text
    Account account = getItem(position);
    accountId.setText(Integer.toString(account.getId()));
    name.setText(account.getName());
    String typeName = dbHelper.getAccountTypeName(account.getType());
    type.setText(typeName);
    TextView balance = (TextView) convertView.findViewById(R.id.acc_balance);
    balance.setText("$" + account.getBalance().toString());

    return convertView;
  }
}
