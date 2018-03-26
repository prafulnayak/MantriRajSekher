package org.sairaa.mantrirajsekher;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.util.Log;

import java.io.IOException;

/**
 * Created by praful on 8/24/2017.
 */

public class NewsCursorLoader extends CursorLoader {
    public static final String LOG_TAGLOADER = NewsActivity.class.getName();
    Uri mUri;
    String[] mProjection;
    String mSelection;
    String[] mSelectionArgs;
    String mSortOrder;

    Cursor mCursor;
    CancellationSignal mCancellationSignal;

    private  String mUrl;
    public Context context1;
    public NewsCursorLoader(Context context) {
        super(context);
    }

    public NewsCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, String newsUrl) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        Log.i(LOG_TAGLOADER, "Test NewCursorLoader() is called..");
        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder =sortOrder;
        mUrl = newsUrl;
        context1 = context;
    }

    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAGLOADER, "Test onStartLoading() is called..");
        super.onStartLoading();
    }

    @Override
    public Cursor loadInBackground() {
        Log.i(LOG_TAGLOADER, "Test loadInBackgroung() is called..");
        Cursor c = null;
        if(mUrl == null){
            return  null;
        }
        try {
            c = QueryUtil.fatchNewsInfo(mUrl, context1,mUri,mProjection,mSelection,mSelectionArgs,mSortOrder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return c;
    }
}
