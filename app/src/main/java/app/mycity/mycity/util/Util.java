package app.mycity.mycity.util;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import app.mycity.mycity.R;

public class Util {

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String createLink(String type, String ownerId, String id){

        return Constants.URL_BASE_SITE + type + ownerId + "_" + id;
    }

    public static String createAtt(String type, String ownerId, String id){

        return type + ownerId + "_" + id;
    }

    public static void copyTextToClipboard(Context context, String text){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(null, text);
        clipboard.setPrimaryClip(clip);
    }

    public static DisplayMetrics getMetrics(Activity activity){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public static void setNawBarClickListener(View v){
        ImageView imageView = v.findViewById(R.id.makeCheckinPhoto);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventBusMessages.MakeCheckin());
            }
        });
        ConstraintLayout layout;
        for (int i = 0; i < 4; i++) {
            layout = v.findViewById(Constants.newNavButtons[i]);
            final int finalI = i;
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (finalI){
                        case 0:
                            EventBus.getDefault().post(new EventBusMessages.OpenMenu());
                            break;
                        case 1:
                            EventBus.getDefault().post(new EventBusMessages.OpenUser(SharedManager.getProperty(Constants.KEY_MY_ID)));
                            break;
                        case 2:
                            EventBus.getDefault().post(new EventBusMessages.OpenDialogs());
                            break;
                        case 3:
                            EventBus.getDefault().post(new EventBusMessages.OpenNotifications());
                            break;
                    }
                }
            });
        }
    }

    public static String getPath(Activity activity, Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);

        if (cursor == null) return null;
        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        return s;
    }

    public static void setNawBarIconColor(Context context, View v, int pos){
        ImageView imageView;
        for (int i = 0; i < 4; i++) {
            imageView = v.findViewById(Constants.newNavButtonsIcons[i]);
            if(i==pos){
                imageView.setColorFilter(context.getResources().getColor(R.color.colorAccent));
            } else {
                imageView.setColorFilter(context.getResources().getColor(R.color.black_67percent));
            }
        }
    }

  public static void setUnreadCount(View view){
      View indicatorView = view.findViewById(R.id.unreadMessageIndicator);
      if(SharedManager.getBooleanProperty("unreadMessages")){
          indicatorView.setVisibility(View.VISIBLE);
      } else {
          indicatorView.setVisibility(View.GONE);
      }
  }


  public static View.OnTouchListener getTouchTextListener(final TextView textView){

        final ColorStateList color = textView.getTextColors();
      View.OnTouchListener onTouchListener = new View.OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
              if(event.getAction()== MotionEvent.ACTION_DOWN){
                  textView.setTextColor(Color.parseColor("#000000"));
              }
              if(event.getAction()== MotionEvent.ACTION_UP){
                  textView.setTextColor(Color.parseColor("#999999"));
              }
              return false;
          }
      };
      return onTouchListener;
  }

    public static String getExternalFileName(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.US);
        Date now = new Date();

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "MyCity");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        String fileName;
        if(success){
            fileName = Environment.getExternalStorageDirectory() + "/MyCity/checkin_" + formatter.format(now) + ".jpg";
        } else {
            fileName = Environment.getExternalStorageDirectory() + "/checkin_" + formatter.format(now) + ".jpg";
        }
        return fileName;
    }

    public static String getExternalVideoFileName(){
        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss", Locale.US);
        Date now = new Date();

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "MyCity");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }

        String fileName;
        if(success){
            fileName = Environment.getExternalStorageDirectory() + "/MyCity/checkin_" + formatter.format(now) + ".mp4";
        } else {
            fileName = Environment.getExternalStorageDirectory() + "/checkin_" + formatter.format(now) + "mp4";
        }
        return fileName;
    }



    public static String getDate_ddMMyyyy(long time) {
        Date date = new Date(time*1000L);
       SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(date);
    }

    public static String getTime(long time) {
        Date date = new Date(time*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        return sdf.format(date);
    }

    public static String getDatePretty(long time) {
        Date date = new Date(time*1000L);
        String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(date.getTime(), Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
        return niceDateStr;
    }

    public static String getDatePrettyOld(long time) {
        Date date = new Date(time*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm");
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm");
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(time*1000L);
        Date curDate = new Date(cdate.getTimeInMillis());
        if(isToday(time*1000L)){
            return formatTime.format(date);
        }
        if(isYesterday(time*1000L)){
            return "Yesterday";
        }
        return sdf.format(date);
    }

    public static boolean isYesterday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        now.add(Calendar.DATE,-1);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

    public static boolean isToday(long date) {
        Calendar now = Calendar.getInstance();
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        return now.get(Calendar.YEAR) == cdate.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == cdate.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == cdate.get(Calendar.DATE);
    }

}
