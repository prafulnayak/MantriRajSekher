package org.sairaa.mantrirajsekher.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by praful on 8/24/2017.
 */

public class NewsContract {
    /**
     +     * The "Content authority" is a name for the entire content provider, similar to the
     +     * relationship between a domain name and its website.  A convenient string to use for the
     +     * content authority is the package name for the app, which is guaranteed to be unique on the
     +     * device.
     +     */

    public static final String CONTENT_AUTHORITY = "org.sairaa.mantrirajsekher";
    /**
     +     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     +     * the content provider.
     +     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     +     * Possible path (appended to base content URI for possible URI's)
     +     * For instance, content://com.example.android.pets/pets/ is a valid path for
     +     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     +     * as the ContentProvider hasn't been given any information on what to do with "staff".
     +     */
    public static final String PATH_NEWS = "news";
    public static final String PATH_IMAGE = "files";

    private NewsContract(){

    }
    public static final class NewsEntry implements BaseColumns{
        /** The content URI to access the news data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NEWS);

        public final static String TABLE_NAME ="news";
        public final static String _ID = BaseColumns._ID;
        public final static String IMAGE_LOC = "image_loc";
        public final static String HEADING = "heading";
        public final static String SHORT_CONTENT = "short_content";
        public final static String SOURCE = "source";
        public final static String CONTENT_ADDRESS = "content_address";
        public final static String DATE = "date";
        public final static String LIKE = "like";
        public final static String LOCAL_NEWS = "local_news";
        public final static String RAJ_NEWS = "raj_news";
        public final static String STEEL_NEWS = "steel_news";
        public final static String NATIONAL_NEWS = "national_news";
        public final static String INTERNATIONAL_NEWS = "international_news";
        public final static String OTHERS = "others";

        /**
         * The MIME type of the  for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        /**
         * The MIME type of the  for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NEWS;

        public static final String CONTENT_IMAGE_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_IMAGE;

    }


}
