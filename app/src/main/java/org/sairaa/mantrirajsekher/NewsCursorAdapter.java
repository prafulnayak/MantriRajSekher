package org.sairaa.mantrirajsekher;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.sairaa.mantrirajsekher.data.NewsContract;
import org.sairaa.mantrirajsekher.data.NewsContract.NewsEntry;
import org.sairaa.mantrirajsekher.data.NewsProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static android.R.attr.content;
import static android.R.attr.data;
import static android.R.attr.id;
import static android.support.v4.content.FileProvider.getUriForFile;
import static java.security.AccessController.getContext;

/**
 * Created by praful on 8/24/2017.
 */

public class NewsCursorAdapter extends CursorAdapter {

    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    public static final String LOG_TAGADAPTER = NewsCursorAdapter.class.getName();

    //@param context The context
    // @param c       The cursor from which to get the data.
    public NewsCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }
    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i(LOG_TAGADAPTER, "Test newView() called..");
        return LayoutInflater.from(context).inflate(R.layout.news_list_object, parent, false);

    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Log.i(LOG_TAGADAPTER, "Test bindView() called..");
        ImageView mainImageView = (ImageView)view.findViewById(R.id.x_image);
        TextView headingT = (TextView)view.findViewById(R.id.x_heading);
        TextView shortContentT = (TextView)view.findViewById(R.id.x_content);
        TextView sourceT = (TextView)view.findViewById(R.id.x_source);
        TextView dateT = (TextView)view.findViewById(R.id.x_date);

        ImageView shareIcon = (ImageView)view.findViewById(R.id.x_share);
        ImageView navigateIcon = (ImageView)view.findViewById(R.id.x_navigate);


        int idIndex = cursor.getColumnIndex(NewsEntry._ID);
        int imageUriIndex = cursor.getColumnIndex(NewsEntry.IMAGE_LOC);
        int headingIndex = cursor.getColumnIndex(NewsEntry.HEADING);
        int shortContentIndex = cursor.getColumnIndex(NewsEntry.SHORT_CONTENT);
        int sourceIndex = cursor.getColumnIndex(NewsEntry.SOURCE);
        int dateIndex = cursor.getColumnIndex(NewsEntry.DATE);
        int contentDetailsIndex = cursor.getColumnIndex(NewsEntry.CONTENT_ADDRESS);

        int currentId = cursor.getInt(idIndex);
        final String imageUriPath = cursor.getString(imageUriIndex);
        mainImageView.setImageBitmap(downloadImageFromInternal(imageUriPath, String.valueOf(currentId)));

        final String heading = cursor.getString(headingIndex);
        headingT.setText(heading);

        final String shortContent = cursor.getString(shortContentIndex);
        shortContentT.setText(shortContent);

        final String source = cursor.getString(sourceIndex);
        sourceT.setText(source);

        final String contentAddress = cursor.getString(contentDetailsIndex);

        String date = cursor.getString(dateIndex);
        dateT.setText(String.valueOf(date));
        final int idForShare = currentId;
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = heading +"\n\nDownload The App \n" + " https://play.google.com/store/apps/details?id=org.sairaa.mantrirajsekher&hl=en ";
                // create new Intent
                Intent intent = new Intent(Intent.ACTION_SEND);
                // set flag to give temporary permission to external app to use your FileProvider
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                File file =new File(imageUriPath);
                // generate URI, I defined authority as the application ID in the Manifest, the last param is file I want to open
                Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
                context.grantUriPermission("org.sairaa.mantrirajsekher",contentUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                //intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=org.sairaa.mantrirajsekher&hl=en ");

                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                Log.i(LOG_TAGADAPTER,"Test "+String.valueOf(file));
                Log.i(LOG_TAGADAPTER,"Test "+String.valueOf(contentUri));
                //This clipData works for android version lower to APK23
                //ClipData capture the RawUri to send data
                intent.setClipData(ClipData.newRawUri("",contentUri));
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // I am opening a PDF file so I give it a valid MIME type
                intent.setDataAndType(contentUri, "image/jpg");
                // validate that the device can open your File!
                PackageManager pm = context.getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    //startActivity(intent);
                    context.startActivity(Intent.createChooser(intent, "Share images..."));
                }

            }
        });
        navigateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getApplicationContext(), NewsDetails.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("id",String.valueOf(idForShare));
                intent.putExtra("heading",heading);
                intent.putExtra("image",imageUriPath);
                intent.putExtra("content",shortContent);
                intent.putExtra("source",source);
                intent.putExtra("contentAddress",contentAddress);
                context.startActivity(intent);
            }
        });
    }

    private Bitmap downloadImageFromInternal(String imageUriPath, String s) {
        Bitmap b = null;
        try {
            File f=new File(imageUriPath);
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
