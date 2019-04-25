package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class SearchTask<T> extends AsyncTask<String, Void, T> {

    private Response<T> response;

    private Long myUserId, userId;
    private String mode, relation;
    private int badge;
    private String fullName;
    private Double latitude, longitude;
    private String username;
    private Boolean isAnonymous;

    public SearchTask() {
    }

    public SearchTask(Long userId) {
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
                case "topPost":
                    return (T) zeem.searchApi().topPost(userId, mode).setKey(ApiKey).execute();
                case "filterBadgeUser":
                    return (T) zeem.searchApi().filterBadgeUser(userId, relation, badge).setKey(ApiKey).execute().getItems();
                case "filterBadgePost":
                    return (T) zeem.searchApi().filterBadgePost(userId, mode, badge).setKey(ApiKey).execute();
                case "nearBy":
                    return (T) zeem.searchApi().nearBy(userId, latitude, longitude, relation).setKey(ApiKey).execute().getItems();
                case "findUser":
                    return (T) zeem.searchApi().findUser().setUsername(username).setKey(ApiKey).execute();
                case "searchUser":
                    return (T) zeem.searchApi().searchUser(fullName).setKey(ApiKey).execute().getItems();
                case "findRelation":
                    return (T) zeem.searchApi().getRelationship(myUserId, userId).setKey(ApiKey).execute();
                case "searchTagUser":
                    return (T) zeem.searchApi().getMentionUser(userId, mode, isAnonymous).setSearchName(fullName).setKey(ApiKey).execute().getItems();
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

    public interface Response<T>{
        void response(T response);
    }

    public void setAnonymous(Boolean anonymous) {
        isAnonymous = anonymous;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setMyUserId(Long myUserId) {
        this.myUserId = myUserId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

}