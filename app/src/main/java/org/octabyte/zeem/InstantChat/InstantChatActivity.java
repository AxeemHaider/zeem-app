package org.octabyte.zeem.InstantChat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.ChatMessage;
import com.appspot.octabyte_zeem.zeem.model.TaskComplete;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.SimpleShowcaseEventListener;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

public class InstantChatActivity extends AppCompatActivity{

    public static Boolean instant_chat_active = false;

    private RecyclerView rvMessages;
    private ImageView ivUserPic, ivMessageSend;
    private TextView tvUserFullName;
    private EditText etMessageText;

    private List<MessageModel> messages = new ArrayList<>();
    private MessageModel message;
    public static String senderToken;
    private String appUserToken;
    private ChatAdapter chatAdapter;
    private Long messagerId;
    private boolean isAnonymousChat = false;
    private long appUserId;
    private String appProfilePic;
    private String appUserName;
    private String userPic, userName, chatAnonymous;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        instant_chat_active = true;

        SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        appUserToken = pref.getString("firebaseToken", null);
        appUserId = pref.getLong("userId", 0L);
        appProfilePic = pref.getString("profilePic", null);
        appUserName = pref.getString("fullName", null);

        initVariable();

        setupChatAdapter();

        try {
            senderToken = getIntent().getStringExtra("ARG_INSTANT_CHAT_SENDER_TOKEN");
            Long messageId = getIntent().getLongExtra("ARG_INSTANT_CHAT_MESSAGE_ID", 0L);
            String messageText = getIntent().getStringExtra("ARG_INSTANT_CHAT_MESSAGE_TEXT");
            userPic = getIntent().getStringExtra("ARG_INSTANT_CHAT_USER_PIC");
            userName = getIntent().getStringExtra("ARG_INSTANT_CHAT_USER_NAME");
            chatAnonymous = getIntent().getStringExtra("ARG_INSTANT_CHAT_ANONYMOUS");

            if (chatAnonymous != null){
                if (!chatAnonymous.equals("chat_anonymous")){
                    String[] chatAnonymousData = chatAnonymous.split("_");
                    messagerId = Long.valueOf(chatAnonymousData[0]);
                    if (appUserId == messagerId) {
                        isAnonymousChat = Boolean.parseBoolean(chatAnonymousData[1]);
                    }
                }
            }

            if (userPic.equals("anonymous")){
                ivUserPic.setImageResource(R.drawable.ic_anonymous);
                tvUserFullName.setText("Anonymous Person");
            }else{
                Glide.with(this).load(Utils.bucketURL + userPic)
                        .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                        .apply(RequestOptions.circleCropTransform()).into(ivUserPic);
                tvUserFullName.setText(userName);
            }

            if (messageId != 0L) {
                message = new MessageModel();
                message.setMessageId(messageId);
                message.setSenderToken(senderToken);
                message.setMessage(messageText);

                chatAdapter.insertItem(message);

                // Send a message that I have received this message
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setMessageId(messageId);
                chatMessage.setSenderToken(appUserToken);
                chatMessage.setReceiverToken(senderToken);
                chatMessage.setIsAnonymous(String.valueOf(false));
                chatMessage.setMessageReceived(String.valueOf(true));
                chatMessage.setInstantChat(true);

                UserTask<TaskComplete> userTask = new UserTask<>();
                userTask.setChatMessage(chatMessage);
                userTask.setListener(new UserTask.Response<TaskComplete>() {
                    @Override
                    public void response(TaskComplete response) {
                        if (response != null){
                            if (response.getComplete()){
                                Log.e("InstantChat", "Successfully chat request send");
                            }else{
                                Log.e("InstantChat", "Chat request sending fail");
                            }
                        }else{
                            Log.e("InstantChat", "null response in ProfileActivity -> sendChatRequest");
                            Log.e("InstantChat", "Chat request sending fail");
                        }
                    }
                });
                userTask.execute("sendChatMessage");

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(instantChatReceiver, new IntentFilter("new-instant-chat-local-receiver"));
    }

    private void initVariable(){

        ImageView toolbarNavigationBack = (ImageView) findViewById(R.id.toolbarNavigationBack);
        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InstantChatActivity.this, HomeActivity.class));
            }
        });

        rvMessages = (RecyclerView) findViewById(R.id.rvMessages);
        ivUserPic = (ImageView) findViewById(R.id.ivUserPic);
        tvUserFullName = (TextView) findViewById(R.id.tvUserFullName);
        etMessageText = (EditText) findViewById(R.id.etMessageText);
        ivMessageSend = (ImageView) findViewById(R.id.ivMessageSend);

        ivMessageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendThisMessage();
            }
        });

        showChatInfoFirstTime();
    }

    private void showChatInfoFirstTime(){
        final SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        boolean firstTime = sharedPreferences.getBoolean("InstantChatFirstTime", true);

        if (firstTime) {
            new ShowcaseView.Builder(this)
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(R.string.info_instant_chat_title)
                    .setContentText(R.string.info_instant_chat_detail)
                    .replaceEndButton(R.layout.show_case_hide_button)
                    .setShowcaseEventListener(new SimpleShowcaseEventListener(){
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("InstantChatFirstTime", false);
                            editor.apply();
                        }
                    })
                    .build();
        }

    }

    private void sendThisMessage(){
        String messageText = etMessageText.getText().toString();

        if (messageText.isEmpty()){
            Toast.makeText(this, "Type a message", Toast.LENGTH_SHORT).show();
            return;
        }

        message = new MessageModel();
        message.setMessage(messageText);
        message.setSender(true);

        chatAdapter.insertItem(message);
        rvMessages.scrollToPosition(chatAdapter.getItemCount() - 1);

        etMessageText.setText("");

        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageId(message.getMessageId());
        chatMessage.setMessageText(messageText);
        chatMessage.setSenderToken(appUserToken);
        chatMessage.setReceiverToken(senderToken);
        chatMessage.setProfilePic(appProfilePic);
        chatMessage.setUsername(Utils.capitalize(appUserName));
        chatMessage.setIsAnonymous(String.valueOf(isAnonymousChat));
        chatMessage.setMessageReceived(String.valueOf(false));
        chatMessage.setInstantChat(true);
        chatMessage.setChatAnonymous(chatAnonymous);

        if (isAnonymousChat)
            chatMessage.setChatTitle("Anonymous Person");
        else
            chatMessage.setChatTitle(Utils.capitalize(appUserName));

        UserTask<TaskComplete> userTask = new UserTask<>();
        userTask.setChatMessage(chatMessage);
        userTask.setListener(new UserTask.Response<TaskComplete>() {
            @Override
            public void response(TaskComplete response) {
                if (response != null){
                    if (response.getComplete()){
                        messageSend(chatMessage.getMessageId());
                    }else{
                        messageFailed();
                    }
                }else{
                    Log.e("InstantChat", "null response in ProfileActivity -> sendChatRequest");
                    Log.e("InstantChat", "Chat request sending fail");
                }
            }
        });
        userTask.execute("sendChatMessage");

    }

    private void messageSend(Long messageId) {
        for (MessageModel messageModel : chatAdapter.getMessages()){
            if (messageModel.getMessageId().equals(messageId)){
                int index = chatAdapter.getMessages().indexOf(messageModel);

                messageModel.setSend(true);

                if (index != -1){
                    chatAdapter.updateItem(index, messageModel);
                }
            }
        }
    }

    private void messageFailed() {
        Toast.makeText(this, "Message fail", Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver instantChatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            String messageSenderToken = intent.getStringExtra("senderToken");
            Long messageId = intent.getLongExtra("messageId", 0L);
            String messageText = intent.getStringExtra("messageText");
            boolean received = intent.getBooleanExtra("received", false);

            if (senderToken == null){
                senderToken = messageSenderToken;
            }else if (senderToken.equals(messageSenderToken)){

                if (chatAdapter == null){
                    setupChatAdapter();
                }

                // IF received true it means it's a info message
                if (received){
                    for (MessageModel messageModel : chatAdapter.getMessages()){
                        if (messageModel.getMessageId().equals(messageId)){
                            int index = chatAdapter.getMessages().indexOf(messageModel);

                            messageModel.setReceived(true);

                            if (index != -1){
                                chatAdapter.updateItem(index, messageModel);
                            }
                        }
                    }
                }else{
                    // New message is received ADD it
                    message = new MessageModel();

                    message.setMessageId(messageId);
                    message.setMessage(messageText);
                    message.setSenderToken(senderToken);

                    chatAdapter.insertItem(message);
                    rvMessages.scrollToPosition(chatAdapter.getItemCount() - 1);

                    // Send back to message that I have received this message
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setMessageId(messageId);
                    chatMessage.setSenderToken(appUserToken);
                    chatMessage.setReceiverToken(senderToken);
                    chatMessage.setIsAnonymous(String.valueOf(false));
                    chatMessage.setMessageReceived(String.valueOf(true));
                    chatMessage.setInstantChat(true);

                    UserTask<TaskComplete> userTask = new UserTask<>();
                    userTask.setChatMessage(chatMessage);
                    userTask.setListener(new UserTask.Response<TaskComplete>() {
                        @Override
                        public void response(TaskComplete response) {
                            if (response != null){
                                if (response.getComplete()){
                                    Log.e("InstantChat", "Successfully chat request send");
                                }else{
                                    Log.e("InstantChat", "Chat request sending fail");
                                }
                            }else{
                                Log.e("InstantChat", "null response in ProfileActivity -> onBroadCastReceiver");
                                Log.e("InstantChat", "Chat request sending fail");
                            }
                        }
                    });
                    userTask.execute("sendChatMessage");

                }


            }

        }
    };

    private void setupChatAdapter(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        rvMessages.setLayoutManager(linearLayoutManager);
        rvMessages.setHasFixedSize(true);

        chatAdapter = new ChatAdapter(this, messages);
        rvMessages.setAdapter(chatAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        instant_chat_active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        instant_chat_active = false;
        stickNotification();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(instantChatReceiver);
        super.onDestroy();
    }


    private void stickNotification(){

        String titleUsername = userName;
        boolean isAnonymous = false;

        Intent intent = new Intent(this, InstantChatActivity.class);
        intent.putExtra("ARG_INSTANT_CHAT_SENDER_TOKEN", senderToken);

        if (userPic != null) {
            if (userPic.equals("anonymous")) {
                intent.putExtra("ARG_INSTANT_CHAT_USER_PIC", "anonymous");
                intent.putExtra("ARG_INSTANT_CHAT_USER_NAME", "Anonymous Person");
                titleUsername = "Anonymous Person";
                isAnonymous = true;
            } else {
                intent.putExtra("ARG_INSTANT_CHAT_USER_PIC", userPic);
                intent.putExtra("ARG_INSTANT_CHAT_USER_NAME", userName);
            }
        }

        Random random = new Random();
        final int notificationId = random.nextInt();

        intent.putExtra("ARG_INSTANT_CHAT_ANONYMOUS", chatAnonymous);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationId, intent, PendingIntent.FLAG_ONE_SHOT);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_chat)
                .setContentTitle("Chat with "+titleUsername)
                .setContentText("Tap to continue chat...")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(notificationId, notificationBuilder.build());

    }

}
