package com.bank.activities.lists.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bank.R;
import com.bank.messages.Message;

import java.util.ArrayList;

public class MessageListAdapter extends ArrayAdapter<Message> {
  public MessageListAdapter(Context context, int resource, ArrayList<Message> messages) {
    super(context, resource, messages);
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // Finding layout view
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_message_item,
          parent, false);
    }

    // Finding TextViews
    TextView id = (TextView) convertView.findViewById(R.id.msg_id);
    TextView content = (TextView) convertView.findViewById(R.id.msg_content);
    TextView viewed = (TextView) convertView.findViewById(R.id.msg_viewed);

    // Get message record
    Message message = getItem(position);

    id.setText(Integer.toString(message.getId()));
    content.setText(message.getMessage());
    viewed.setText((message.getViewed() == 0) ? "N" : "Y");

    return convertView;
  }
}
