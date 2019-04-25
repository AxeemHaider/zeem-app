package org.octabyte.zeem.Firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.InstantChat.InstantChatActivity;
import org.octabyte.zeem.Notification.NotificationActivity;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.SinglePost.SinglePostActivity;
import org.octabyte.zeem.Utils.Utils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import static org.octabyte.zeem.InstantChat.InstantChatActivity.instant_chat_active;
import static org.octabyte.zeem.InstantChat.InstantChatActivity.senderToken;
import static org.octabyte.zeem.Profile.ProfileActivity.ARG_REVEAL_START_LOCATION;
import static org.octabyte.zeem.Profile.ProfileActivity.USER_PROFILE_ID;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {

        boolean instantChat = false;
        boolean instantChatMessageInfo = false;
        String newSenderToken = "senderToken";


        try {
            Map<String, String> data = message.getData();
            instantChat =  data.get("instantChat") != null && Boolean.parseBoolean(data.get("instantChat"));
            newSenderToken =  data.get("senderToken") != null ? data.get("senderToken") : "senderToken";
            instantChatMessageInfo =  data.get("instantChatMessageInfo") != null && Boolean.parseBoolean(data.get("instantChatMessageInfo"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!instantChat){
            showNotification(message);
        }else if (!instant_chat_active) {

            if(!instantChatMessageInfo){
                showNotification(message);
            }

        }else{

            if(senderToken == null){
                return;
            }

            if (!senderToken.equals(newSenderToken)) {

                if(!instantChatMessageInfo) {
                    showNotification(message);
                }


            }else{
                Long messageId = null;
                String messageText = null, senderToken = null, chatAnonymous = null;

                try {
                    Map<String, String> data = message.getData();
                    senderToken =  data.get("senderToken") != null ? data.get("senderToken") : "senderToken";
                    messageText =  data.get("messageText") != null ? data.get("messageText") : "messageText";
                    chatAnonymous =  data.get("chatAnonymous") != null ? data.get("chatAnonymous") : "chat_anonymous";
                    instantChatMessageInfo =  data.get("instantChatMessageInfo") != null && Boolean.parseBoolean(data.get("instantChatMessageInfo"));
                    messageId = data.get("messageId") != null ? Long.parseLong(data.get("messageId")) : 0L;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Send local broadcast
                Intent intent = new Intent("new-instant-chat-local-receiver");
                intent.putExtra("senderToken", senderToken);
                intent.putExtra("messageId", messageId);
                intent.putExtra("messageText", messageText);
                intent.putExtra("received", instantChatMessageInfo);
                intent.putExtra("chatAnonymous", chatAnonymous);

                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }

    }

    private void showNotification(RemoteMessage message){

        int notificationIcon = R.drawable.notification_icon;

        String notificationType = "UPDATE_APP";
        String profilePic = "no picture";
        String userId = "123";
        String mode = "PRIVATE";
        String postSafeKey = "not available";
        Boolean isAnonymous = false;

        String senderToken = "Not Found";
        String userFullName = "Not Found";
        String chatAnonymous = "chat_anonymous";
        Long messageId = null;

        String notificationBody = null;
        try {
            notificationBody = message.getNotification().getBody();
            notificationBody = notificationBody.replace("[", "");
            notificationBody = notificationBody.replace("]", "");
        } catch (NullPointerException e) {
            e.printStackTrace();

            return;
        }

        try {
            Map<String, String> data = message.getData();

            notificationType = data.get("notificationType") != null ? data.get("notificationType") : "UPDATE_APP";
            profilePic = data.get("profilePic") != null ? data.get("profilePic") : "no picture";
            userId = data.get("userId") != null ? data.get("userId") : "123";
            mode = data.get("mode") != null ? data.get("mode") : "PRIVATE";
            postSafeKey = data.get("postSafeKey") != null ? data.get("postSafeKey") : "not available";
            isAnonymous = data.get("anonymous") != null && Boolean.parseBoolean(data.get("anonymous"));

            senderToken = data.get("senderToken") != null ? data.get("senderToken") : "Not Found";
            userFullName = data.get("userFullName") != null ? data.get("userFullName") : "Not Found";
            chatAnonymous = data.get("chatAnonymous") != null ? data.get("chatAnonymous") : "chat_anonymous";

            messageId = data.get("messageId") != null ? Long.parseLong(data.get("messageId")) : 0L;

        } catch (Exception e) {
            e.printStackTrace();
        }

        Random random = new Random();
        final int notificationId = random.nextInt();

        // Activity intent
        Intent intent = null;

        switch (notificationType){

            case "INSTANT_CHAT":

                notificationIcon = R.drawable.notification_chat;

                if (isAnonymous){
                    profilePic = "anonymous";
                    userFullName = "Anonymous Person";
                }

                intent = new Intent(this, InstantChatActivity.class);
                intent.putExtra("ARG_INSTANT_CHAT_SENDER_TOKEN", senderToken);
                intent.putExtra("ARG_INSTANT_CHAT_MESSAGE_ID", messageId);
                intent.putExtra("ARG_INSTANT_CHAT_MESSAGE_TEXT", notificationBody);
                intent.putExtra("ARG_INSTANT_CHAT_USER_PIC", profilePic);
                intent.putExtra("ARG_INSTANT_CHAT_USER_NAME", userFullName);
                intent.putExtra("ARG_INSTANT_CHAT_ANONYMOUS", chatAnonymous);
                break;

            case "POST_LIKE":
            case "POST_COMMENT":
            case "TAG_POST":
            case "LIST_POST":
            case "POST_MENTION":
            case "COMMENT_MENTION":
            case "REPORT_SPAM":
            case "REPORT_ANTI_RELIGION":
            case "REPORT_SEXUAL_CONTENT":
            case "REPORT_OTHER":

                intent = new Intent(this, SinglePostActivity.class);
                intent.putExtra("ARG_SINGLE_POST_SAFE_KEY", postSafeKey);
                intent.putExtra("ARG_SINGLE_POST_POST_MODE", mode);
                break;

            case "FOLLOWER":
            case "NEW_FRIEND":
            case "COMMENT_LIKE":

                intent = new Intent(this, ProfileActivity.class);
                int[] location = new int[2];
                location[0] = 0;
                location[1] = 0;
                intent.putExtra(ARG_REVEAL_START_LOCATION, location);
                intent.putExtra(USER_PROFILE_ID, userId);
                break;

            case "FRIEND_REQUEST":

                intent = new Intent(this, NotificationActivity.class);
                intent.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
                intent.putExtra("ARG_NOTIFICATION_FOR_REQUEST", true);
                break;

            case "UPDATE_APP":
                final String appPackageName = getPackageName();
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
        }

        // opening activity
        if (intent == null)
            intent = new Intent(this, HomeActivity.class);
        else
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_ONE_SHOT);

        // Sound
        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Setup notification // R.mipmap.ic_launcher
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(notificationIcon)
                .setContentTitle(message.getNotification().getTitle())
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (isAnonymous) {
            // Hide profile pic when person is anonymous
            Glide.with(this).asBitmap().load(R.drawable.ic_anonymous)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            notificationManager.notify(notificationId, notificationBuilder.build());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            notificationBuilder.setLargeIcon(resource);
                            notificationManager.notify(notificationId, notificationBuilder.build());
                            return false;
                        }
                    }).submit();

        } else { // show profile pic when person is not anonymous
            Glide.with(this).asBitmap().load(Utils.bucketURL + profilePic)
                    .apply(RequestOptions.circleCropTransform())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            notificationManager.notify(notificationId, notificationBuilder.build());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            notificationBuilder.setLargeIcon(resource);
                            notificationManager.notify(notificationId, notificationBuilder.build());
                            return false;
                        }
                    }).submit();
        }
    }


}