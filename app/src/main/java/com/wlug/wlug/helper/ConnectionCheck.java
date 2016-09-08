package com.wlug.wlug.helper;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Inspiron on 04-09-2016.
 */
public class ConnectionCheck {
private Context context;
    public ConnectionCheck(Context ctx)
    {
        context=ctx;
    }
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
