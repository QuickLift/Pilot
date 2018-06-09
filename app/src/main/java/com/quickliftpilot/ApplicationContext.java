package com.quickliftpilot;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

/**
 * Created by pandey on 2/3/18.
 */

public class ApplicationContext extends Activity {
    private static Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mContext = getApplicationContext();
    }

    public Context getContext(){
        return mContext;
    }
}
