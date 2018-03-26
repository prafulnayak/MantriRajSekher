package org.sairaa.mantrirajsekher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sairaa.mantrirajsekher.data.NewsContract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
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
import org.sairaa.mantrirajsekher.data.NewsContract.NewsEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.R.attr.bitmap;
import static android.R.id.list;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.text.Normalizer.NO;
import static org.sairaa.mantrirajsekher.NewsActivity.LOG_TAG;

/**
 * Created by praful on 8/6/2017.
 */
public class QueryUtil {
    private static Bitmap bitmap;
    private static final int MAX_RETRIES = 3;

    public static final String LOG_TAG = QueryUtil.class.getName();
    public static Cursor fatchNewsInfo(String mURL, Context context1, Uri mUri, String[] mProjection, String mSelection, String[] mSelectionArgs, String mSortOrder) throws IOException {
        URL url;
        // Create URL object
        Log.i(LOG_TAG, "TestUrl : "+(findLastRecord(context1)));
        //for 1st
        if(findLastRecord(context1) == String.valueOf(0)){
            url = createUrl("http://sairaa.org/mantri_rajsekhar/get_first15_json_details.php");
            Log.i(LOG_TAG, "TestUrl : 0" );
        }else{
            url = createUrl(mURL);
            Log.i(LOG_TAG, "TestUrl : 1" );
        }

        Log.i(LOG_TAG,"Test : FatchNewsInfo( ) is called");
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        jsonResponse = makeHttpRequest(url,context1 );
        Cursor ok = extractFeatureFromJson(jsonResponse,context1,mUri,mProjection,mSelection,mSelectionArgs,mSortOrder);
        return ok;
    }

    private static Cursor extractFeatureFromJson(String newsJSON, Context context, Uri mUri, String[] mProjection, String mSelection, String[] mSelectionArgs, String mSortOrder) {
        Log.i(LOG_TAG, " Test : extractFeatureFromJson() is called");
        Cursor cursor = null;
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
                    imageUriAddress = uploadImageToInternalStorage(bitmap,context,String.valueOf(currentId));
                    Log.i(LOG_TAG," Test NO: "+i+ " image loaded" );
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
                values.put(NewsEntry._ID, id);
                values.put(NewsEntry.IMAGE_LOC,imageUriAddress);
                values.put(NewsEntry.HEADING, heading);
                values.put(NewsEntry.SHORT_CONTENT,shortContent);
                values.put(NewsEntry.SOURCE, source);
                values.put(NewsEntry.CONTENT_ADDRESS,contentAddress);
                values.put(NewsEntry.DATE,dataOfInsert);
                values.put(NewsEntry.LIKE,likeCount);
                values.put(NewsEntry.LOCAL_NEWS,isLocalNews);
                values.put(NewsEntry.RAJ_NEWS,isRajNews);
                values.put(NewsEntry.STEEL_NEWS,isSteelNews);
                values.put(NewsEntry.NATIONAL_NEWS,isNationalNews);
                values.put(NewsEntry.INTERNATIONAL_NEWS,isInternationalNews);
                values.put(NewsEntry.OTHERS,otherNews);
                //inserting News if new NEWS found in the Master database
                insertNews(context, values);

                //NewsObject newsObject = new NewsObject(bitmap,heading,shortContent,source,contentAddress,likeCount);
                //topNews.add(newsObject);
                //bitmap = null;
            }
            cursor = context.getContentResolver().query(mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder);
            if (cursor != null) {
                try {
                    // Ensure the cursor window is filled.
                    Log.i(LOG_TAG, " Test : cursor count :"+cursor.getCount());
                    //cursor.registerContentObserver(mObserver);
                } catch (RuntimeException ex) {
                    Log.i(LOG_TAG, " Test : cursor count :"+cursor.getCount());
                    cursor.close();
                    throw ex;
                }
            }
            //return cursor;
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.i(LOG_TAG, " Test : Problem parsing the news JSON results");
        }
        Log.i(LOG_TAG, " Test : returning cursor");
        return cursor;
    }

    private static String uploadImageToInternalStorage(Bitmap bitmap, Context context, String s) {
        Log.i(LOG_TAG,"Test : uploadImageToInternalStorage( ) is called");
        // path to /data/data/yourapp/app_data/imageDir
        //File directory = context.getDir("imageDira", Context.MODE_PRIVATE);
        // Create imageDir
        //File mypath=new File(directory,s+".jpg");
        String filename = s+".jpg";
        File file = new File(context.getFilesDir(), filename);

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
        Log.i(LOG_TAG,"Test : "+String.valueOf(file.getAbsolutePath()));
        return file.getAbsolutePath();
    }

    private static void insertNews(Context context, ContentValues values) {


        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        Uri newUri;
        newUri = context.getContentResolver().insert(NewsEntry.CONTENT_URI,values);
        Log.i(LOG_TAG,String.valueOf(newUri));
    }

    private static Bitmap getBitmapFromUrl(String url) throws IOException {
        Log.i(LOG_TAG,"Test : getBitmapUri( ) is called");
        URLConnection connection = null;
        try {
            connection = new URL(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream((InputStream)connection.getContent(), null, null);
    }

    private static String makeHttpRequest(URL url, Context contextCheck) throws IOException {
        String jsonResponse = "";
        Log.i(LOG_TAG,"Test : makeHttp( ) is called");
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(20000 /* milliseconds */);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            //urlConnection.setRequestProperty();
            //urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            OutputStream outputStream = urlConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            /*Date now = new Date();
            Date alsoNow = Calendar.getInstance().getTime();
            String nowAsString = new SimpleDateFormat("yyyy-MM-dd").format(now);
            String nowAsString1 = new SimpleDateFormat("dd").format(now);
            int datef = Integer.parseInt(nowAsString);
            Log.i(LOG_TAG,"TestDate: "+nowAsString1);*/
            // This findLastRecord() method finds last record of SQLite database table
            // @param context is passed
            String last_record = findLastRecord(contextCheck);
            String post_data = URLEncoder.encode("last_record","UTF-8")+"="+URLEncoder.encode(last_record,"UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
//            Log.e(LOG_TAG, "Test : Problem retrieving the News JSON results."+ urlConnection.getResponseCode() );
            //Log.i(LOG_TAG, "Test : Problem retrieving the News JSON results."+ urlConnection.getResponseCode() );

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (EOFException e){
            Log.e(LOG_TAG, "EOFException Problem retrieving the News JSON results.", e);
        } catch (IOException e) {
            //Log.e(LOG_TAG, "Problem retrieving the News JSON results."+ urlConnection.getResponseCode() );
            Log.e(LOG_TAG, "Problem retrieving the News JSON results.", e);
            //Log.e(LOG_TAG, "Problem retrieving the News JSON results."+ urlConnection.getResponseCode() );
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

    private static String findLastRecord(Context contextCheck) {
        Log.i(LOG_TAG,"Test : finfLastRecord( ) is called");
        String[] projection = {
                NewsEntry._ID};
        // The query method justifies : "SELECT id FROM news ORDER BY id DESC LIMIT 1" statement
        Cursor lastRecordCursor = contextCheck.getContentResolver().query(NewsEntry.CONTENT_URI,projection,
                null,
                null,
                NewsEntry._ID+ " DESC "+" LIMIT 1");
        if(lastRecordCursor.getCount() > 0){
            // DETERMINE column index
            int index = lastRecordCursor.getColumnIndex(NewsEntry._ID);
            lastRecordCursor.moveToFirst();
            Log.i(LOG_TAG, "Test: "+String.valueOf(lastRecordCursor.getInt(index)));
            String lastRecord = String.valueOf(lastRecordCursor.getInt(index));
            lastRecordCursor.close();
            return lastRecord;


        }else{
            lastRecordCursor.close();
            return String.valueOf(0);
        }



    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();

    }

    private static URL createUrl(String urlNews) {

        URL url2 = null;
        try{
            url2 = new URL(urlNews);
        }catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url2;
    }
}
