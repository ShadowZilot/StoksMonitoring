package com.shadow_zilot.stoksmonitoring.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.shadow_zilot.stoksmonitoring.database.DataSchema.Cols.*;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String sDataBaseName = "Companies.db";
    private static final int sVersion = 1;

    public DataBaseHelper(Context context) {
        super(context, sDataBaseName, null, sVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                String.format(
                  "CREATE VIRTUAL TABLE %1s USING fts4(%2s," +
                          " %3s, %4s, %5s, %6s, %7s, %8s)",
                        DataSchema.NAME, TICKER, NAME, STOCK_PRICE, PRICES_LIST,
                        DELTA_PRICE, CURRENCY_NAME, IS_FAVORITE
                )
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
