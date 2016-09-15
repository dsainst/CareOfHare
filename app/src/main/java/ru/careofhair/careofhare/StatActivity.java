package ru.careofhair.careofhare;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StatActivity extends AppCompatActivity {

    DBHelper dbHelper;
    ImageButton homeBtn;

    TextView LogClients, LogToday, LogYester, LogYesYester, LogSumma, thisM;
    SimpleDateFormat dfDate_day, dfDate_month, dfDate_month_text;
    String dt = "";

    Cursor cursor;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        homeBtn = (ImageButton) findViewById(R.id.home_btn);
        dbHelper = new DBHelper(this);
        LogToday = (TextView) findViewById(R.id.today);
        LogYester = (TextView) findViewById(R.id.yesterday);
        LogYesYester = (TextView) findViewById(R.id.yesyestreday);
        LogClients = (TextView) findViewById(R.id.tv_clients);
        LogSumma = (TextView) findViewById(R.id.tv_summa);
        thisM = (TextView) findViewById(R.id.thisM);

        setContent();

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    // вывод в лог данных из курсора
    public void logCursor(Cursor c) {
        if (c != null) {
            if (c.moveToFirst()) {
                String str;
                do {
                    str = "";
                    for (String cn : c.getColumnNames()) {
                        str = str.concat(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                    }
                    Log.d("my_tag", str);
                } while (c.moveToNext());
            }
        } else
            Log.d("my_tag", "Cursor is null");
    }

    public void setContent() {

        Integer summa = 0;

        dfDate_day = new SimpleDateFormat("dd.MM.yyyy");
        calendar = Calendar.getInstance();

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        cursor = database.query(DBHelper.TABLE_CONTACTS, null, DBHelper.KEY_STATUS + "=?", new String[]{"1"}, null, null, null);
        LogClients.setText(String.valueOf(cursor.getCount()));

        cursor = database.query(DBHelper.TABLE_MESSAGE_SEND, null, null, null, null, null, null);
        //получаем сумму за все время
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            do {
                summa += cursor.getInt(idIndex);
            } while (cursor.moveToNext());
            LogSumma.setText(String.valueOf(summa) + " РУБ.");
        } else
            LogSumma.setText(getString(R.string.empty_summa));
        cursor.close();

        dt = dfDate_day.format(calendar.getTime());

        cursor = database.query(DBHelper.TABLE_MESSAGE_SEND, null, DBHelper.KEY_DATE + " LIKE ?", new String[]{"%" + dt + "%"}, null, null, null);
        LogToday.setText(getString(R.string.today) + " " + String.valueOf(cursor.getCount()) + " " + getString(R.string.clients) + " ");

        Integer price = 0;
        //получаем сумму за сегодня
        if (cursor.moveToFirst()) {
            int mess = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            do {
                price += cursor.getInt(mess);
            } while (cursor.moveToNext());
            LogToday.append(String.valueOf(price) + " РУБ.");
        } else
            LogToday.append(getString(R.string.empty_summa));
        cursor.close();

        calendar.add(Calendar.DATE, -1);
        dt = dfDate_day.format(calendar.getTime());

        //узнаем сколько клиентов и денег было получено вчера
        cursor = database.query(DBHelper.TABLE_MESSAGE_SEND, null, DBHelper.KEY_DATE + " LIKE ?", new String[]{"%" + dt + "%"}, null, null, null);
        LogYester.setText(getString(R.string.yesterday) + " " + String.valueOf(cursor.getCount()) + " " + getString(R.string.clients) + " ");

        price = 0;
        //получаем сумму за вчера
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            do {
                price += cursor.getInt(idIndex);
            } while (cursor.moveToNext());
            LogYester.append(String.valueOf(price) + " РУБ.");
        } else
            LogYester.append(getString(R.string.empty_summa));
        cursor.close();

        calendar.add(Calendar.DATE, -1);
        dt = dfDate_day.format(calendar.getTime());

        //узнаем сколько клиентов и денег было получено вчера
        cursor = database.query(DBHelper.TABLE_MESSAGE_SEND, null, DBHelper.KEY_DATE + " LIKE ?", new String[]{"%" + dt + "%"}, null, null, null);
        LogYesYester.setText(getString(R.string.yesyesterday) + " " + String.valueOf(cursor.getCount()) + " " + getString(R.string.clients) + " ");

        price = 0;
        //получаем сумму за позавчера
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            do {
                price += cursor.getInt(idIndex);
            } while (cursor.moveToNext());
            LogYesYester.append(String.valueOf(price) + " РУБ.");
        } else
            LogYesYester.append(getString(R.string.empty_summa));
        cursor.close();


        // статистика по месяцам
        dfDate_month = new SimpleDateFormat("MM.yyyy");
        dfDate_month_text = new SimpleDateFormat("MMM");
        calendar = Calendar.getInstance();

        dt = dfDate_month.format(calendar.getTime());

        cursor = database.query(DBHelper.TABLE_MESSAGE_SEND, null, DBHelper.KEY_DATE + " LIKE ?", new String[]{"%" + dt + "%"}, null, null, null);
        thisM.setText(getString(R.string.tomonth) + " " + String.valueOf(cursor.getCount()) + " " + getString(R.string.clients) + " ");

        price = 0;
        //получаем сумму за позавчера
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            do {
                price += cursor.getInt(idIndex);
            } while (cursor.moveToNext());
            thisM.append(String.valueOf(price) + " РУБ.");
        } else
            thisM.append(getString(R.string.empty_summa));
        cursor.close();

        database.close();
    }
}
