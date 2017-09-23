package com.example.android.wines.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.wines.data.WineContract.WineEntry;

/**
 * Database helper for Wines app. Manages database creation and version management.
 */
public class WineDbHelper extends SQLiteOpenHelper {

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "wine_inventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link WineDbHelper}.
     *
     * @param context of the app
     */
    public WineDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a String that contains the SQL statement to create the wines table
        String SQL_CREATE_WINES_TABLE = "CREATE TABLE " + WineEntry.TABLE_NAME + " ("
                + WineEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + WineEntry.COLUMN_WINE_NAME + " TEXT NOT NULL, "
                + WineEntry.COLUMN_WINE_WINERY + " TEXT NOT NULL, "
                + WineEntry.COLUMN_WINE_YEAR + " INTEGER NOT NULL, "
                + WineEntry.COLUMN_WINE_QUANTITY + " INTEGER NOT NULL DEFAULT 0,"
                + WineEntry.COLUMN_WINE_PRICE + " INTEGER NOT NULL DEFAULT 0); ";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_WINES_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}//End of WineDbHelper
