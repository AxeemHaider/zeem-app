package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class FriendTask<T> extends AsyncTask<String, Void, T> {

    private Response<T> response;

    private Long userId, requestId, friendId;
    private Long userPhone, friendPhone;

    public FriendTask() {
    }

    public FriendTask(Long userId) {
        this.userId = userId;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T doInBackground(String... strings) {
        String action = strings[0];

        // Create Api service
        Zeem.Builder builder = new Zeem.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        Zeem zeem = builder.build();

        try {
            switch (action){
                case "acceptFriendRequest":
                    return (T) zeem.friendApi().acceptFriendRequest(userId, requestId, friendId).setKey(ApiKey).execute();
                case "blockFriend":
                    return (T) zeem.friendApi().blockFriend(userId, friendId).setKey(ApiKey).execute();
                case "unBlockFriend":
                    return (T) zeem.friendApi().unBlockFriend(userId, friendId).setKey(ApiKey).execute();
                case "follow":
                    return (T) zeem.friendApi().follow(userId, friendId).setKey(ApiKey).execute();
                case "unFollow":
                    return (T) zeem.friendApi().unFollow(userId, friendId).setKey(ApiKey).execute();
                case "addFriend":
                    return (T) zeem.friendApi().addFriend(userId, userPhone, friendPhone).setKey(ApiKey).execute();

            }
        } catch (IOException e) {
            e.printStackTrace();
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

    interface Response<T>{
        void response(T response);
    }

    public void setUserPhone(Long userPhone) {
        this.userPhone = userPhone;
    }

    public void setFriendPhone(Long friendPhone) {
        this.friendPhone = friendPhone;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }
}