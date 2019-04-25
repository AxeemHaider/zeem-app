package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.appspot.octabyte_zeem.zeem.model.Stories;
import com.appspot.octabyte_zeem.zeem.model.StoryComment;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class StoryTask<T> extends AsyncTask<String, Void, T> {

    private Response<T> response;

    private Long userId, storyId;
    private String mode;
    private Stories story;
    private Integer storyNum;
    private String storySafeKey;
    private StoryComment storyComment;
    private String cursor;

    public StoryTask() {
    }

    public StoryTask(Long userId) {
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
                case "createStory":
                    return (T) zeem.storyApi().createStory(userId, mode, story).setKey(ApiKey).execute();
                case "storyComment":
                    return (T) zeem.storyApi().getStoryComments(storyId, storyNum).setCursor(cursor).setKey(ApiKey).execute();
                case "createStoryComment":
                    return (T) zeem.storyApi().postStoryComment(userId, storySafeKey, storyComment).setKey(ApiKey).execute();

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

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setStorySafeKey(String storySafeKey) {
        this.storySafeKey = storySafeKey;
    }

    public void setStoryComment(StoryComment storyComment) {
        this.storyComment = storyComment;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setStory(Stories story) {
        this.story = story;
    }

    public void setStoryId(Long storyId) {
        this.storyId = storyId;
    }

    public void setStoryNum(Integer storyNum) {
        this.storyNum = storyNum;
    }
}