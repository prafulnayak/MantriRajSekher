package org.sairaa.mantrirajsekher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView profileT = (TextView)findViewById(R.id.x_deatils_profile);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        /*TextView shortN = (TextView)findViewById(R.id.x_details_content);
        headN.setText();*/
        if(isConnected){
            new backgroundTaskForProfile().execute("http://sairaa.org/mantri_rajsekhar/content_details/profile.txt","http://sairaa.org/mantri_rajsekhar/content_details/life.txt","http://sairaa.org/mantri_rajsekhar/content_details/achievement.txt");
            //new NewsDetails.backgroundTaskForText().execute("");
            //headN.setText(head);
        }else{
            //headN.setText("Check Your Internet Connection");
        }
    }

    public class backgroundTaskForProfile extends AsyncTask<String,String,String>{

        TextView profileT = (TextView)findViewById(R.id.x_deatils_profile);
        TextView profileLife = (TextView)findViewById(R.id.x_deatils_life);
        TextView profileAchieve = (TextView)findViewById(R.id.x_deatils_achiev);
        @Override
        protected void onPreExecute() {
            profileT.setText("Loading....");
            profileLife.setText("Loading....");
            profileAchieve.setText("Loading....");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            publishProgress(downloadTextFromUrl(params[0]),downloadTextFromUrl(params[1]),downloadTextFromUrl(params[2]));

            return downloadTextFromUrl(params[0]);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            profileT.setText(values[0]);
            profileLife.setText(values[1]);
            profileAchieve.setText(values[2]);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private String downloadTextFromUrl(String contentC) {
        String contentDetails = "";
        URLConnection url = null;
        if(contentC != null){
            try {
                Log.i("Hello", " : "+contentC);
                url = new URL(contentC).openConnection();

                BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
                String line = "";
                while((line = in.readLine()) != null){
                    contentDetails = contentDetails + line;
                }
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return contentDetails;
        }
        return null;

    }
}
