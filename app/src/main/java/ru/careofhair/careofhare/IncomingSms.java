package ru.careofhair.careofhare;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Admin on 09.09.2016.
 * Project name CareOfHare
 */


public class IncomingSms extends BroadcastReceiver {

    DBHelper dbHelper;
    private static final String LOG_TAG = "my_tag";

    @TargetApi(Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {

        dbHelper = new DBHelper(context);
        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                String messageg = "";
                String senderNumg = null;

                assert pdusObj != null;
                for (Object aPdusObj : pdusObj) {
                    String format = bundle.getString("format");
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj, format);
                    String senderNum = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();
                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    messageg = messageg + message;
                    senderNumg = senderNum;

                    // Show Alert
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "Отправитель: " + senderNum + ", текст сообщения: " + message, duration);
                    toast.show();

                } // end for loop

                //String url = "http://careofhair.expertsites.pro/sms/sms-receiver";
                //String json = "{\"senderNum\":\"" + senderNumg + "\",\"message\":\"" + messageg + "\"}";

                //new SendCallAsyncTask().execute(url, json, "1", senderNumg, "10");

                //записываем в БД номер телефона и сообщение

                int is_int = Integer.parseInt(messageg);
                if (is_int > 0)
                    writeContact(dbHelper, senderNumg, messageg, context);

            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }
    }

    private void writeContact(DBHelper dbHelper, String senderNum, String message, Context context) {
        Cursor cursor;
        String sale = "", str = "";

        SimpleDateFormat dfDate_day = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        Calendar c = Calendar.getInstance();
        String dt = dfDate_day.format(c.getTime());

        ContentValues contentValues = new ContentValues();

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        contentValues.put(DBHelper.KEY_PHONE, senderNum);
        contentValues.put(DBHelper.KEY_STATUS, "1");

        Log.d(LOG_TAG, "database write contact");
        cursor = database.query(DBHelper.TABLE_CONTACTS, null, DBHelper.KEY_PHONE + "=?", new String[]{senderNum}, null, null, null);

        if (cursor.moveToFirst()) {
            contentValues.put(DBHelper.KEY_DATE_LAST_VISIT, dt);
            Log.d(LOG_TAG, "Такой контакт уже есть ставим статус 1 и обновляем дату последнего визита");
            database.update(DBHelper.TABLE_CONTACTS, contentValues, DBHelper.KEY_PHONE + "=?", new String[]{senderNum});
        } else {
            contentValues.put(DBHelper.KEY_DATE, dt);
            contentValues.put(DBHelper.KEY_DATE_LAST_VISIT, dt);
            Log.d(LOG_TAG, "insert" + contentValues.toString());
            database.insert(DBHelper.TABLE_CONTACTS, null, contentValues);
        }
        cursor.close();

        contentValues.remove(DBHelper.KEY_DATE_LAST_VISIT);
        contentValues.put(DBHelper.KEY_DATE, dt);
        contentValues.put(DBHelper.KEY_MESS, message);

        database.insert(DBHelper.TABLE_MESSAGE_RECEIVE, null, contentValues);
        Log.d(LOG_TAG, "Сообщение записано в БД");

        cursor = database.query(DBHelper.TABLE_SALE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int phoneIndex = cursor.getColumnIndex(DBHelper.KEY_SALE);
            sale = cursor.getString(phoneIndex);
        }
        cursor.close();
        int sms_num = Integer.parseInt(message);
        Log.d(LOG_TAG, String.valueOf(sms_num));
        if (sms_num > 0 && !sale.isEmpty()) {
            Cursor mes_id = database.query(DBHelper.TABLE_MESSAGE_SEND, null, null, null, null, null, null);
            int idIndex = 0;
            if (mes_id.moveToLast()) idIndex = mes_id.getInt(mes_id.getColumnIndex(DBHelper.KEY_ID));
            mes_id.close();
            Integer full_sum = sms_num;
            sms_num = ((int)Math.ceil((sms_num - ((sms_num * Integer.parseInt(sale)) / 100))/10)*10);
            full_sum = full_sum - sms_num;
            str = context.getString(R.string.sms_send, String.valueOf(idIndex+1), sms_num, full_sum);
            contentValues.put(DBHelper.KEY_MESS, str);
            contentValues.put(DBHelper.KEY_PRICE, sms_num);
            contentValues.put(DBHelper.KEY_SALE, Integer.parseInt(sale));
            contentValues.put(DBHelper.KEY_STATUS, 0);
            database.insert(DBHelper.TABLE_MESSAGE_SEND, null, contentValues);
            Log.d(LOG_TAG, "Сообщение со скидкой помещено в очередь");
            //Toast.makeText(context, "Отправляю смс на номер " + senderNum + ", текст сообщения: " + str, Toast.LENGTH_LONG).show();
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(senderNum, null, str, null, null);
        }

        database.close();
    }


}