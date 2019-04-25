package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.appspot.octabyte_zeem.zeem.model.UserList;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.List;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class ListTask<T> extends AsyncTask<String, Void, T> {

    private Response<T> response;

    private Long userId;
    private Long listId;
    private UserList userList;
    private List<Long> memberIds;
    private String listSafeKey;

    public ListTask() {
    }

    public ListTask(Long userId) {
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
                case "createList":
                    return (T) zeem.listApi().createList(userId, userList).setMemberIds(memberIds).setKey(ApiKey).execute();
                case "getUserList":
                    return (T) zeem.listApi().getUserList(userId).setKey(ApiKey).execute().getItems();
                case "getListMembers":
                    return (T) zeem.listApi().getListMembers(listId).setKey(ApiKey).execute().getItems();
                case "removeList":
                    return (T) zeem.listApi().removeList(listSafeKey).setKey(ApiKey).execute();
                case "removeMember":
                    return (T) zeem.listApi().removeListMember(listId, userId).setKey(ApiKey).execute();

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

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setListSafeKey(String listSafeKey) {
        this.listSafeKey = listSafeKey;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public void setUserList(UserList userList) {
        this.userList = userList;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }
}
