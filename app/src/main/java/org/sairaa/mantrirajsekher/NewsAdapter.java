package org.sairaa.mantrirajsekher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static org.sairaa.mantrirajsekher.NewsActivity.LOG_TAG;


/**
 * Created by praful on 8/6/2017.
 */

public class NewsAdapter extends ArrayAdapter<NewsObject> {

    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    public static final String LOG_T = NewsAdapter.class.getName();

    public NewsAdapter(Activity context, ArrayList<NewsObject> newsObjectArrayList) {
        super(context, 0, newsObjectArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(LOG_T, "Test getView() called..");
        // Check if the existing view is being reused, otherwise inflate the view

        View listItemView = convertView;

        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_object, parent, false);
        }

        final NewsObject dataToDisplay = getItem(position);

        ImageView imageView = (ImageView)listItemView.findViewById(R.id.x_image);
        imageView.setImageBitmap(dataToDisplay.getImage());

        TextView headingText = (TextView)listItemView.findViewById(R.id.x_heading);
        headingText.setText(dataToDisplay.getHeading());

        TextView shotrContentText = (TextView)listItemView.findViewById(R.id.x_content);
        shotrContentText.setText(dataToDisplay.getShort_heading());

        TextView sourceText = (TextView)listItemView.findViewById(R.id.x_source);
        sourceText.setText(dataToDisplay.getSource());

        TextView dateText = (TextView)listItemView.findViewById(R.id.x_date);
        dateText.setText("08-08-2017");

        final View FINALLISTVIEW = listItemView;
        ImageView shareImage = (ImageView)listItemView.findViewById(R.id.x_share);
        shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheckRead = ContextCompat.checkSelfPermission(FINALLISTVIEW.getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                int permissionCheckWrite = ContextCompat.checkSelfPermission(FINALLISTVIEW.getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                String text = "Look at my awesome picture";
                // Here, thisActivity is the current activity
                if ((ContextCompat.checkSelfPermission(FINALLISTVIEW.getContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(FINALLISTVIEW.getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) FINALLISTVIEW.getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {


                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        Toast.makeText(FINALLISTVIEW.getContext(),"hello",Toast.LENGTH_LONG).show();

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions((Activity) FINALLISTVIEW.getContext(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                        ActivityCompat.requestPermissions((Activity) FINALLISTVIEW.getContext(),
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                        Toast.makeText(FINALLISTVIEW.getContext(),"hello : "+permissionCheckRead+" "+permissionCheckWrite
                                ,Toast.LENGTH_LONG).show();
                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
                if(permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionCheckWrite == PackageManager.PERMISSION_GRANTED) {
                    Uri pictureUri = getImageUri(FINALLISTVIEW.getContext(), dataToDisplay.getImage());
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, text);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                    shareIntent.setType("image/*");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    FINALLISTVIEW.getContext().startActivity(Intent.createChooser(shareIntent, "Share images..."));
                }


               /* Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);


                shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(FINALLISTVIEW.getContext(), dataToDisplay.getImage()));
                shareIntent.setType("image/*");
                //shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mantri");
                //shareIntent.putExtra(Intent.EXTRA_TEXT, "Jai Ho INTUC");
                FINALLISTVIEW.getContext().startActivity(Intent.createChooser(shareIntent, "Share images to.."));*/

                /*Intent intent = new Intent(Intent.ACTION_SEND);
                // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setType("text/plain");
                //intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                //intent.putExtra(Intent.EXTRA_EMAIL,toAddress);
                //intent.putExtra(intent.EXTRA_CC,ccAddress);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Just Java");
                intent.putExtra(Intent.EXTRA_TEXT, "hello");
                FINALLISTVIEW.getContext().startActivity(Intent.createChooser(intent,"Share using"));*/

                //Toast.makeText(FINALLISTVIEW.getContext(),"Hello : "+getImageUri(FINALLISTVIEW.getContext(), dataToDisplay.getImage()),Toast.LENGTH_LONG).show();


            }
        });

        return listItemView;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
