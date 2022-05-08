package com.sohrab.obd.reader.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DialogUtils {

    public static void toast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    public static void infoDialog(Context context, String title) {
        infoDialog(context, title, "");
    }

    public static void infoDialog(Context context, String title, String message) {
        showDialog(context, title, message, android.R.drawable.ic_dialog_info);
    }

    public static void alertDialog(Context context, String title) {
        alertDialog(context, title, "");
    }

    public static void alertDialog(Context context, String title, String message) {
        showDialog(context, title, message, android.R.drawable.ic_dialog_alert);
    }

    private static void showDialog(Context context, String title, String message, @DrawableRes int iconId) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                //.setPositiveButton(android.R.string.ok, null)
                .setIcon(iconId)
                .show();
    }
}