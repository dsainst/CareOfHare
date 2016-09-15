package ru.careofhair.careofhare;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Сергей on 10.09.2016.
 * Project name CareOfHare
 */
public class SynchronizeDB {

    class exportDB extends AsyncTask<String, Integer, Integer> {

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

                if (parameter[2] != null && parameter[2] != "0") {
                    try {
                        Log.d("test", "test#1");
                        final JSONObject jsonObject = new JSONObject(response.body().string());
                        final String sms = jsonObject.getString("sms");
                        final String success = jsonObject.getString("success");
                        Log.d("test2", sms);
                        Log.d("test3", success);
                        return 1;
                    } catch (JSONException e) {
                        Log.d("test", e.toString());
                    }
                }
            } catch (Exception e) {
                Log.d("test", "ERROR HTTP");
            }

            return 1;
        }
    }

    public void importDB() {

    }
}
