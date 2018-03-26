package org.sairaa.mantrirajsekher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static org.sairaa.mantrirajsekher.NewsCursorAdapter.LOG_TAGADAPTER;

public class NewsDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_details);

        String head = getIntent().getStringExtra("heading");
        String shortC = getIntent().getStringExtra("content");
        String sourceC = getIntent().getStringExtra("source");
        String imageC = getIntent().getStringExtra("image");
        String contentC = getIntent().getStringExtra("contentAddress");

        TextView sourceN = (TextView)findViewById(R.id.x_details_source);
        sourceN.setText(sourceC);

        ImageView imageN = (ImageView)findViewById(R.id.x_details_image);
        imageN.setImageBitmap(downloadImageFromInternal(imageC));

        TextView headN = (TextView)findViewById(R.id.x_deatils_heading);

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        /*TextView shortN = (TextView)findViewById(R.id.x_details_content);
        headN.setText();*/
        if(isConnected){
            new backgroundTaskForText().execute(contentC);
            headN.setText(head);
        }else{
            headN.setText("Check Your Internet Connection");
        }


    }

    public class backgroundTaskForText extends AsyncTask<String,Void,String>{
        TextView headN = (TextView)findViewById(R.id.x_details_content);
        @Override
        protected void onPreExecute() {
            headN.setText("      Loading....\n" +
                    "        Please Wait");
        }

        @Override
        protected String doInBackground(String... params) {

            return downloadTextFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {

            Log.i("Hello", " : "+s);
            if(s != null){
                headN.setText(s);
            }else {
                headN.setText("Content will be updated soon....\n" +
                        "Be in Touch.");
            }

            //super.onPostExecute(s);
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

    private Bitmap downloadImageFromInternal(String imageC) {
        Bitmap b = null;
        try {
            File f=new File(imageC);
            Log.i(LOG_TAGADAPTER,"Test "+String.valueOf(f));
            b = BitmapFactory.decodeStream(new FileInputStream(f));
            //ImageView img=(ImageView)findViewById(R.id.imgPicker);
            //iImageView.setImageBitmap(b);

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return b;
    }
}
