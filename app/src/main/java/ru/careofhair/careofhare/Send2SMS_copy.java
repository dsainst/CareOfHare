package ru.careofhair.careofhare;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Send2SMS_copy extends Service {


    Button sendBtn;
    TextView tv = null;
    Thread thr;

    String SENT      = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    Integer id_sms = null;

    private BroadcastReceiver sent      = null;
    private BroadcastReceiver delivered = null;

    public Send2SMS_copy() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        Toast.makeText(this, "Служба удалена",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Toast.makeText(this, "Служба создана",
                Toast.LENGTH_SHORT).show();

        //tv = (TextView) findViewById(R.id.textView);
        //Регистрация широковещательного приемника: Отправка
        IntentFilter in_sent = new IntentFilter(SENT);
        sent = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                //tv.append(intent.getStringExtra("PARTS")+": ");
                //tv.append(intent.getStringExtra("MSG")+": ");
                switch(getResultCode())
                {
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
        delivered = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                //tv.append(intent.getStringExtra("PARTS")+": ");
                //tv.append(intent.getStringExtra("MSG")+": ");
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        //tv.append("SMS Доставлено\n");
                        String url = "http://careofhair.expertsites.pro/sms/sms-send";
                        String json = "{\"password\":\"update\",\"id\":"+id_sms+",\"delivery\":\"1\"}";
                        new SendCallAsyncTask().execute(url, json, "1");
                        break;
                    case Activity.RESULT_CANCELED:
                        //tv.append("SMS Не доставлено\n");
                        String url2 = "http://careofhair.expertsites.pro/sms/sms-send";
                        String json2 = "{\"password\":\"update\",\"id\":"+id_sms+",\"delivery\":\"0\"}";
                        new SendCallAsyncTask().execute(url2, json2, "1");
                        break;
                }
            }
        };
        registerReceiver(delivered, in_delivered);
        StartApp();
    }


    //Метод отправки SMS сообщения
    final public void SendSMS(String phone, String message)
    {
        SmsManager sms = SmsManager.getDefault();

        ArrayList<String> al_message = new ArrayList<String>();
        al_message = sms.divideMessage(message);

        ArrayList<PendingIntent> al_piSent = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> al_piDelivered = new ArrayList<PendingIntent>();

        for (int i = 0; i < al_message.size(); i++)
        {
            Intent sentIntent = new Intent(SENT);
            sentIntent.putExtra("PARTS", "Часть: "+i);
            sentIntent.putExtra("MSG", "Сообщение: "+al_message.get(i));
            PendingIntent pi_sent = PendingIntent.getBroadcast(this, i, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piSent.add(pi_sent);

            Intent deliveredIntent = new Intent(DELIVERED);
            deliveredIntent.putExtra("PARTS", "Часть: "+i);
            deliveredIntent.putExtra("MSG", "Сообщение: "+al_message.get(i));
            PendingIntent pi_delivered = PendingIntent.getBroadcast(this, i, deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            al_piDelivered.add(pi_delivered);
        }
        sms.sendMultipartTextMessage(phone, null, al_message, al_piSent, al_piDelivered);
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
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, parameter[1]);

                Request request = new Request.Builder()
                        .url(parameter[0])
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                if (parameter[2]!=null && parameter[2]!="1") {
                    try {
                        String datas = "";
                        JSONObject myJson = new JSONObject(response.body().string());

                        id_sms = myJson.optInt("id");
                        String message = myJson.optString("message");
                        String receiver = myJson.optString("receiver");

                        datas = "Node" + id_sms + " : \n id= " + id_sms + " \n message= " + message + " \n receiver= " + receiver + " \n ";

                        Log.d("test", datas);
                        if (message != "" && (receiver.length() == 11 || receiver.length() == 12)) {
                            try {
                                SendSMS(receiver, message);
                            }  catch(Exception e){
                                Log.d("test", e.toString());
                            }
                        }

                    } catch (JSONException e) {
                        Log.d("test", "Смс очередь пустая");
                    }
                }
            } catch(Exception e){
                Log.d("test", "Нет интернета!");
            }

            return 1;
        }
    }

    public void StartApp() {
        try {
            thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        String url = "http://careofhair.expertsites.pro/sms/sms-send";
                        String json = "{\"password\":\"get\"}";
                        new SendCallAsyncTask().execute(url, json, "0");

                        try {
                            Thread.sleep(10000);
                        } catch (Exception e) {
                            Log.d("test", "ERROR 2");
                        }
                    }
                }
            });

            thr.setDaemon(true);
            thr.start();

        } catch(Exception e){
            Log.d("test", "Нет интернета!");
        }
    }

    @Override
    public void onDestroy()
    {
        Thread.currentThread().interrupt();
        if(sent != null)
            unregisterReceiver(sent);
        if(delivered != null)
            unregisterReceiver(delivered);
        Toast.makeText(this, "Служба остановлена",
                Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }


}