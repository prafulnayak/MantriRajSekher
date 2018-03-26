package org.sairaa.mantrirajsekher;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import me.tatarka.support.job.JobInfo;
import me.tatarka.support.job.JobScheduler;



        import android.content.Context;
        import android.content.Intent;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.net.Uri;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Object;

        import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
        import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
        import java.util.List;

import static android.R.attr.bitmap;
import static android.R.attr.data;
import static android.R.attr.id;
import static android.R.id.empty;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static java.util.Collections.addAll;
import static org.sairaa.mantrirajsekher.UtilCheckStatus.check_status;

import android.app.LoaderManager;
        import android.app.LoaderManager.LoaderCallbacks;
        import android.content.Loader;
        import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sairaa.mantrirajsekher.data.NewsContract.NewsEntry;
import org.sairaa.mantrirajsekher.data.NewsDbHelper;
import org.sairaa.mantrirajsekher.services.BackgroundServices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import me.tatarka.support.job.JobScheduler;
import me.tatarka.support.os.PersistableBundle;

public class NewsActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, NavigationView.OnNavigationItemSelectedListener {
    /**
         * Constant value for the news loader ID. We can choose any integer.
         * This really only comes into play if you're using multiple loaders.
    */
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private static final int NEWS_LOADER_ID = 1;
    private static final int NEWS_ACHIEVMENT = 2;
    private static final int NEWS_RAJ_SEKHAR = 3;
    private static final int NEWS_STEEL = 4;
    private static final int NEWS_LOCAL_NEWS = 5;
    private static final int NEWS_WITHOUT_NET_NEWS = 6;
    public static final String LOG_TAG = NewsActivity.class.getName();
    public static final String NEWS_URL = "http://sairaa.org/mantri_rajsekhar/get_json_details.php";
    private static final int JOB_ID = 555;
    // this "countNullPointerException" variable is to help run the program offline, when there is
    // NullPointerException for single time
    private static int countNullPointerException = 0;

    private static final String CHECK_URL = "http://sairaa.org/mantri_rajsekhar/check_updated_table.php";
    //Declaring JobScheduler. It imported from me.tatarka.support.job.JobScheduler;
    //it works on lolipop and pre lolipop
    //check complile dependency
    private JobScheduler mJobScheduler;

    /*
    When we get to the onPostExecute() method, we need to update the ListView.
     The only way to update the contents of the list is to update the data set within the NewsAdapter.
     To access and modify the instance of the NewseAdapter, we need to make it a global variable in the NewsActivity.
     */
    /*
    Adapter for list of news
     */
    private NewsCursorAdapter adapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;
    LoaderManager loaderManager = getLoaderManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

       // StackNavigator stackNavigator = (StackNavigator)findViewById(R.id.navigate_drawer);
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigate_drawer);
        navigationView.setNavigationItemSelectedListener(this);
       /* navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.refresh:
                    Log.i(LOG_TAG,"Test : Hello : "+item.getItemId());
                    break;
                }
                return true;
            }
        });*/


        Log.i(LOG_TAG, "Test onCreate() called..");
        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView)findViewById(R.id.x_list);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);


        adapter = new NewsCursorAdapter(this, null);
        newsListView.setAdapter(adapter);
        // reference to ArrayAdapter
        /*adapter = new NewsAdapter(this, new ArrayList<NewsObject>());
        newsListView.setAdapter(adapter);*/
        URL url;
        int check_status = 0;
        if(isConnected){
            // Get a reference to the LoaderManager, in order to interact with loaders.
            //LoaderManager loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            new backgroundTaskforStatus(this).execute();
            //url = createUrl(CHECK_URL);
            //String jsonResponse = null;
            /*try {
                jsonResponse = makeHttpRequest(url,this);
                check_status = extractFeatureFromJson(jsonResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
           // Log.i(LOG_TAG, "Test : Check Status :"+check_status+" last record : "+findLastRecord(this));
            //if(String.valueOf(check_status) != findLastRecord(this)){
               // Log.i(LOG_TAG, "Test initLoader() called..");
               // loaderManager.initLoader(NEWS_LOADER_ID, null, this);
            //}else
              //  getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, this);

            int noOfRow = displayDatabaseInfo();
        }else{
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            if(findLastRecord(this) != String.valueOf(0)){
                //url = createUrl("http://sairaa.org/mantri_rajsekhar/get_first15_json_details.php");
                //Log.i(LOG_TAG, "TestUrl : 0" );
                Toast.makeText(this,"No internet Connection..\nUnable to get Fresh News",Toast.LENGTH_LONG).show();
                getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, this);
            }else {
                // Set empty state text to display "No earthquakes found."
                mEmptyStateTextView.setText("No Internet connection");
            }


        }
        // create Jobscheduler instance
    }

    @Override
    protected void onStop() {
        //Toast.makeText(this,"onStopJob",Toast.LENGTH_SHORT).show();
        Log.i(LOG_TAG,"TestNews : onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG,"TestNews : onDestroy()");
        super.onDestroy();
    }

    private String findLastRecord(NewsActivity newsActivity) {
        Log.i(LOG_TAG,"Test : finfLastRecord( ) is called");
        String[] projection = {
                NewsEntry._ID};
        // The query method justifies : "SELECT id FROM news ORDER BY id DESC LIMIT 1" statement
        Cursor lastRecordCursor = this.getContentResolver().query(NewsEntry.CONTENT_URI,projection,
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


    private int displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        NewsDbHelper mDbHelper = new NewsDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Perform this raw SQL query "SELECT * FROM pets"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = db.rawQuery("SELECT * FROM " + NewsEntry.TABLE_NAME, null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            //TextView displayView = (TextView) findViewById(R.id.text_view_pet);
            //displayView.setText("Number of rows in pets database table: " + cursor.getCount());
            Log.v(LOG_TAG, "Database no of row : "+cursor.getCount());
            return cursor.getCount();
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

       /* @Override
        public Loader<List<NewsObject>> onCreateLoader(int id, Bundle args) {
            Log.i(LOG_TAG, "Test onCreateLoader() called..");
                return new NewsLoader(this, NEWS_URL);
        }*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "Test onCreateLoader() called..");
        String[] projection = {
                NewsEntry._ID,
                NewsEntry.IMAGE_LOC,
                NewsEntry.HEADING,
                NewsEntry.SHORT_CONTENT,
                NewsEntry.SOURCE,
                NewsEntry.CONTENT_ADDRESS,
                NewsEntry.DATE,
                NewsEntry.LIKE,
                NewsEntry.LOCAL_NEWS,
                NewsEntry.RAJ_NEWS,
                NewsEntry.STEEL_NEWS,
                NewsEntry.NATIONAL_NEWS,
                NewsEntry.INTERNATIONAL_NEWS,
                NewsEntry.OTHERS
        };
        switch (id){
            case NEWS_LOADER_ID:
                return new NewsCursorLoader(this,
                        NewsEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        NewsEntry._ID+" DESC",NEWS_URL);
            case NEWS_ACHIEVMENT:
                Log.i(LOG_TAG, "Test onCreateLoader() called.. "+NEWS_ACHIEVMENT);
                String selectionOther = NewsEntry.OTHERS + "=?"; //Define where part in the query
                String[] selectionArgsOther = {"1"};
                return new CursorLoader(this,
                        NewsEntry.CONTENT_URI,
                        projection,
                        selectionOther,
                        selectionArgsOther,
                        NewsEntry._ID+" DESC");
            case NEWS_RAJ_SEKHAR:
                String selectionRaj = NewsEntry.RAJ_NEWS + "=?"; //Define where part in the query
                String[] selectionArgsRaj = {"1"};
                return new CursorLoader(this,
                        NewsEntry.CONTENT_URI,
                        projection,
                        selectionRaj,
                        selectionArgsRaj,
                        NewsEntry._ID+" DESC");
            case NEWS_STEEL:
                String selectionSteel = NewsEntry.STEEL_NEWS + "=?"; //Define where part in the query
                String[] selectionArgsSteel = {"1"};
                return new CursorLoader(this,
                        NewsEntry.CONTENT_URI,
                        projection,
                        selectionSteel,
                        selectionArgsSteel,
                        NewsEntry._ID+" DESC");
            case NEWS_LOCAL_NEWS:
                String selectionLocal = NewsEntry.LOCAL_NEWS + "=?"; //Define where part in the query
                String[] selectionArgsLocal = {"1"};
                return new CursorLoader(this,
                        NewsEntry.CONTENT_URI,
                        projection,
                        selectionLocal,
                        selectionArgsLocal,
                        NewsEntry._ID+" DESC");
            case NEWS_WITHOUT_NET_NEWS:
                return new CursorLoader(this,
                        NewsEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        NewsEntry._ID+" DESC");
            default:
                return null;

        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "Test onLoadFinished() called..");
        Cursor old_data;
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No Newss found."
        mEmptyStateTextView.setText(" Server Is Busy...\n Try After Some Time...");
        //Test
        // data = null;
        old_data = adapter.getCursor();

            try{
                //adapter.getCursor();
                //adapter.changeCursor(data);
                adapter.swapCursor(data);
                Log.i(LOG_TAG, "Test :OnLoadFinished : Data count: ");
                //if(data.getCount()>5){
                    //job seceduler for background service
                    mJobScheduler = JobScheduler.getInstance(this);
                    constructJob(this);
                    countNullPointerException = 0;
                //}
            }catch (NullPointerException e){
                Toast.makeText(this,"Server Busy\nTry After Some time",Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, "Test: OnLoadFinished :  nullpointer : "+countNullPointerException);
                if (countNullPointerException != 1){
                    getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, this);

                    countNullPointerException = 1;
                }
                //getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, this);
            }
            // the background service will start after 5th record is inserted in SQLite Database




            /*if(old_data != null){
                adapter.changeCursor(old_data);
                Log.i(LOG_TAG, "OnLoadFinished : Data count: new data");
            }else{
                Log.i(LOG_TAG, "OnLoadFinished : Data count: exist");
            }*/


            //Log.i(LOG_TAG, "OnLoadFinished : Data is null");






    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG, "Test onLoaderReset() called..");
        adapter.swapCursor(null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.color_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)){

            return true;
        }
        ListView newsListView = (ListView)findViewById(R.id.x_list);
        //int lineItem = 0;
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            // Respond to a click on the "Insert dummy data" menu option
            case R.id.refresh:
                // When Refresh bitton is clicked

                //it triggers onCreateLoader()
                // check internet connection
                ConnectivityManager cm =
                        (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if(isConnected){
                    new backgroundTaskforStatus(this).execute();
                    //Toast.makeText(this,"Refreshing...\nWait Please...",Toast.LENGTH_LONG).show();
                    //getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
                }else {
                    Toast.makeText(this,"Check Connection to Refresh",Toast.LENGTH_LONG).show();
                    getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, this);
                }

                //filterOnRequest();
                //displayDatabaseInfo();
                getSupportActionBar().setTitle(R.string.app_name);
                newsListView.smoothScrollToPosition(0);
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.go_top:
                //Toast.makeText(this,"achievment",Toast.LENGTH_SHORT).show();
                //getLoaderManager().restartLoader(NEWS_ACHIEVMENT, null, this);
                newsListView.smoothScrollToPosition(0);
                return true;

               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.blue));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff00DDED));
                }*/
                //getLoaderManager().restartLoader(NEWS_LOCAL_NEWS, null, this);


        }
        return super.onOptionsItemSelected(item);
    }

    private void constructJob(NewsActivity newsActivity){
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(this, BackgroundServices.class));
        //PersistableBundle persistableBundle = new PersistableBundle();
        builder.setPeriodic(7200000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true);

        mJobScheduler.schedule(builder.build());
        Log.i(LOG_TAG,"Test : constructJob()");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            // Respond to a click on the "Insert dummy data" menu option
            case R.id.menu_about_us:
                Log.i(LOG_TAG,"Test : Hello : "+item.getItemId());
                // When Refresh bitton is clicked
                //it triggers onCreateLoader()
                //Toast.makeText(this,"refresh",Toast.LENGTH_SHORT).show();
                //getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
                Intent intent = new Intent(this,Profile.class);
                this.startActivity(intent);
                        //intent.
                mDrawerLayout.closeDrawers();

                //filterOnRequest();
                //displayDatabaseInfo();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.menu_news:

                //Toast.makeText(this,"achievment",Toast.LENGTH_SHORT).show();
                getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, this);
                //getLoaderManager().restartLoader(NEWS_ACHIEVMENT, null, this);
                getSupportActionBar().setTitle(R.string.allnews);
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_localnews:
                getLoaderManager().restartLoader(NEWS_LOCAL_NEWS, null, this);
                getSupportActionBar().setTitle(R.string.localnews);
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_steel_news:
                getLoaderManager().restartLoader(NEWS_STEEL, null, this);
                getSupportActionBar().setTitle(R.string.rinlnews);
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_achievement:
                getLoaderManager().restartLoader(NEWS_RAJ_SEKHAR, null, this);
                getSupportActionBar().setTitle(R.string.achievmentnews);
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_manifesto:
                //getLoaderManager().restartLoader(NEWS_LOCAL_NEWS, null, this);
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_galary:
                //Toast.makeText(this,"raj",Toast.LENGTH_SHORT).show();
                //getLoaderManager().restartLoader(NEWS_RAJ_SEKHAR, null, this);
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_speach:
                //getLoaderManager().restartLoader(NEWS_STEEL, null, this);
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_settings:
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_contact_us:
                //getLoaderManager().restartLoader(NEWS_LOCAL_NEWS, null, this);
                mDrawerLayout.closeDrawers();
                return true;
        }
        return true;
    }

    private class backgroundTaskforStatus extends AsyncTask<String,Void,String> {
        NewsActivity bservice;
        public backgroundTaskforStatus(NewsActivity newsActivity) {
            this.bservice = newsActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            int UtilStatus = UtilCheckStatus.facthUpdates(CHECK_URL,bservice);

            return String.valueOf(UtilStatus);
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(LOG_TAG, "Test :OnPostExecute :Test: " + s);
            /*String last_info =
            if (s != findLastRecord(this)) {
                Log.i(LOG_TAG, "Test initLoader() called..");
                loaderManager.initLoader(NEWS_LOADER_ID, null, this);
                //}else
                //  getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, this);
                super.onPostExecute(s);
            }*/
            if(s.equals(String.valueOf(0))){
                Log.i(LOG_TAG, "Test offline() load called..");
                getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, NewsActivity.this);
            }else if(s.equals(String.valueOf(1))){
                Log.i(LOG_TAG, "Test offline() load called.. exception" +s);
                //Toast.makeText(this,"Connection Slow",Toast.LENGTH_SHORT).show();
                getLoaderManager().restartLoader(NEWS_WITHOUT_NET_NEWS, null, NewsActivity.this);
            }else{

                Log.i(LOG_TAG, "Test online() load called..");
                loaderManager.initLoader(NEWS_LOADER_ID, null, NewsActivity.this);
            }

        }
    }

    /*@Override
        public void onLoadFinished(Loader<List<NewsObject>> loader, List<NewsObject> newsList) {
            Log.i(LOG_TAG, "Test onLoadFinished() called..");

            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Set empty state text to display "No Newss found."
            mEmptyStateTextView.setText("No News found");
            // Clear the adapter of previous earthquake data
            adapter.clear();
            // If there is a valid list of News, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if(newsList != null && !newsList.isEmpty()){
                adapter.addAll(newsList);
            }
        }*/

        /*@Override
        public void onLoaderReset(Loader<List<NewsObject>> loader) {
            Log.i(LOG_TAG, "Test onLoaderReset() called..");
            adapter.clear();


        }*/

}
