package com.waplak.alertreader;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

class AlertListViewAdapter extends BaseAdapter {

    final ArrayList<Alert> listAlert;

    AlertListViewAdapter(ArrayList<Alert> listAlert) {
        this.listAlert = listAlert;
    }

    @Override
    public int getCount() {
        return listAlert.size();
    }

    @Override
    public Object getItem(int position) {
        return listAlert.get(position);
    }

    @Override
    public long getItemId(int position) {
        return listAlert.get(position).getAlertId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View viewAlert;
        if (convertView == null) {
            viewAlert = View.inflate(parent.getContext(), R.layout.list_item, null);
        } else viewAlert = convertView;

        Alert alert = (Alert) getItem(position);
        ((TextView) viewAlert.findViewById(R.id.contact_name)).setText(alert.getContactName());
        ((TextView) viewAlert.findViewById(R.id.date_time)).setText(alert.getDateTime());
        ImageButton callBackBt = viewAlert.findViewById(R.id.call_back);
        callBackBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+alert.getContactNumber()));
                v.getContext().startActivity(callIntent);
            }
        });

        ImageButton msgBackBt = viewAlert.findViewById(R.id.msg_back);
        msgBackBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("smsto:"+alert.getContactNumber());
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra("sms_body", "I will call you later");
                v.getContext().startActivity(intent);
            }
        });
        return viewAlert;
    }
}
