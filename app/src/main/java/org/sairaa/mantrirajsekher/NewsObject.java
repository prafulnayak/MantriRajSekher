package org.sairaa.mantrirajsekher;

import android.graphics.Bitmap;

/**
 * Created by praful on 8/2/2017.
 */

public class NewsObject {
    private Bitmap image;
    private String heading, short_heading, source, url;
    private int like;

    //constructor
    public NewsObject(Bitmap imageC, String headingC, String short_headingC, String sourceC, String urlC, int likeC){
        image = imageC;
        heading = headingC;
        short_heading = short_headingC;
        source = sourceC;
        url = urlC;
        like = likeC;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getHeading() {
        return heading;
    }

    public String getShort_heading() {
        return short_heading;
    }
    public String getSource(){
        return source;
    }

    public String getUrl() {
        return url;
    }

    public int getLike() {
        return like;
    }
}
