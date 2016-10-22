package com.di7ak.cus;

import android.net.Uri;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class Mail {
    
    public static void sendMessage(Session session, UserData user, String message) throws SpacesException {
        StringBuilder args = new StringBuilder();
        args.append("method=").append("sendMessage")
            .append("&user=").append(Uri.encode(user.name))
            .append("&sid=").append(Uri.encode(session.sid))
            .append("&CK=").append(Uri.encode(session.ck))
            .append("&texttT=").append(Uri.encode(message));

        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://spaces.ru/neoapi/mail/").openConnection();

            con.setRequestMethod("POST");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(args.toString());
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            int code = json.getInt("code");
            if (code != 0) throw new SpacesException(code);
        } catch (IOException e) {
            throw new SpacesException(-1);
        } catch (JSONException e) {
            throw new SpacesException(-2);
        }
    }
    
}
