package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.appspot.octabyte_zeem.zeem.model.Post;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.List;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class PostTask<T> extends AsyncTask<String, Void, T> {

    private Response<T> response;

    private String postSafeKey, reportType;
    private Long userId, postId;
    private Boolean isPublic;
    private Post newPost;
    private List<Long> taggedUserList;

    public PostTask() {
    }

    public PostTask(Long userId) {
        this.userId = userId;
    }

    public PostTask(String postSafeKey) {
        this.postSafeKey = postSafeKey;
    }

    public PostTask(Long userId, String postSafeKey, String reportType) {
        this.postSafeKey = postSafeKey;
        this.reportType = reportType;
        this.userId = userId;
    }

    public PostTask(Long userId, Long postId, Boolean isPublic) {
        this.userId = userId;
        this.postId = postId;
        this.isPublic = isPublic;
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
                case "getSinglePost":
                    return (T) zeem.postApi().getSinglePost(userId, postSafeKey).setKey(ApiKey).execute();
                case "createPost":
                    return (T) zeem.postApi().createPost(userId, newPost).setTagUserId(taggedUserList).setKey(ApiKey).execute();
                case "deletePost":
                    return (T) zeem.postApi().deletePost(postSafeKey).setKey(ApiKey).execute();
                case "getPostStar":
                    return (T) zeem.postApi().getPostStar(postSafeKey).setKey(ApiKey).execute().getItems();
                case "report":
                    return (T) zeem.postApi().report(userId, postSafeKey, reportType).setKey(ApiKey).execute();
                case "tagApproved":
                    return (T) zeem.postApi().tagApproved(userId, postId, isPublic).setKey(ApiKey).execute();
                case "getTaggedUser":
                    return (T) zeem.postApi().getTaggedUser(postId).setKey(ApiKey).execute().getItems();
                case "postStar":
                    return (T) zeem.postApi().postStar(userId, postSafeKey).setKey(ApiKey).execute();
                case "savePost":
                    return (T) zeem.postApi().savePost(userId, postSafeKey).setKey(ApiKey).execute();
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

    public void setNewPost(Post newPost) {
        this.newPost = newPost;
    }

    public void setTaggedUserList(List<Long> taggedUserList) {
        this.taggedUserList = taggedUserList;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public void setPostSafeKey(String postSafeKey) {
        this.postSafeKey = postSafeKey;
    }
}
