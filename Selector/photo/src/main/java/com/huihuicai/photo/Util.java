package com.huihuicai.photo;

import android.app.Activity;
import android.os.Build;
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
}
