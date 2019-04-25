package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.appspot.octabyte_zeem.zeem.ZeemRequest;
import com.appspot.octabyte_zeem.zeem.ZeemRequestInitializer;
import com.appspot.octabyte_zeem.zeem.model.ChatMessage;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.octabyte.zeem.Utils.Utils;

import java.io.IOException;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class UserTask<T> extends AsyncTask<String, Void, T>{

    private Response<T> response;

    private Long userId;
    private Long alertId = 123L;
    private String mode, relationType;
    private String profilePic, fullName, status, email;
    private int dob;
    private String showTagPost, postTag;
    private Boolean anonymousTag;
    private Integer offset;
    private String cursor;
    private Long myUserId;
    private String feedSafeKey;
    private Long lastNotificationId;
    private Double lat, lng;
    private String firebaseToken;
    private ChatMessage chatMessage;

    public UserTask() {
    }

    public UserTask(Long userId) {
        this.userId = userId;
    }

    public UserTask(Long userId, String mode) {
        this.userId = userId;
        this.mode = mode;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T doInBackground(String... strings) {
        String action = strings[0];

        // Create Api service
        Zeem.Builder builder = new Zeem.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        //builder.setZeemRequestInitializer(requestInitializer);
        Zeem zeem = builder.build();

        try {
            switch (action){

                case "sendChatMessage":
                    return (T) zeem.userApi().sendInstantMessage(chatMessage).setKey(ApiKey).execute();
                case "refreshFirebaseToken":
                    return (T) zeem.userApi().refreshFirebaseToken(userId, firebaseToken).setKey(ApiKey).execute();
                case "updateUserLocation":
                    return (T) zeem.userApi().updateUserLocation(userId, lat, lng).setKey(ApiKey).execute();
                case "getProfileView":
                    return (T) zeem.userApi().getProfileView(myUserId).setUserId(userId).setKey(ApiKey).execute();
                case "generateUserSuggestion":
                    return (T) zeem.userApi().generateUserSuggestion(userId).setKey(ApiKey).execute();
                case "getUpdationAlert":
                    return (T) zeem.userApi().getUpdationAlert(userId).setKey(ApiKey).execute();
                case "getFeed":
                    return (T) zeem.userApi().getFeed(userId, mode).setCursor(cursor).setKey(ApiKey).execute();
                case "deleteFeed":
                    return (T) zeem.userApi().deleteFeed(feedSafeKey).setKey(ApiKey).execute();
                case "updateFeed":
                    return (T) zeem.userApi().getUpdateFeed(userId, alertId, mode).setKey(ApiKey).execute();
                case "getStoriesFeed":
                    return (T) zeem.userApi().getStoriesFeed(userId, mode).setKey(ApiKey).execute().getItems();
                case "getRelation":
                    return (T) zeem.userApi().getRelation(userId, relationType).setKey(ApiKey).execute().getItems();
                case "getNotification":
                    return (T) zeem.userApi().getNotification(userId, mode).setCursor(cursor).setKey(ApiKey).execute();
                case "updateNotification":
                    return (T) zeem.userApi().getUpdateNotification(userId, mode, alertId).setKey(ApiKey).execute();
                case "clearNotification":
                    return (T) zeem.userApi().deleteNotification(userId, mode, lastNotificationId).setKey(ApiKey).execute();
                case "getFriendRequest":
                    return (T) zeem.userApi().getFriendRequest(userId).setKey(ApiKey).execute().getItems();
                case "getSavedPost":
                    return (T) zeem.userApi().getSavedPost(userId).setKey(ApiKey).execute();
                case "getUserPost":
                    return (T) zeem.userApi().getUserPost(userId).setPostMode(mode).setKey(ApiKey).execute();
                case "getUserProfile":
                    return (T) zeem.userApi().getUserProfile(userId).setKey(ApiKey).execute();
                case "getUser":
                    return (T) zeem.userApi().getUser(userId).setKey(ApiKey).execute();
                case "updateProfile":
                    return (T) zeem.userApi().updateProfile(userId).setProfilePic(profilePic)
                            .setFullName(fullName).setStatus(status).setEmail(email).setDob(dob).setKey(ApiKey).execute();
                case "updateSetting":
                    return (T) zeem.userApi().updateSetting(userId).setShowTagPost(showTagPost)
                            .setPostTag(postTag).setAnonymousTag(anonymousTag).setKey(ApiKey).execute();
                case "discover":
                    return (T) zeem.userApi().discover(userId, offset).setKey(ApiKey).execute();

            }
        } catch (IOException e) {
            e.printStackTrace();

            return null;
        } catch (Exception e){
            e.printStackTrace();

            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(T t) {
        try {
            response.response(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(Response<T> response) {
        this.response = response;
    }

    public interface Response<T>{
        void response(T response);
    }

    /*private ZeemRequestInitializer requestInitializer = new ZeemRequestInitializer(ApiKey){
        @Override
        protected void initializeZeemRequest(ZeemRequest<?> request) throws IOException {
            super.initializeZeemRequest(request);

            request.getRequestHeaders().set("X-Android-Package", Utils.appPackageName);
            request.getRequestHeaders().set("X-Android-Cert", Utils.appSignature);
        }
    };*/

    public void setLastNotificationId(Long lastNotificationId) {
        this.lastNotificationId = lastNotificationId;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public void setGender(String gender) {
        String gender1 = gender;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setFeedSafeKey(String feedSafeKey) {
        this.feedSafeKey = feedSafeKey;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public void setMyUserId(Long myUserId) {
        this.myUserId = myUserId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setAlertId(Long alertId) {
        this.alertId = alertId;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDob(int dob) {
        this.dob = dob;
    }

    public void setShowTagPost(String showTagPost) {
        this.showTagPost = showTagPost;
    }

    public void setPostTag(String postTag) {
        this.postTag = postTag;
    }

    public void setAnonymousTag(Boolean anonymousTag) {
        this.anonymousTag = anonymousTag;
    }
}
