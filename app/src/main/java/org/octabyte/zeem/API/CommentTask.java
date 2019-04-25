package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.appspot.octabyte_zeem.zeem.model.Comment;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class CommentTask<T> extends AsyncTask<String, Void, T> {

    private Response<T> response;

    private Long userId, postId;
    private String postSafeKey, commentSafeKey, mode;
    private Comment comment;
    private String cursor;

    public CommentTask() {
    }

    public CommentTask(Long postId) {
        this.postId = postId;
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
                case "getComments":
                    return (T) zeem.commentApi().getComments(userId, postId).setCursor(cursor).setKey(ApiKey).execute();
                case "createComment":
                    return (T) zeem.commentApi().postComment(userId, postSafeKey, comment).setKey(ApiKey).execute();
                case "starComment":
                    return (T) zeem.commentApi().commentStar(userId, commentSafeKey, mode).setKey(ApiKey).execute();
                case "deleteComment":
                    return (T) zeem.commentApi().deleteComment(commentSafeKey).setKey(ApiKey).execute();
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

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public void setCommentSafeKey(String commentSafeKey) {
        this.commentSafeKey = commentSafeKey;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPostSafeKey(String postSafeKey) {
        this.postSafeKey = postSafeKey;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
