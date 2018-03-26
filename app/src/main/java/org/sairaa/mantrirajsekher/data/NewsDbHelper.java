package org.sairaa.mantrirajsekher.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.sairaa.mantrirajsekher.data.NewsContract.NewsEntry;

/**
 * Created by praful on 8/24/2017.
 */

public class NewsDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = NewsDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "raas.db";
    //private static final String DATABASE_NAME = "raasa.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 8;
    /**
     * Constructs a new instance of .
     *
     * @param context of the app
     */
    public NewsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE =  "CREATE TABLE " + NewsEntry.TABLE_NAME + " ("
                + NewsEntry._ID + " INTEGER PRIMARY KEY, "
                + NewsEntry.IMAGE_LOC + " TEXT, "
                + NewsEntry.HEADING + " TEXT NOT NULL, "
                + NewsEntry.SHORT_CONTENT + " TEXT NOT NULL, "
                + NewsEntry.SOURCE + " TEXT, "
                + NewsEntry.CONTENT_ADDRESS + " TEXT, "
                + NewsEntry.DATE + " TEXT, "
                + NewsEntry.LIKE + " INTEGER, "
                + NewsEntry.LOCAL_NEWS + " INTEGER NOT NULL DEFAULT 0, "
                + NewsEntry.RAJ_NEWS + " INTEGER NOT NULL DEFAULT 0, "
                + NewsEntry.STEEL_NEWS + " INTEGER NOT NULL DEFAULT 0, "
                + NewsEntry.NATIONAL_NEWS + " INTEGER NOT NULL DEFAULT 0, "
                + NewsEntry.INTERNATIONAL_NEWS + " INTEGER NOT NULL DEFAULT 0, "
                + NewsEntry.OTHERS + " INTEGER NOT NULL DEFAULT 0);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }
    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
