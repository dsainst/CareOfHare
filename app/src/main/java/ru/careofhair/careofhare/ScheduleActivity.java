package ru.careofhair.careofhare;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class ScheduleActivity extends AppCompatActivity {

    private static final String LOG_TAG = "my_tag";
    EditText multiText;
    TextView header, length;
    Button ok;
    ImageButton homeBtn;
    DBHelper dbHelper;
    SimpleDateFormat dfDate_day;
    String dt = "";
    Cursor cursor;
    Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

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

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        getInfo();

        // Кнопка активировать
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setInfo();
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

    public void getInfo () {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        calendar = Calendar.getInstance();
        dt = dfDate_day.format(calendar.getTime());

        cursor = db.query(DBHelper.TABLE_SCHEDULER_LOG, null, DBHelper.KEY_DATE + " LIKE ?", new String[]{"%" + dt + "%"}, null, null, null);

        header.setText(getString(R.string.schedule_head, String.valueOf(cursor.getCount())));

        cursor = db.query(DBHelper.TABLE_SCHEDULER, null, null, null, null, null, null);

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

    public void setInfo () {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String mText = multiText.getText().toString().trim();
        if (!mText.isEmpty()) {
            calendar = Calendar.getInstance();
            dt = dfDate_day.format(calendar.getTime());
            ContentValues contentValues = new ContentValues();

            contentValues.put(DBHelper.KEY_MESS, mText);
            contentValues.put(DBHelper.KEY_DATE, dt);
            cursor = db.query(DBHelper.TABLE_SCHEDULER, null, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                Log.d(LOG_TAG, "Запись уже есть - обновляем!");
                db.update(DBHelper.TABLE_SCHEDULER, contentValues, DBHelper.KEY_ID + "=?", new String[]{"1"});
                Toast.makeText(this, getString(R.string.update_db), Toast.LENGTH_SHORT).show();
            } else {
                Log.d(LOG_TAG, "Вставляем новую запись" + contentValues.toString());
                db.insert(DBHelper.TABLE_SCHEDULER, null, contentValues);
                Toast.makeText(this, getString(R.string.insert_db), Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } else {
            Toast.makeText(this, getString(R.string.no_text), Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

}
