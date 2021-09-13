package com.waplak.alertreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    //private static final String PATTERN = "\\d{10}";
    private static final String PATTERN = "\\d{9,}";

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    AlertListViewAdapter alertListViewAdapter;
    public static MainActivity instance;
    ArrayList<Alert> alertList = new ArrayList<>();
    ListView lvSMS;

    public static MainActivity Instance(){
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        // Define ActionBar object
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        //actionBar.hide();
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#FF6200EE"));
        actionBar.setBackgroundDrawable(colorDrawable);

        checkAndRequestPermissions();
        alertList = refreshInbox();

        // setting List View for Messages
        lvSMS = findViewById(R.id.lv_sms);
        alertListViewAdapter = new AlertListViewAdapter(alertList);
        lvSMS.setAdapter(alertListViewAdapter);

    }

    private  boolean checkAndRequestPermissions() {
        int permissionReadMessage = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS);
        int permissionReadContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int permissionPhoneCall = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionReadMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (permissionReadContacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (permissionPhoneCall != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

//    public void refreshInboxOnClick(View v){
//        ArrayList<Alert> alertList = refreshInbox();
//        alertListViewAdapter = new AlertListViewAdapter(alertList);
//        lvSMS.setAdapter(alertListViewAdapter);
//        alertListViewAdapter.notifyDataSetChanged();
//    }

    public ArrayList<Alert> refreshInbox(){
        ArrayList<Alert> alertList = new ArrayList<>();
        ContentResolver cResolver = getContentResolver();
        Cursor smsInboxCursor = cResolver.query(Uri.parse("content://sms/inbox"),
                null, null, null, null);

        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        int indexDate =smsInboxCursor.getColumnIndex("date");
        if(indexBody < 0 || !smsInboxCursor.moveToFirst()) return null;

        alertList.clear();
        long alertId = 0;
        do{
            if("Alert".equals(smsInboxCursor.getString(indexAddress))) {
                String line = smsInboxCursor.getString(indexBody);
                Pattern p = Pattern.compile(PATTERN);
                Matcher m = p.matcher(line);

                while (m.find()) {
                    Alert alert = new Alert();
                    ++alertId;
                    alert.setAlertId(alertId);
                    // Write the mobile number to output.txt file
                    String number = m.group();
                    alert.setContactNumber(number);
                    String name = getContactName(number, getApplicationContext());
                    if (name != null) {
                        alert.setContactName(name);
                    } else {
                        alert.setContactName(number);
                    }


                    String date = smsInboxCursor.getString(indexDate);
                    Long timestamp = Long.parseLong(date);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    Date finaldate = calendar.getTime();
                    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String strDate = sdfDate.format(finaldate);
                    alert.setDateTime(strDate);
                    alert.setCount(1);
                    alertList.add(alert);
                }
            }
        }while (smsInboxCursor.moveToNext());

        return alertList;
    }


    public void updateList(){
        refreshInbox();
        alertListViewAdapter = new AlertListViewAdapter(alertList);
        lvSMS.setAdapter(alertListViewAdapter);
        alertListViewAdapter.notifyDataSetChanged();
    }

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName=null;
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
}