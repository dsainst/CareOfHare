package ru.careofhair.careofhare;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SendNowActivity extends AppCompatActivity {

    private static final String LOG_TAG = "my_tag";
    EditText multiText;
    TextView header, length;
    Button ok;
    ImageButton homeBtn;
    DBHelper dbHelper;
    SimpleDateFormat dfDate_day;
    String dt = "", dt3 = "";
    Cursor cursor, contacts;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_now);

        multiText = (EditText) findViewById(R.id.multi_text);
        header = (TextView) findViewById(R.id.header);
        length = (TextView) findViewById(R.id.length);
        ok = (Button) findViewById(R.id.btn_ok);
        homeBtn = (ImageButton) findViewById(R.id.home_btn);

        dbHelper = new DBHelper(this);
        dfDate_day = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        multiText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                length.setText(String.valueOf(s.length()));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        getInfo();

        // Кнопка активировать
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInfo();

                sendSMS();

                finish();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void sendSMS() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        calendar = Calendar.getInstance();
        dt = dfDate_day.format(calendar.getTime());

        calendar.add(Calendar.MONTH, 3);
        dt3 = dfDate_day.format(calendar.getTime());

        contacts = db.query(DBHelper.TABLE_CONTACTS, null, DBHelper.KEY_STATUS + "=? and " + DBHelper.KEY_DATE_LAST_VISIT + "<=?", new String[]{"1", dt3}, null, null, null);
        //header.setText(getString(R.string.sendnow_head, String.valueOf(contacts.getCount())));

        if (contacts.getCount() > 0) {
            String message = "";
            cursor = db.query(DBHelper.TABLE_SEND_NOW, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                int intMess = cursor.getColumnIndex(DBHelper.KEY_MESS);
                message = cursor.getString(intMess);
            } else {
                Log.d(LOG_TAG, getString(R.string.empty_rows));
            }
            if (!message.isEmpty()) { // подключаем сервис рассылки SMS
                if (contacts.moveToFirst()) {
                    String phone = "";
                    do {
                        int intPhone = contacts.getColumnIndex(DBHelper.KEY_PHONE);
                        phone = contacts.getString(intPhone);
                        if (!phone.isEmpty()) {
                            contentValues.put(DBHelper.KEY_PHONE, phone);
                            contentValues.put(DBHelper.KEY_STATUS, 0);
                            contentValues.put(DBHelper.KEY_MESS_ID, "1");
                            contentValues.put(DBHelper.KEY_DATE_SEND, dt);
                            Log.d("my_tag", contentValues.toString());
                            db.insert(DBHelper.TABLE_SEND_NOW_LOG, null, contentValues);
                        }
                    } while (contacts.moveToNext());
                    if (!isMyServiceRunning(Send2SMS.class)) {
                        startService(new Intent(SendNowActivity.this, Send2SMS.class));
                    }
                } else {
                    Log.d(LOG_TAG, getString(R.string.empty_rows));
                }
            } else {
                Log.d(LOG_TAG, "Ошибка");
            }
        }
        cursor.close();
        db.close();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void getInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        calendar = Calendar.getInstance();
        dt = dfDate_day.format(calendar.getTime());

        cursor = db.query(DBHelper.TABLE_CONTACTS, null, null, null, null, null, null);
        header.setText(getString(R.string.sendnow_head, String.valueOf(cursor.getCount())));

        cursor = db.query(DBHelper.TABLE_SEND_NOW, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int intMess = cursor.getColumnIndex(DBHelper.KEY_MESS);
            String message = cursor.getString(intMess);
            multiText.setText(message);
        } else {
            Log.d(LOG_TAG, getString(R.string.empty_rows));
        }
        cursor.close();
        db.close();
    }

    public void setInfo() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String mText = multiText.getText().toString().trim();
        if (!mText.isEmpty()) {
            calendar = Calendar.getInstance();
            dt = dfDate_day.format(calendar.getTime());
            ContentValues contentValues = new ContentValues();

            contentValues.put(DBHelper.KEY_MESS, mText);
            contentValues.put(DBHelper.KEY_DATE, dt);
            cursor = db.query(DBHelper.TABLE_SEND_NOW, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                Log.d(LOG_TAG, "Запись уже есть - обновляем!");
                db.update(DBHelper.TABLE_SEND_NOW, contentValues, DBHelper.KEY_ID + "=?", new String[]{"1"});
                Toast.makeText(this, getString(R.string.update_db), Toast.LENGTH_SHORT).show();
            } else {
                Log.d(LOG_TAG, "Вставляем новую запись " + contentValues.toString());
                db.insert(DBHelper.TABLE_SEND_NOW, null, contentValues);
                Toast.makeText(this, getString(R.string.insert_db), Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } else {
            Toast.makeText(this, getString(R.string.no_text), Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
}
