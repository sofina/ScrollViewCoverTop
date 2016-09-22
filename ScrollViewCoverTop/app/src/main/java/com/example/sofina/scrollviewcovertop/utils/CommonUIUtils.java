package com.example.sofina.scrollviewcovertop.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

public class CommonUIUtils {

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getScreenWidth(Context c) {
        if (c == null) {
            throw new RuntimeException("context can not be null when getScreenWidth(...)");
        }

        WindowManager wm = (WindowManager) c.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            Point p = new Point();
            display.getSize(p);
            return p.x;
        } else {
            return wm.getDefaultDisplay().getWidth();
        }
    }

    public static int getScreenHeight(Context c) {
        if (c == null) {
            throw new RuntimeException("context can not be null when getScreenHeight(...)");
        }
        WindowManager wm = (WindowManager) c.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            Point p = new Point();
            display.getSize(p);
            return p.y;
        } else {
            return wm.getDefaultDisplay().getHeight();
        }
    }
}
