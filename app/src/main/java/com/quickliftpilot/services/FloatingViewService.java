package com.quickliftpilot.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.quickliftpilot.R;
import com.quickliftpilot.activities.MapActivity;

import java.util.List;

public class FloatingViewService extends Service {
    private WindowManager windowManager;
    private View mFloatView;
    private ImageView img;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFloatView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget,null);

        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
////                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
////                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
////                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
////                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
////                LAYOUT_FLAG,
//                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT

                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        windowManager.addView(mFloatView,params);

        img = (ImageView)mFloatView.findViewById(R.id.float_btn);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                killAppBypackage("com.google.android.apps.maps");
                Intent intent = new Intent(FloatingViewService.this, MapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                stopSelf();
            }
        });
    }

    private void killAppBypackage(String packageTokill){

        List<ApplicationInfo> packages;
        PackageManager pm;
        pm = getPackageManager();
        //get a list of installed apps.
        packages = pm.getInstalledApplications(0);


        ActivityManager mActivityManager = (ActivityManager) FloatingViewService.this.getSystemService(Context.ACTIVITY_SERVICE);
        String myPackage = getApplicationContext().getPackageName();

        for (ApplicationInfo packageInfo : packages) {
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1) {
                continue;
            }
            if(packageInfo.packageName.equals(myPackage)) {
                continue;
            }
            if(packageInfo.packageName.equals(packageTokill)) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatView != null)
            windowManager.removeView(mFloatView);
    }
}
