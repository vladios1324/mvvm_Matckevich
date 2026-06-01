package com.example.mvvm_matckevich.datas.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbContext extends SQLiteOpenHelper {
    public static SQLiteDatabase sqLiteDatabase;

    public DbContext(Context context) {
        super(context, "DbWeather", null, 1);
        sqLiteDatabase = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE Days (" +
                        "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "Name TEXT NOT NULL, " +
                        "Temp INTEGER NOT NULL, " +
                        "Codition TEXT NOT NULL);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
