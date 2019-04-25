package org.octabyte.zeem.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.octabyte.zeem.Home.HomeActivity.APP_USER_ID;

/**
 * Created by Azeem on 8/1/2017.
 */

public class Utils {

    // API Key
    public static final String ApiKey = "AIzaSyAFmmml05GeZH-vyk0QbA-BtjUQFm-ZNi8";

    // Bucket url
    public static final String bucketURL = "https://storage.googleapis.com/octabyte-zeem/";

    // Package name
    //public static final String appPackageName = "org.octabyte.zeem";

    // Signature
    //public static final String appSignature = "b0f7d7fb73eb9a152289b30004ec3aa76ff91356";


    private static int screenWidth = 0;
    private static int screenHeight = 0;

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static float dpToPx(float dp) {
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static boolean isAndroid5() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static void blinkAnimation(ImageView view){
        Log.i("com.azeem.Utils", "blinkAnimation");
        final Animation animation = new AlphaAnimation(1,0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        view.setAnimation(animation);
    }

    public static void audioWaveAnimation(ImageView view){
        Animation animation = new ScaleAnimation(
                1f, 1f,0.5f,1f,
                Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);
        view.setAnimation(animation);
    }

    public static void scaleView(View v, float xScale, float yScale) {
        Animation anim = new ScaleAnimation(
                Animation.RELATIVE_TO_SELF, xScale, // Start and end values for the X axis scaling
                Animation.RELATIVE_TO_SELF, yScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 1f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(300);
        v.startAnimation(anim);
    }

    /**
     * Function to convert milliseconds time to
     * @param milliseconds  MilliSeconds that you want to convert
     * @return              Timer Format Hours:Minutes:Seconds
     */
    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /** Create a File for saving an json */
    public static File getOutputJsonFile(Context context, String fileName){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File jsonStorageDir = new File(context.getExternalFilesDir(null), "offline");

        // Create the storage directory if it does not exist
        if (! jsonStorageDir.exists()){
            if (! jsonStorageDir.mkdirs()){
                Log.d("DJSON", "failed to create directory");
                return null;
            }
        }

        // Create a json file
        return new File(jsonStorageDir.getPath() + File.separator + fileName);
    }

    /**
     * Replace String in last occurrence from string
     * @param str           String that need to modify
     * @param toReplace     Which you want to replace
     * @param replacement   Replace with this string
     * @return              Modified String after replacement
     */
    public static String replaceLast(String str, String toReplace, String replacement){
        int start = str.lastIndexOf(toReplace);
        return str.substring(0, start) +
                replacement +
                str.substring(start + toReplace.length());
    }

    // Over loading function getBadgeText
    public static String getBadgeText(int badge){
        return getBadgeText(badge, "N");
    }
    /**
     * Function convert int badge number into string
     * @param badge     Badge Number
     * @param gender    Gender of user
     * @return          String name of this badge
     */
    private static String getBadgeText(int badge, String gender){
        switch (badge){
            case 0:
                return "BASIC";
            case 1:
                return "BRONZE";
            case 2:
                return "SILVER";
            case 3:
                return "GOLD";
            case 4:
                return "DIAMOND";
            case 5:
                return "CELEBRITY";
            case 6:
                return "STAR";
            case 7:
                if (gender.equals("M"))
                    return "KING";
                else if (gender.equals("F"))
                    return "QUEEN";
                else
                    return "KING";
             default:
                 return "BASIC";
        }
    }

    public static int getBadgeStar(int badge){
        switch (badge){
            case 0:
                return 100;
            case 1:
                return 500;
            case 2:
                return 1000;
            case 3:
                return 5000;
            case 4:
                return 10000;
            case 5:
                return 25000;
            case 6:
                return 50000;
            case 7:
                return 100000;
            default:
                return 100;
        }
    }

    // Over loading function getBadgePic
    public static int getBadgePic(int badge){
        return getBadgePic(badge, "N");
    }

    /**
     * Convert badge number into badge picture
     * @param badge     Badge Number
     * @param gender    Gender of user
     * @return          Res int id if the badge
     */
    private static int getBadgePic(int badge, String gender){
        switch (badge){
            case 0:
                return R.drawable.shape_badge_dark;
            case 1:
                return R.drawable.shape_badge_brown;
            case 2:
                return R.drawable.shape_badge_silver;
            case 3:
                return R.drawable.shape_badge_gold;
            case 4:
                return R.drawable.badge_diamond;
            case 5: // celebrity
                return R.drawable.ic_fill_star;
            case 6:
                return R.drawable.ic_fill_star;
            case 7:
                if (gender.equals("M"))
                    return R.drawable.badge_king;
                else if (gender.equals("F"))
                    return R.drawable.badge_queen;
                else
                    return R.drawable.badge_king;
            default:
                return R.drawable.shape_badge_dark;
        }
    }

    /**
     * Create new Audio file with extension 3gp in ZeemAudio folder
     * @param context Context of the class
     * @return  Audio file path
     */
    public static String getOutputAudioMedia(Context context){

        File mediaStorageDir = new File(context.getExternalFilesDir(null), "temp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("UtilsLog", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        Long timeStamp = System.currentTimeMillis();

        return mediaStorageDir.getPath() + File.separator +
                "AUD_"+ timeStamp + ".3gp";
    }

    /**
     * Function to get Media File duration
     * @param filePath      Absolute path if the media file
     * @return              Long - duration
     */
    public static Long getMediaDuration(String filePath){

        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(filePath, new HashMap<String, String>());
            String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(durationStr);
        }catch (RuntimeException e){
            e.printStackTrace();

            try {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(filePath);
                String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                return Long.parseLong(durationStr);
            } catch (IllegalArgumentException e1) {
                e1.printStackTrace();

                return 0L;
            }
        }

    }

    /**
     * Check media get enough recording or not
     * @param filePath  File to check contains enough recording
     * @return          Boolean TRUE | FALSE
     */
    public static boolean isMediaRecorded(String filePath){

        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(filePath, new HashMap<String, String>());
            Long durationStr = Long.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            return durationStr > 500;
        } catch (Exception e) {
            e.printStackTrace();

            try {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(filePath);
                Long durationStr = Long.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                return durationStr > 500;
            } catch (Exception ex) {
                ex.printStackTrace();

                return false;
            }
        }

    }

    public enum GC_FOLDER {
        AUDIO, VIDEO, IMAGE, GIF, COVER, PIC, STORY_IMG, STORY_VID, STORY_GIF, COMMENT_IMG
    }

    /**
     * Create file name using user id and System current Milli sec
     * @param folder    Folder Type etc AUDIO, VIDEO
     * @return          fileName used to store file on cloud storage
     */
    public static String cloudStorageFileName(GC_FOLDER folder){
        String fileName = APP_USER_ID + "_" + System.currentTimeMillis();

        switch (folder){
            case AUDIO:
                fileName = "AUDIO/" + fileName + ".3gp";
                break;
            case VIDEO:
                fileName = "VIDEO/" + fileName + ".mp4";
                break;
            case IMAGE:
                fileName = "IMAGE/" + fileName + ".jpg";
                break;
            case COVER:
                fileName = "COVER/" + fileName + ".jpg";
                break;
            case GIF:
                fileName = "GIF/" + fileName + ".mp4";
                break;
            case PIC:
                fileName = "PIC/" + fileName + ".jpg";
                break;
            case STORY_IMG:
                fileName = "STORY/IMAGE/" + fileName + ".jpg";
                break;
            case STORY_VID:
                fileName = "STORY/VIDEO/" + fileName + ".mp4";
                break;
            case STORY_GIF:
                fileName = "STORY/GIF/" + fileName + ".mp4";
                break;
            case COMMENT_IMG:
                fileName = "COMMENT_IMG/" + fileName + ".jpg";
                break;
        }

        return fileName;
    }

    /**
     * Used to convert text view text into click able links For example mention user and hash tag
     * @param textView      TextView that you want to convert into click able
     * @param str           What string need to be convert and set this string into TextView
     */
    public static void applyClickableSpan(final Activity mActivity, TextView textView, final String str){
        SpannableString ss = new SpannableString(str);

        Pattern mentionPattern = Pattern.compile("(@[A-Za-z0-9_-]+)");
        Pattern hashTagPattern = Pattern.compile("(#[A-Za-z0-9_-]+)");
        Pattern urlPattern = Patterns.WEB_URL;

        Matcher mentionMatcher = mentionPattern.matcher(ss);
        while (mentionMatcher.find()) {
            final int start = mentionMatcher.start();
            final int end = mentionMatcher.end();
            ss.setSpan(new ClickableSpan() {
                final String clickString = str.substring(start,end);
                @Override
                public void onClick(View widget) {
                    //Toast.makeText(mContext, "Mention" + clickString, Toast.LENGTH_SHORT).show();
                    int[] startingLocation = new int[2];
                    widget.getLocationOnScreen(startingLocation);
                    startingLocation[0] += widget.getWidth() / 2; //2
                    ProfileActivity.startUserProfileFromLocation(startingLocation, mActivity, clickString);
                    mActivity.overridePendingTransition(0, 0);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#00bfff"));
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        Matcher hashTagMatcher = hashTagPattern.matcher(ss);
        while (hashTagMatcher.find()) {
            final int start = hashTagMatcher.start();
            final int end = hashTagMatcher.end();
            ss.setSpan(new ClickableSpan() {
                final String clickString = str.substring(start,end);
                @Override
                public void onClick(View widget) {
                    //Toast.makeText(mContext, "HashTag" + clickString, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#00bfff"));
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        Matcher urlMatcher = urlPattern.matcher(ss);
        while (urlMatcher.find()) {
            final int start = urlMatcher.start();
            final int end = urlMatcher.end();
            ss.setSpan(new ClickableSpan() {
                final String clickString = str.substring(start,end);
                @Override
                public void onClick(View widget) {
                    //Toast.makeText(mContext, "URL" + clickString, Toast.LENGTH_SHORT).show();
                }
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(Color.parseColor("#00bfff"));
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Capitalize each first char of every word
     * @param input     String input that you want to convert into capitalize form
     * @return          Capitalize form of String
     */
    public static String capitalize(@NonNull String input) {

        String[] words = input.toLowerCase().split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            if (i > 0 && word.length() > 0) {
                builder.append(" ");
            }

            String cap = word.substring(0, 1).toUpperCase() + word.substring(1);
            builder.append(cap);
        }
        return builder.toString();
    }

}
