package com.duodian.admore.android.sdk.log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LogDbHelper extends SQLiteOpenHelper {
    private static final String COMMA_SEP = ",";
    public static final String DATABASE_NAME = "AdmoreAdSdkLog.db";
    public static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE AdmoreAdSdkLogTable (_id INTEGER PRIMARY KEY,time TEXT,content TEXT,column1 TEXT,column2 TEXT )";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS AdmoreAdSdkLogTable";
    private static final String TEXT_TYPE = " TEXT";

    public LogDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
