package org.sairaa.mantrirajsekher;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sairaa.mantrirajsekher.data.NewsContract;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import static android.R.attr.id;

/**
 * Created by praful on 1/25/2018.
 */
public class UtilCheckStatus {
    public static final String LOG_TAGU = UtilCheckStatus.class.getName();
    static int check_status = 0;
    //public static final String LOG_TAG = UtilCheckStatus.class.getName();
    public static int facthUpdates(String checkUrl, Context bservice) {
        URL url = null;
        //int check_status = 0;
        if(checkUrl != null){
            url = createUrl(checkUrl);
        }

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url,bservice);
            check_status = extractFeatureFromJson(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(check_status == 0){
            // if internet is working but data is not coming than go online
            // and this will be solved on onLoadFinish( ) method
            Log.i(LOG_TAGU,"Test : check findLastRecord: "+findLastRecord(bservice)+" check Status : "+check_status);
            return 1;
        }
        // if internet is working properly and data also coming than
        if(findLastRecord(bservice).equals(String.valueOf(check_status))){
            //satisfy if server database and local SQLite database info are same.
            // returns Zero to work offline therefore in NewsActivity OnloadFinished method
            Log.i(LOG_TAGU,"Test : check findLastRecord: "+findLastRecord(bservice)+" check Status : "+check_status);
            return 0;
        }
        Log.i(LOG_TAGU,"Test : check findLastRecord: "+findLastRecord(bservice)+" check Status : "+check_status);
        return check_status;
    }
    private static String findLastRecord(Context contextCheck) {
        Log.i(LOG_TAGU,"Test : finfLastRecord( ) is called");
        String[] projection = {
                NewsContract.NewsEntry._ID};
        // The query method justifies : "SELECT id FROM news ORDER BY id DESC LIMIT 1" statement
        Cursor lastRecordCursor = contextCheck.getContentResolver().query(NewsContract.NewsEntry.CONTENT_URI,projection,
                null,
                null,
                NewsContract.NewsEntry._ID+ " DESC "+" LIMIT 1");
        if(lastRecordCursor.getCount() > 0){
            // DETERMINE column index
            int index = lastRecordCursor.getColumnIndex(NewsContract.NewsEntry._ID);
            lastRecordCursor.moveToFirst();
            Log.i(LOG_TAGU, "Test: "+String.valueOf(lastRecordCursor.getInt(index)));
            String lastRecord = String.valueOf(lastRecordCursor.getInt(index));
            Log.i(LOG_TAGU,"Test : finfLastRecord( ) is called : "+lastRecord);
            lastRecordCursor.close();
            return lastRecord;


        }else{
            lastRecordCursor.close();
            return String.valueOf(0);
        }



    }

    private static int extractFeatureFromJson(String newsJSON) {
        Log.i(LOG_TAGU, " Test : extractFeatureFromJson() is called");
        int status = 0;
        //Cursor cursor = null;
        //String imageUriAddress = "";
        if (TextUtils.isEmpty(newsJSON)) {
            return 0;
        }
        try {
            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(newsJSON);

            // Extract the JSONArray associated with the key called "top_news",
            // which represents a list of features (or earthquakes).
            JSONArray newsArray = baseJsonResponse.getJSONArray("top_news");
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject currentNews = newsArray.getJSONObject(i);
                status = currentNews.getInt("status");
                int currentId = id;
                //String imageLocation = currentNews.getString("image_loc");
                /*try {
                    //getBitmapFromUrl() retribes bimap from url

                    bitmap = getBitmapFromUrl(imageLocation);
                    imageUriAddress = uploadImageToInternalStorage(bitmap,context,String.valueOf(currentId));
                    Log.i(LOG_TAG," Test NO: "+i+ " image loaded" );
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                /*String heading = currentNews.getString("heading");
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
                insertNews(context, values);*/

                //NewsObject newsObject = new NewsObject(bitmap,heading,shortContent,source,contentAddress,likeCount);
                //topNews.add(newsObject);
                //bitmap = null;
            }
            /*cursor = context.getContentResolver().query(mUri, mProjection, mSelection,
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
            }*/
            //return cursor;
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.i(LOG_TAGU, " Test : Problem parsing the news JSON results");
        }
        Log.i(LOG_TAGU, " Test : Status: "+status);
        return status;
    }

    private static String makeHttpRequest(URL url, Context contextCheck) throws IOException {
        String jsonResponse = "";
        Log.i(LOG_TAGU,"Test : makeHttp( ) is called"+url);
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
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
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
            String last_record = "news";
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
                Log.e(LOG_TAGU, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (EOFException e){
            Log.e(LOG_TAGU, "EOFException Problem retrieving the News JSON results.", e);
        } catch (IOException e) {
            //Log.e(LOG_TAG, "Problem retrieving the News JSON results."+ urlConnection.getResponseCode() );
            Log.e(LOG_TAGU, "Problem retrieving the News JSON results.", e);
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
            Log.e(LOG_TAGU, "Error with creating URL ", e);
        }
        return url2;
    }


}
