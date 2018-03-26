package org.sairaa.mantrirajsekher.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;

import org.sairaa.mantrirajsekher.data.NewsContract.NewsEntry;

import java.security.Provider;

/**
 * Created by praful on 8/24/2017.
 */

public class NewsProvider extends FileProvider {
    /** Tag for the log messages */
    public static final String LOG_TAG = NewsProvider.class.getSimpleName();
    /** URI matcher code for the content URI for the pets table */
    private static final int NEWS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int NEWS_ID = 101;

    private static final int IMAGE_ID = 102;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY, NewsContract.PATH_NEWS, NEWS);

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY, NewsContract.PATH_NEWS + "/#", NEWS_ID);

        sUriMatcher.addURI(NewsContract.CONTENT_AUTHORITY, NewsContract.PATH_IMAGE + "/*", IMAGE_ID);
    }

    private NewsDbHelper mDbHelper;
    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new NewsDbHelper(getContext());
        // TODO: Create and initialize a NewsDbHelper object to gain access to the news database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case NEWS:
                // For the PETS code, query the pets table directly with the given
                    // projection, selection, selection arguments, and sort order. The cursor
                    // could contain multiple rows of the pets table.
                    cursor = database.query(NewsEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder);
                break;
            case NEWS_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = NewsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(NewsEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //Set notification Uri on the Cursor
        //so we know what content uri the cursor was created for
        //If the data at the uri changes, then we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;

    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS:
                Log.i(LOG_TAG,"Test :"+String.valueOf(NewsEntry.CONTENT_LIST_TYPE));
                return NewsEntry.CONTENT_LIST_TYPE;
            case NEWS_ID:
                Log.i(LOG_TAG,"Test :"+String.valueOf(NewsEntry.CONTENT_ITEM_TYPE));
                return NewsEntry.CONTENT_ITEM_TYPE;
            case IMAGE_ID:
                //Log.i(LOG_TAG,"Test :"+String.valueOf(NewsEntry.CONTENT_IMAGE_TYPE));
                return "image/jpg";
            default:
                //Log.i(LOG_TAG,"Test :"+String.valueOf(NewsEntry.CONTENT_LIST_TYPE));
                //Log.i(LOG_TAG,"Test :"+String.valueOf(NewsEntry.CONTENT_ITEM_TYPE));
                //Log.i(LOG_TAG,"Test :"+String.valueOf(NewsEntry.CONTENT_IMAGE_TYPE));
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS:
                Log.i(LOG_TAG,"Test "+String.valueOf(uri));
                return insertNews(uri, contentValues);

            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertNews(Uri uri, ContentValues contentValues) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Insert the new pet with the given values
        long id = database.insert(NewsEntry.TABLE_NAME, null, contentValues);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        // If the ID is -1, then the insertion failed. Log an error and return null.
        Log.i(LOG_TAG, "inserted " + uri +" at :"+id);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // notify all listner that the data has changed for the pet content uri
        getContext().getContentResolver().notifyChange(uri,null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (match) {
            case NEWS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(NewsEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case NEWS_ID:
                // Delete a single row given by the ID in the URI
                selection = NewsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(NewsEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NEWS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case NEWS_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = NewsEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Returns the number of database rows affected by the update statement
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(NewsEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
        // No need to check the breed, any value is valid (including null).

    }

}
