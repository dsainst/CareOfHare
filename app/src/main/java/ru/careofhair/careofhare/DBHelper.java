package ru.careofhair.careofhare;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Сергей on 10.09.2016.
 * Project name CareOfHare
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "careOfHare"; // имя базы данных
    public static final String TABLE_CONTACTS = "userProfile"; // сохраняются контакты
    public static final String TABLE_SALE = "sale"; // сохраняется скидка
    public static final String TABLE_MESSAGE_RECEIVE = "message"; // полученные смс
    public static final String TABLE_MESSAGE_SEND = "message_send"; // отправленные смс с пересчитанной суммой заказа
    public static final String TABLE_SCHEDULER = "scheduler"; // смс текст в рассылке по расписанию
    public static final String TABLE_SCHEDULER_LOG = "scheduler_log"; // разосланные смс`ки по расписанию
    public static final String TABLE_SEND_NOW = "send_now"; // таблица с текстом рассылки прямо сейчас
    public static final String TABLE_SEND_NOW_LOG = "send_now_log"; // разосланные смс`ки TABLE_SEND_NOW

    public static final String KEY_ID = "_id";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_DATE = "dateReg";
    public static final String KEY_DATE_LAST_VISIT = "dateLastVisit";
    public static final String KEY_DATE_SEND = "dateSend";
    public static final String KEY_SALE = "value";
    public static final String KEY_MESS = "message";
    public static final String KEY_MESS_ID = "message_id";
    public static final String KEY_PRICE = "price";
    public static final String KEY_STATUS = "status";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_CONTACTS + "(" + KEY_ID
                + " integer primary key," + KEY_PHONE + " text," + KEY_DATE + " text," + KEY_DATE_LAST_VISIT + " text," + KEY_STATUS + " text" + ")");
        db.execSQL("create table if not exists " + TABLE_SALE + "(" + KEY_ID
                + " integer primary key," + KEY_SALE + " integer" + ")");
        db.execSQL("create table if not exists " + TABLE_MESSAGE_RECEIVE + "(" + KEY_ID
                + " integer primary key," + KEY_PHONE + " text," + KEY_DATE + " text," + KEY_MESS + " text" + ")");
        db.execSQL("create table if not exists " + TABLE_MESSAGE_SEND + "(" + KEY_ID
                + " integer primary key," + KEY_PHONE + " text," + KEY_DATE + " text," + KEY_PRICE + " text," + KEY_SALE + " integer," + KEY_MESS + " text," + KEY_MESS_ID + " integer," + KEY_STATUS + " integer" + ")");
        //текст в рассылке по расписанию
        db.execSQL("create table if not exists " + TABLE_SCHEDULER + "(" + KEY_ID
                + " integer primary key," + KEY_DATE + " text," + KEY_MESS + " text" + ")");
        db.execSQL("create table if not exists " + TABLE_SCHEDULER_LOG + "(" + KEY_ID
                + " integer primary key," + KEY_PHONE + " text," + KEY_DATE + " text," + KEY_DATE_SEND + " text," + KEY_SALE + " integer," + KEY_MESS_ID + " text," + KEY_STATUS + " integer" + ")");
        //текст в рассылке прямо сейчас
        db.execSQL("create table if not exists " + TABLE_SEND_NOW + "(" + KEY_ID
                + " integer primary key," + KEY_DATE + " text," + KEY_MESS + " text" + ")");
        db.execSQL("create table if not exists " + TABLE_SEND_NOW_LOG + "(" + KEY_ID
                + " integer primary key," + KEY_PHONE + " text," + KEY_DATE + " text," + KEY_DATE_SEND + " text," + KEY_SALE + " integer," + KEY_MESS_ID + " text," + KEY_STATUS + " integer" + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_CONTACTS);
        db.execSQL("drop table if exists " + TABLE_SALE);
        db.execSQL("drop table if exists " + TABLE_MESSAGE_RECEIVE);
        db.execSQL("drop table if exists " + TABLE_MESSAGE_SEND);
        db.execSQL("drop table if exists " + TABLE_SCHEDULER);
        db.execSQL("drop table if exists " + TABLE_SCHEDULER_LOG);
        db.execSQL("drop table if exists " + TABLE_SEND_NOW);
        db.execSQL("drop table if exists " + TABLE_SEND_NOW_LOG);

        onCreate(db);

    }
}
