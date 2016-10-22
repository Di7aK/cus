package com.di7ak.cus;

import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Comm {
    public String id;
    public String name;

    public CommResult getUsers(int page) throws SpacesException {
        CommResult result = new CommResult();
        result.users = new ArrayList<UserData>();

        StringBuilder url = new StringBuilder("http://spaces.ru/comm/users/?")
            .append("Comm=").append(Uri.encode(id))
            .append("&P=").append(Integer.toString(page));

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("X-Proxy", "spaces");

            InputStream inputStream = con.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            JSONObject json = new JSONObject(baos.toString("UTF-8"));
            int code = json.getInt("code");
            if (code != 0) throw new SpacesException(code);
            if (json.has("pagination") && !json.isNull("pagination")) {
                result.pagination = PaginationData.fromJson(json.getJSONObject("pagination"));
            } else {
                result.pagination = new PaginationData();
                result.pagination.currentPage = page;
                result.pagination.lastPage = page;
            }
            if (json.has("users_list")) {
                JSONArray users = json.getJSONArray("users_list");
                for (int i = 0; i < users.length(); i ++) {
                    JSONObject user = users.getJSONObject(i);
                    result.users.add(UserData.fromJson(user));
                }
                result.pagination.itemsOnPage = result.users.size();
            }
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
        }
        return result;
    }

    public static Comm getByAddress(String address) throws SpacesException {
        Comm comm = new Comm();
        StringBuilder url = new StringBuilder("http://spaces.ru/comm/comm_show/?")
            .append("address=").append(Uri.encode(address));

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url.toString()).openConnection();

            con.setRequestMethod("GET");
            con.setRequestProperty("X-Proxy", "spaces");

            InputStream inputStream = con.getInputStream();
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            JSONObject json = new JSONObject(result.toString("UTF-8"));
            int code = json.getInt("code");
            if (code != 0) throw new SpacesException(code);
            if (json.has("comm_widget")) {
                JSONObject commWidget = json.getJSONObject("comm_widget");
                if (commWidget.has("id")) comm.id = commWidget.getString("id");
                if (commWidget.has("comm_name")) comm.name = commWidget.getString("comm_name");
            } else throw new SpacesException(22);

        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
        }
        return comm;
    }

    public class CommResult {
        public List<UserData> users;
        public PaginationData pagination;
    }
}
