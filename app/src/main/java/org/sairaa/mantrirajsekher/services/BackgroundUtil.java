package org.sairaa.mantrirajsekher.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sairaa.mantrirajsekher.NewsActivity;
import org.sairaa.mantrirajsekher.R;
import org.sairaa.mantrirajsekher.data.NewsContract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;

import org.sairaa.mantrirajsekher.data.NewsContract.NewsEntry;
import org.sairaa.mantrirajsekher.notification.NewNotification;

import static android.R.attr.id;


/**
 * Created by praful on 9/10/2017.
 */

public class BackgroundUtil {
    private static Bitmap bitmap;
    private static final int INSERT_NEW_RECORD = 101;
    private static final int UPDATE_EXISTING_RECORD = 201;
    private static final int DELETE_RECORD = 301;
    public static final String LOG_TAG_BUTIL = BackgroundUtil.class.getName();



    public static String doDatabaseUpdate(String inserturl, String updateurl, String deleteUri, BackgroundServices myService) {

        String notifyString = insertNewsToSqlite(inserturl,myService);

        updateNewsToSqlite(updateurl,myService);

        deleteNewsRowFromSqlite(myService);

        if(notifyString != null){
            return notifyString;
        }


        return null;
    }

    private static void deleteNewsRowFromSqlite(BackgroundServices myService) {
        //Uri mRecentUri = Uri.withAppendedPath(NewsEntry.CONTENT_URI,String.valueOf(id));
        String[] projection = {
                NewsEntry._ID, NewsEntry.IMAGE_LOC};
        // The query method justifies : "SELECT id FROM news ORDER BY id DESC LIMIT 1" statement
        Cursor cursor = myService.getContentResolver().query(NewsEntry.CONTENT_URI,projection,
                null,
                null,
                null);
        if(cursor.getCount()>200){
            cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex(NewsEntry._ID);
            int currentId = cursor.getInt(idIndex);

            int imageIndex = cursor.getColumnIndex(NewsEntry.IMAGE_LOC);
            String imageLoc = cursor.getString(imageIndex);

            Uri mCurrentUri = Uri.withAppendedPath(NewsEntry.CONTENT_URI,String.valueOf(currentId));
            int row = myService.getContentResolver().delete(mCurrentUri,null,null);
            Log.i(LOG_TAG_BUTIL,"TestD: deletedNews() is called: "+row);
            Log.i(LOG_TAG_BUTIL,"TestD: deletedNews() is called: "+currentId);

            File file = new File(imageLoc);
            if(file.isDirectory()){
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    new File(file , children[i]).delete();
                }
            }
        }
        cursor.close();
    }

    private static void updateNewsToSqlite(String updateurl, BackgroundServices myService) {

        URL url = createUrl(updateurl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, myService,UPDATE_EXISTING_RECORD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Cursor ok = extractFeatureFromJsonUpdate(jsonResponse,myService,UPDATE_EXISTING_RECORD);

    }

    private static Cursor extractFeatureFromJsonUpdate(String newsJSON, BackgroundServices myService, int updateExistingRecord) {
        Cursor cursor = null;
        String imageUriAddress = "";
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // Extract the JSONArray associated with the key called "top_news",
            // which represents a list of features (or News).
            JSONArray newsArray = baseJsonResponse.getJSONArray("top_news");
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject currentNews = newsArray.getJSONObject(i);
                int id = currentNews.getInt("id");
                int currentId = id;
                // findEditedOrNot() , method check weather the recod is edited or not. It
                // checks OTHERS column value. It compares server table OTHERS column VS SQLite table OTHERS column
                // if same no need to edit and if different edit the row.
                int others_sql = findEditedOrNot(myService, id);
                int otherNews = currentNews.getInt("others");
                Log.i(LOG_TAG_BUTIL," Test NO: "+i+ " others_sql :" +others_sql+"  otherNews: "+otherNews );
                if(others_sql != otherNews){
                    String imageLocation = currentNews.getString("image_loc");
                    try {
                        //getBitmapFromUrl() retribes bimap from url

                        bitmap = getBitmapFromUrl(imageLocation);
                        imageUriAddress = uploadImageToInternalStorage(bitmap,myService,String.valueOf(currentId));
                        Log.i(LOG_TAG_BUTIL," Test NO: "+i+ " image loaded" );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String heading = currentNews.getString("heading");
                    String shortContent = currentNews.getString("short_content");
                    String source = currentNews.getString("source");

                    String contentAddress = currentNews.getString("content_address");
                    String dataOfInsert = currentNews.getString("date");
                    int likeCount = currentNews.getInt("like");
                    int isLocalNews = currentNews.getInt("local_news");
                    int isRajNews = currentNews.getInt("raj_news");
                    int isSteelNews = currentNews.getInt("steel_news");
                    int isNationalNews = currentNews.getInt("national_news");
                    int isInternationalNews = currentNews.getInt("international_news");

                    // Creating content values that to be passed to contentProvider
                    ContentValues values = new ContentValues();
                    values.put(NewsContract.NewsEntry._ID, id);
                    values.put(NewsContract.NewsEntry.IMAGE_LOC,imageUriAddress);
                    values.put(NewsContract.NewsEntry.HEADING, heading);
                    values.put(NewsContract.NewsEntry.SHORT_CONTENT,shortContent);
                    values.put(NewsContract.NewsEntry.SOURCE, source);
                    values.put(NewsContract.NewsEntry.CONTENT_ADDRESS,contentAddress);
                    values.put(NewsContract.NewsEntry.DATE,dataOfInsert);
                    values.put(NewsContract.NewsEntry.LIKE,likeCount);
                    values.put(NewsContract.NewsEntry.LOCAL_NEWS,isLocalNews);
                    values.put(NewsContract.NewsEntry.RAJ_NEWS,isRajNews);
                    values.put(NewsContract.NewsEntry.STEEL_NEWS,isSteelNews);
                    values.put(NewsContract.NewsEntry.NATIONAL_NEWS,isNationalNews);
                    values.put(NewsContract.NewsEntry.INTERNATIONAL_NEWS,isInternationalNews);
                    values.put(NewsContract.NewsEntry.OTHERS,otherNews);
                    //Update News if updated NEWS found in the Master database

                    updateNews(myService,values,id);
                }




                //NewsObject newsObject = new NewsObject(bitmap,heading,shortContent,source,contentAddress,likeCount);
                //topNews.add(newsObject);
                //bitmap = null;
            }
            /*cursor = myService.getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder);*/
            if (cursor != null) {
                try {
                    // Ensure the cursor window is filled.
                    cursor.getCount();
                    //cursor.registerContentObserver(mObserver);
                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }
            //return cursor;
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }
        return cursor;
    }

    private static int findEditedOrNot(BackgroundServices myService, int id) {
        Log.i(LOG_TAG_BUTIL,"Test : findEditedOrNOt( ) is called");
        Uri mRecentUri = Uri.withAppendedPath(NewsEntry.CONTENT_URI,String.valueOf(id));
        String[] projection = {
                NewsEntry.OTHERS};
        // The query method justifies : "SELECT id FROM news ORDER BY id DESC LIMIT 1" statement
        Cursor lastRecordCursor = myService.getContentResolver().query(mRecentUri,projection,
                null,
                null,
                null);
        if(lastRecordCursor.getCount() > 0){
            // DETERMINE column index
            int index = lastRecordCursor.getColumnIndex(NewsEntry.OTHERS);
            lastRecordCursor.moveToFirst();
            Log.i(LOG_TAG_BUTIL, "Test: "+String.valueOf(lastRecordCursor.getInt(index)));
            int  othersRecord = lastRecordCursor.getInt(index);
            lastRecordCursor.close();
            return othersRecord;


        }else{
            lastRecordCursor.close();
            return 505;
        }
    }

    private static String insertNewsToSqlite(String inserturl, BackgroundServices myService) {
        URL url = createUrl(inserturl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url, myService,INSERT_NEW_RECORD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String notify = extractFeatureFromJson(jsonResponse,myService,INSERT_NEW_RECORD);


        return notify;
    }

    private static String extractFeatureFromJson(String newsJSON, BackgroundServices myService, int operation) {
        Cursor cursor = null;
        String notificationString = null;
        String imageUriAddress = "";
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // Extract the JSONArray associated with the key called "top_news",
            // which represents a list of features (or earthquakes).
            JSONArray newsArray = baseJsonResponse.getJSONArray("top_news");
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject currentNews = newsArray.getJSONObject(i);
                int id = currentNews.getInt("id");
                int currentId = id;
                String imageLocation = currentNews.getString("image_loc");
                try {
                    //getBitmapFromUrl() retribes bimap from url

                    bitmap = getBitmapFromUrl(imageLocation);
                    imageUriAddress = uploadImageToInternalStorage(bitmap,myService,String.valueOf(currentId));
                    Log.i(LOG_TAG_BUTIL," Test NO: "+i+ " image loaded" );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String heading = currentNews.getString("heading");

                String shortContent = currentNews.getString("short_content");
                String source = currentNews.getString("source");

                String contentAddress = currentNews.getString("content_address");
                String dataOfInsert = currentNews.getString("date");
                int likeCount = currentNews.getInt("like");
                int isLocalNews = currentNews.getInt("local_news");
                int isRajNews = currentNews.getInt("raj_news");
                int isSteelNews = currentNews.getInt("steel_news");
                int isNationalNews = currentNews.getInt("national_news");
                int isInternationalNews = currentNews.getInt("international_news");
                int otherNews = currentNews.getInt("others");
                // Creating content values that to be passed to contentProvider
                ContentValues values = new ContentValues();
                values.put(NewsContract.NewsEntry._ID, id);
                values.put(NewsContract.NewsEntry.IMAGE_LOC,imageUriAddress);
                values.put(NewsContract.NewsEntry.HEADING, heading);
                values.put(NewsContract.NewsEntry.SHORT_CONTENT,shortContent);
                values.put(NewsContract.NewsEntry.SOURCE, source);
                values.put(NewsContract.NewsEntry.CONTENT_ADDRESS,contentAddress);
                values.put(NewsContract.NewsEntry.DATE,dataOfInsert);
                values.put(NewsContract.NewsEntry.LIKE,likeCount);
                values.put(NewsContract.NewsEntry.LOCAL_NEWS,isLocalNews);
                values.put(NewsContract.NewsEntry.RAJ_NEWS,isRajNews);
                values.put(NewsContract.NewsEntry.STEEL_NEWS,isSteelNews);
                values.put(NewsContract.NewsEntry.NATIONAL_NEWS,isNationalNews);
                values.put(NewsContract.NewsEntry.INTERNATIONAL_NEWS,isInternationalNews);
                values.put(NewsContract.NewsEntry.OTHERS,otherNews);
                //inserting News if new NEWS found in the Master database
                switch (operation){
                    case INSERT_NEW_RECORD:
                        insertNews(myService, values);
                        notificationString = heading;


                        break;
                    case UPDATE_EXISTING_RECORD:
                        updateNews(myService,values,id);
                }


                //NewsObject newsObject = new NewsObject(bitmap,heading,shortContent,source,contentAddress,likeCount);
                //topNews.add(newsObject);
                //bitmap = null;
            }
            /*cursor = myService.getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder);*/
            if (cursor != null) {
                try {
                    // Ensure the cursor window is filled.
                    cursor.getCount();
                    //cursor.registerContentObserver(mObserver);
                } catch (RuntimeException ex) {
                    cursor.close();
                    throw ex;
                }
            }
            //return cursor;
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the news JSON results", e);
        }
        return notificationString;
    }

    private static void updateNews(BackgroundServices myService, ContentValues values, int id) {
        Uri mCurrentUri = Uri.withAppendedPath(NewsEntry.CONTENT_URI,String.valueOf(id));
        int row = myService.getContentResolver().update(mCurrentUri,values,null,null);
        Log.i(LOG_TAG_BUTIL,"Test: updateNews() is called: "+row);
    }

    private static void insertNews(BackgroundServices myService, ContentValues values) {
        Uri newUri;
        newUri = myService.getContentResolver().insert(NewsContract.NewsEntry.CONTENT_URI,values);
        Log.i(LOG_TAG_BUTIL,String.valueOf(newUri));
    }

    private static String uploadImageToInternalStorage(Bitmap bitmap, BackgroundServices myService, String s) {
        Log.i(LOG_TAG_BUTIL,"Test : uploadImageToInternalStorage( ) is called");
        // path to /data/data/yourapp/app_data/imageDir
        //File directory = context.getDir("imageDira", Context.MODE_PRIVATE);
        // Create imageDir
        //File mypath=new File(directory,s+".jpg");
        String filename = s+".jpg";
        File file = new File(myService.getFilesDir(), filename);

        //Log.i(LOG_TAG,"Test : "+String.valueOf(mypath));
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(LOG_TAG_BUTIL,"Test : "+String.valueOf(file.getAbsolutePath()));
        return file.getAbsolutePath();

    }

    private static Bitmap getBitmapFromUrl(String url) throws IOException {
        Log.i(LOG_TAG_BUTIL,"Test : getBitmapUri( ) is called");
        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream((InputStream)connection.getContent(), null, null);
    }


    private static String makeHttpRequest(URL url, BackgroundServices myService, int operation) throws IOException {
        String jsonResponse = "";
        Log.i(LOG_TAG_BUTIL,"Test : makeHttp( ) is called");
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(60000 /* milliseconds */);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            // This findLastRecord() method finds last record of SQLite database table
            // @param context is passed
            String last_record = "";
            switch (operation){
                case INSERT_NEW_RECORD:
                    last_record = findLastRecord(myService);
                    break;
                case UPDATE_EXISTING_RECORD:
                    last_record = "505";
                    break;
                case DELETE_RECORD:
                    last_record = "501";
                    break;
            }

            String post_data = URLEncoder.encode("last_record","UTF-8")+"="+URLEncoder.encode(last_record,"UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG_BUTIL, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG_BUTIL, "Problem retrieving the News JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static String findLastRecord(BackgroundServices myService) {
        Log.i(LOG_TAG_BUTIL,"Test : finfLastRecord( ) is called");
        String[] projection = {
                NewsContract.NewsEntry._ID};
        // The query method justifies : "SELECT id FROM news ORDER BY id DESC LIMIT 1" statement
        Cursor lastRecordCursor = myService.getContentResolver().query(NewsContract.NewsEntry.CONTENT_URI,projection,
                null,
                null,
                NewsContract.NewsEntry._ID+ " DESC "+" LIMIT 1");
        if(lastRecordCursor.getCount() > 0){
            // DETERMINE column index
            int index = lastRecordCursor.getColumnIndex(NewsContract.NewsEntry._ID);
            lastRecordCursor.moveToFirst();
            Log.i(LOG_TAG_BUTIL, "Test: closed"+String.valueOf(lastRecordCursor.getInt(index)));
            String lastRecord = String.valueOf(lastRecordCursor.getInt(index));
            lastRecordCursor.close();
            return lastRecord;


        }else{
            Log.i(LOG_TAG_BUTIL, "Test: closed");
            lastRecordCursor.close();
            return String.valueOf(0);
        }

    }


    private static URL createUrl(String urlNews) {
        URL url2 = null;
        try{
            url2 = new URL(urlNews);
        }catch (MalformedURLException e) {
            Log.e(LOG_TAG_BUTIL, "Error with creating URL ", e);
        }
        return url2;
    }

    public static void showNotification(BackgroundServices myService, String s){
        int uniqueInteger = 0;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(myService);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.mantrileblue);
            builder.setLargeIcon(BitmapFactory.decodeResource(myService.getResources(), R.drawable.mantrilargeicon));
            builder.setColor(myService.getResources().getColor(R.color.colorAccent));
        } else {
            builder.setSmallIcon(R.drawable.mantrileblue);
        }
        //builder.setSmallIcon(R.drawable.mantrimall);
        builder.setContentTitle("mantri Rajasekhar");

        builder.setContentText(s);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(uri);
        builder.setAutoCancel(true);

        Intent intent = new Intent(myService, NewsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(myService);
        stackBuilder.addParentStack(NewNotification.class);
        stackBuilder.addNextIntent(intent);
        uniqueInteger = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        Log.i(LOG_TAG_BUTIL, "Test : "+uniqueInteger);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) myService.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(uniqueInteger, builder.build());
        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(myService);
        builder.setSmallIcon(R.mipmap.ic_mantri);
       // builder.setSmallIcon(R.mipmap.ic_mantri,1);
        builder.setContentTitle("Mantri Rajasekhar");
        //builder.setContentText(s);
        //builder.setAutoCancel(true);
        Intent intent = new Intent(myService, NewsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(myService);
        stackBuilder.addParentStack(NewsActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_NO_CREATE);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) myService.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());*/


    }
    
}
