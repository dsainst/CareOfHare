package ru.careofhair.careofhare;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Send2SMS extends Service {

    Thread thr;
    DBHelper dbHelper;
    SimpleDateFormat dfDate_day;
    String dt = "";

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    private BroadcastReceiver sent = null;
    private BroadcastReceiver delivered = null;

    public Send2SMS() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Toast.makeText(this, "Служба по рассылке смс удалена",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Служба по рассылке смс создана. Смс начнут рассылаться через 5 секунд",
                Toast.LENGTH_SHORT).show();

        dbHelper = new DBHelper(this);
        dfDate_day = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        //Регистрация широковещательного приемника: Отправка
        IntentFilter in_sent = new IntentFilter(SENT);
        sent = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //tv.append(intent.getStringExtra("PARTS")+": ");
                //tv.append(intent.getStringExtra("MSG")+": ");
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        //tv.setText("SMS Отправлено\n");
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        //tv.setText("Общий сбой\n");
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        //tv.setText("Нет сети\n");
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        //tv.setText("Null PDU\n");
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        //tv.setText("Нет связи\n");
                        break;
                }

            }
        };
        registerReceiver(sent, in_sent);

        //Регистрация широковещательного приемника: Доставка
        IntentFilter in_delivered = new IntentFilter(DELIVERED);
        delivered = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:

                        break;
                    case Activity.RESULT_CANCELED:

                        break;
                }
            }
        };
        registerReceiver(delivered, in_delivered);

        SystemClock.sleep(5000);

        new SendCallAsyncTask().execute();
    }


    //Метод отправки SMS сообщения
    final public void SendSMS(String phone, String message) {
        SmsManager sms = SmsManager.getDefault();

        ArrayList<String> al_message = new ArrayList<String>();
        al_message = sms.divideMessage(message);

        ArrayList<PendingIntent> al_piSent = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> al_piDelivered = new ArrayList<PendingIntent>();

        for (int i = 0; i < al_message.size(); i++) {
            Intent sentIntent = new Intent(SENT);
            sentIntent.putExtra("PARTS", "Часть: " + i);
            sentIntent.putExtra("MSG", "Сообщение: " + al_message.get(i));
            PendingIntent pi_sent = PendingIntent.getBroadcast(this, i, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piSent.add(pi_sent);

            Intent deliveredIntent = new Intent(DELIVERED);
            deliveredIntent.putExtra("PARTS", "Часть: " + i);
            deliveredIntent.putExtra("MSG", "Сообщение: " + al_message.get(i));
            PendingIntent pi_delivered = PendingIntent.getBroadcast(this, i, deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piDelivered.add(pi_delivered);
        }
        sms.sendMultipartTextMessage(phone, null, al_message, al_piSent, al_piDelivered);
    }

    public void StartApp() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Calendar calendar = Calendar.getInstance();
        dt = dfDate_day.format(calendar.getTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_DATE, dt);

        Cursor cursor = db.rawQuery("select snl.*, sn."+DBHelper.KEY_MESS+" from " + DBHelper.TABLE_SEND_NOW_LOG + " as snl inner join " + DBHelper.TABLE_SEND_NOW + " as sn on sn." + DBHelper.KEY_ID + "=snl." + DBHelper.KEY_MESS_ID + "  where " + DBHelper.KEY_STATUS + "=? ", new String[]{"0"});

        try {
            if (cursor.moveToFirst()) {
                do {
                    int intId = cursor.getColumnIndex(DBHelper.KEY_ID);
                    int intMess = cursor.getColumnIndex(DBHelper.KEY_MESS);
                    int intPhone = cursor.getColumnIndex(DBHelper.KEY_PHONE);
                    String id = cursor.getString(intId);
                    String message = cursor.getString(intMess);
                    String phone = cursor.getString(intPhone);

                    SendSMS(phone, message);

                    contentValues.put(DBHelper.KEY_STATUS, 1);

                    db.update(DBHelper.TABLE_SEND_NOW_LOG, contentValues, DBHelper.KEY_ID + "=?", new String[]{id});

                    SystemClock.sleep(1000);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("test", "Ошибочка!");
        }
        cursor.close();
        db.close();

        stopSelf();
    }

    class SendCallAsyncTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // [... Обновите индикатор хода выполнения, уведомления или другой
            // элемент пользовательского интерфейса ...]
        }

        @Override
        protected Integer doInBackground(String... parameter) {
            try {
                StartApp();
            } catch(Exception e){
                Log.d("test", "Ошибочка!");
            }

            return 1;
        }
    }

    @Override
    public void onDestroy() {
        Thread.currentThread().interrupt();
        if (sent != null)
            unregisterReceiver(sent);
        if (delivered != null)
            unregisterReceiver(delivered);
        Toast.makeText(this, "Служба по рассылке смс остановлена",
                Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


}