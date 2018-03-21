package com.bank.activities.lists.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.bank.R;
import com.bank.users.User;
import java.util.ArrayList;

/**
 * Created by byron on 2017-07-28.
 */

public class UserListAdapter extends ArrayAdapter<User> {
  public UserListAdapter(Context context, int resource, ArrayList<User> users) {
    super(context, resource, users);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // Finding layout view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_user_item,
          parent, false);
    }

    // Finding TextViews
    TextView userId = (TextView) convertView.findViewById(R.id.user_id);
    TextView name = (TextView) convertView.findViewById(R.id.user_name);

    // Getting user object and setting TextView text
    User user = getItem(position);
    userId.setText(Integer.toString(user.getId()));
    name.setText(user.getName());

    return convertView;
  }
}
