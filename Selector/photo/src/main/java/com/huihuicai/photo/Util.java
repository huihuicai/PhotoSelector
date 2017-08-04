package com.huihuicai.photo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.ActivityInfoCompat;
import android.view.WindowManager;

/**
 * Created by ybm on 2017/8/3.
 */

public class Util {

    public static void settingStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = activity.getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
    }

    /**
     * 检查权限是否申请
     */
    public static boolean checkPermission(Context context, String[] permission) {
        if (permission == null || permission.length == 0) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String p : permission) {
                if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }

    /**
     * 申请权限
     */
    public static boolean requestPermission(Activity context, String[] permission, int requestCode) {
        if (permission == null) {
            return true;
        }
        if (!checkPermission(context, permission)) {
            ActivityCompat.requestPermissions(context, permission, requestCode);
            return false;
        }else{
            return true;
        }
    }
}
