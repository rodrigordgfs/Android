package com.sendlook.yeslap;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import es.dmoral.toasty.Toasty;

public class Utils {

    public static final String USERS = "users";
    public static final String USER_IMAGES = "user_images";

    public static final String USERNAME = "username";
    public static final String IMAGE = "image";
    public static final String EMAIL = "email";
    public static final String IMAGE_1 = "image1";
    public static final String IMAGE_2 = "image2";
    public static final String IMAGE_3 = "image3";
    public static final String SINCE = "since";

    public static final String TYPE_IMAGE = "image/*";


    public static void toastySuccess(Context context, String msg) {
        Toasty.success(context, msg, Toast.LENGTH_LONG, true).show();
    }

    public static void toastyError(Context context, String msg) {
        Toasty.error(context, msg, Toast.LENGTH_LONG, true).show();
    }

    public static void toastyInfo(Context context, String msg) {
        Toasty.info(context, msg, Toast.LENGTH_LONG, true).show();
    }

    public static void toastyUsual(Context context, String msg, Drawable icon) {
        Toasty.normal(context, msg, Toast.LENGTH_LONG, icon).show();
    }

}