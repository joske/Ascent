package be.sourcery.ascent.eighta;

import android.util.Log;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import be.sourcery.ascent.XMLParser;

/**
 * Created by jos on 16/02/17.
 */

public class Login {

    private final String email;
    private final String password;
    private String sessionId;
    private String userId;

    public Login(String email, String password) {

        this.email = email;
        this.password = password;
        this.sessionId = createSessionId();
    }

    public String login() {
        String result = getPayload(createLoginURL());
        if (result != null) {
            userId = result.replaceAll(".*<UserId>(.*)</UserId>.*", "$1");
            return userId;
        }
        return null;
    }

    public String getAscentsURL() {
        return String.format("http://www.8a.nu/scorecard/xml/%s_routes.xml", userId);
    }

    public String getSessionId() {
        return sessionId;
    }

    public EightAAscent[] getRouteAscents() {
        HttpURLConnection httpURLConnection3 = null;
        try {
            httpURLConnection3 = (HttpURLConnection) new URL(getAscentsURL()).openConnection();
            httpURLConnection3.setRequestMethod("GET");
            httpURLConnection3.setRequestProperty("Content-length", "0");
            httpURLConnection3.setUseCaches(false);
            httpURLConnection3.setAllowUserInteraction(false);
            httpURLConnection3.setConnectTimeout(60000);
            httpURLConnection3.setReadTimeout(60000);
            httpURLConnection3.connect();
            switch (httpURLConnection3.getResponseCode()) {
                case 200:
                case 201:
                    Parser parser = new Parser();
                    return parser.parse(httpURLConnection3.getInputStream());
            }
        } catch (Exception e) {
            Log.e(toString(), "failed to parse", e);
        } finally {
            if (httpURLConnection3 != null) {
                httpURLConnection3.disconnect();
            }
        }
        return null;
    }

    private String getPayload(String url) {
        HttpURLConnection httpURLConnection3 = null;
        try {
            httpURLConnection3 = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection3.setRequestMethod("GET");
            httpURLConnection3.setRequestProperty("Content-length", "0");
            httpURLConnection3.setUseCaches(false);
            httpURLConnection3.setAllowUserInteraction(false);
            httpURLConnection3.setConnectTimeout(60000);
            httpURLConnection3.setReadTimeout(60000);
            httpURLConnection3.connect();
            switch (httpURLConnection3.getResponseCode()) {
                case 200:
                case 201:
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection3.getInputStream()));
                    while (true) {
                        String readLine = bufferedReader.readLine();
                        if (readLine == null) {
                            bufferedReader.close();
                            return stringBuilder.toString();
                        }
                        stringBuilder.append(readLine.trim());
                    }
            }
        } catch (Exception e) {
            Log.e(toString(), "failed to parse", e);
        } finally {
            if (httpURLConnection3 != null) {
                httpURLConnection3.disconnect();
            }
        }
        return null;
    }

    private String createLoginURL() {
        StringBuilder stringBuilder = new StringBuilder("http://www.8a.nu/API/AuthenticateUser.aspx?UserEmail=");
        stringBuilder.append(email);
        stringBuilder.append("&SessionId=");
        stringBuilder.append(sessionId);
        return stringBuilder.toString();
    }

    private String createSessionId() {
        StringBuilder stringBuilder = new StringBuilder();
        String stringBuilder2 = new StringBuilder(String.valueOf(password)).append("rghfm4j4wz3mcfqxdvptdl55").toString();
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(stringBuilder2.getBytes());
            byte[] digest = instance.digest();
            for (byte b : digest) {
                stringBuilder.append(Integer.toString((b & 255) + 256, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
