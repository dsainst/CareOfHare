package ru.careofhair.careofhare;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Сергей on 10.09.2016.
 * Project name CareOfHare
 */

public class CareActivity extends AppCompatActivity {

    private static final String LOG_TAG = "my_tag";

    Button btnStart, btnSale, btnShedule;
    ImageButton StatBtn;
    TextView dateStart, LogMoney, LogClients, YesClients, YesMoney, dev;
    Intent intent, intent_stats, intentDev, intent_scheduler, intent_sendNow;
    SimpleDateFormat dfDate_day;
    DBHelper dbHelper;
    String valueSale = "";
    String dt = "", sale = "0";

    Cursor cursor;
    Calendar calendar;

    // имя файла настройки

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care);

        intent = new Intent(this, SaleActivity.class);
        intent_stats = new Intent(this, StatActivity.class);
        intentDev = new Intent(this, DevActivity.class);
        intent_scheduler = new Intent(this, ScheduleActivity.class);
        intent_sendNow = new Intent(this, SendNowActivity.class);
        dbHelper = new DBHelper(this);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnShedule = (Button) findViewById(R.id.btn_schedule);
        btnSale = (Button) findViewById(R.id.button);
        LogClients = (TextView) findViewById(R.id.today_clients);
        LogMoney = (TextView) findViewById(R.id.today_money);
        YesClients = (TextView) findViewById(R.id.yes_clients);
        YesMoney = (TextView) findViewById(R.id.yes_money);
        dev = (TextView) findViewById(R.id.developer);
        StatBtn = (ImageButton) findViewById(R.id.btn_stat);

        setDate();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select snl.*, sn."+DBHelper.KEY_MESS+" from " + DBHelper.TABLE_SEND_NOW_LOG + " as snl inner join " + DBHelper.TABLE_SEND_NOW + " as sn on sn." + DBHelper.KEY_ID + "=snl." + DBHelper.KEY_MESS_ID + "  where " + DBHelper.KEY_STATUS + "=? ", new String[]{"0"});


        // Кнопка Настроить скидку
        btnSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.putExtra("sale", sale);
                startActivityForResult(intent, 1);
            }
        });

        // Кнопка Разработчик
        dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intentDev);
                setDate();
            }
        });

        // Кнопка Статистика
        StatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent_stats);
                setDate();
            }
        });


        // Кнопка рассылка сейчас
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent_sendNow);
                setDate();
            }
        });

        // Кнопка рассылка по расписанию
        btnShedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent_scheduler);
                setDate();
            }
        });

        /*
        if (isMyServiceRunning(Send2SMS.class)) {
            btnStart.setEnabled(false);
        } else {
            btnStart.setEnabled(true);
        }
        startService(
                new Intent(CareActivity.this, Send2SMS.class));
        btnStart.setEnabled(false);
        */
    }


    public void setDate() {
        dateStart = (TextView) findViewById(R.id.getDate);
        dfDate_day = new SimpleDateFormat("dd.MM.yyyy");

        calendar = Calendar.getInstance();
        dt = dfDate_day.format(calendar.getTime());
        dateStart.setText(getString(R.string.today) + " " + dt);

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        cursor = database.query(DBHelper.TABLE_SALE, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            int phoneIndex = cursor.getColumnIndex(DBHelper.KEY_SALE);
            sale = cursor.getString(phoneIndex);
            dateStart.append(getString(R.string.sale) + sale + "%");
        } else {
            Log.d(LOG_TAG, getString(R.string.empty_rows));
            intent.putExtra("sale", "0");
            startActivityForResult(intent, 1);
        }
        cursor.close();

        cursor = database.query(DBHelper.TABLE_MESSAGE_SEND, null, DBHelper.KEY_DATE + " LIKE ?", new String[]{"%" + dt + "%"}, null, null, null);
        LogClients.setText(String.valueOf(cursor.getCount()));

        Integer price = 0;
        //получаем сумму за сегодня
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            do {
                price += cursor.getInt(idIndex);
            } while (cursor.moveToNext());
            LogMoney.setText(String.valueOf(price) + " РУБ");
        } else
            LogMoney.setText(getString(R.string.empty_summa));

        cursor.close();

        calendar.add(Calendar.DATE, -1);
        dt = dfDate_day.format(calendar.getTime());

        //узнаем сколько клиентов и денег было получено вчера
        cursor = database.query(DBHelper.TABLE_MESSAGE_SEND, null, DBHelper.KEY_DATE + " LIKE ?", new String[]{"%" + dt + "%"}, null, null, null);
        YesClients.setText(cursor.getCount() + " КЛИЕНТОВ");

        price = 0;
        //получаем сумму за вчера
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            do {
                price += cursor.getInt(idIndex);
            } while (cursor.moveToNext());
            YesMoney.setText(String.valueOf(price) + " РУБ.");
        } else
            YesMoney.setText(getString(R.string.empty_summa));

        cursor.close();
        database.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            String sale = data.getStringExtra("sale");
            writeSale(sale);
            setDate();
            Toast.makeText(this, getString(R.string.sale_change), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.sale_not_change), Toast.LENGTH_SHORT).show();
        }
    }

    public void writeSale(String sale) { //изменение значения скидки

        Log.d(LOG_TAG, "writeToSQLdb");

        valueSale = sale;

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "database");
        ContentValues contentValues = new ContentValues();
        Log.d(LOG_TAG, "contentValues");

        try {
            contentValues.put(DBHelper.KEY_SALE, valueSale);
            Log.d(LOG_TAG, "contentValues");

            Cursor cursor = database.query(DBHelper.TABLE_SALE, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                Log.d(LOG_TAG, "update");
                database.update(DBHelper.TABLE_SALE, contentValues, DBHelper.KEY_ID + "= ?", new String[]{"1"});
            } else {
                Log.d(LOG_TAG, "insert");
                database.insert(DBHelper.TABLE_SALE, null, contentValues);
            }
            cursor.close();

        } catch (Exception e) {
            Log.d(LOG_TAG, getString(R.string.error_write));
        }
        database.close();
    }

    /*    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
*/
}
