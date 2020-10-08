package be.sourcery.ascent.eighta;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.List;

import be.sourcery.ascent.Ascent;
import be.sourcery.ascent.Crag;
import be.sourcery.ascent.InternalDB;
import be.sourcery.ascent.Route;

/**
 * Created by jos on 16/02/17.
 */

public class EightA {

    private String sessionId;
    private String userId;

    public EightA() {
    }

    public EightA(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    public String login(String email, String password) {
        sessionId = createSessionId(password);
        String result = getPayload(createLoginURL(email));
        if (result != null) {
            userId = result.replaceAll(".*<UserId>(.*)</UserId>.*", "$1");
            return userId;
        }
        return null;
    }

    public void pushAscents(List<Ascent> ascents) {
        EightAAscent[] eightAAscents = new EightAAscent[ascents.size()];
        int i = 0;
        for (Ascent ascent: ascents) {
            eightAAscents[i++] = convert(ascent);
        }
        pushAscents(eightAAscents);
    }

    private EightAAscent convert(Ascent ascent) {
        EightAAscent eightAAscent = new EightAAscent();
        eightAAscent.setName(ascent.getRoute().getName());
        eightAAscent.setCrag(ascent.getRoute().getCrag().getName());
        eightAAscent.setSector(ascent.getRoute().getSector());
        eightAAscent.setDate(ascent.getDate());
        eightAAscent.setScore(ascent.getScore());
        eightAAscent.setRating(ascent.getStars());
        eightAAscent.setStyle(ascent.getStyle());
        eightAAscent.setGrade(ascent.getRoute().getGrade());
        eightAAscent.setComment(ascent.getComment());
        eightAAscent.setCountryCode(ascent.getRoute().getCrag().getCountry());
        return eightAAscent;
    }

    private void pushAscents(EightAAscent[] ascents) {
        String format = "https://www.8a.nu/API/AddAscent.aspx?UserId={0}&AscentType={1}&AscentGrade={2}&AscentStyle={3}&AscentDate={4}&AscentName={5}&AscentCragName={6}&CragCountryCode={8}";
        for (EightAAscent ascent : ascents) {
            String url = MessageFormat.format(format, userId, 1, ascent.getStyle(), Grades.convertFrenchTo8a(ascent.getGrade()), ascent.getStyle(), ascent.getDate(), ascent.getName(), ascent.getCrag(), ascent.getCountryCode());
            if (ascent.getSector() != null) {
                url += "&AscentCragSectorName=" + ascent.getSector();
            }
            url += "&SessionId=" + sessionId;
            pushAscent(url);
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public long importData(Context context) {
        EightAAscent[] ascents = getAscents(getRoutesURL());
        return importAscents(context, ascents);
    }

    private String getRoutesURL() {
        assert userId != null;
        return String.format("https://www.8a.nu/scorecard/xml/%s_routes.xml", userId);
    }

    private String getBoulderURL() {
        assert userId != null;
        return String.format("https://www.8a.nu/scorecard/xml/%s_boulders.xml", userId);
    }

    private void pushAscent(String url) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-length", "0");
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setConnectTimeout(60000);
            con.setReadTimeout(60000);
            con.connect();
            switch (con.getResponseCode()) {
                case 200:
                case 201:
                    InputStream inputStream = con.getInputStream();
                    break;
            }
            if (con != null) {
                con.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EightAAscent[] getAscents(String url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-length", "0");
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setConnectTimeout(60000);
            con.setReadTimeout(60000);
            con.connect();
            switch (con.getResponseCode()) {
                case 200:
                case 201:
                    Parser parser = new Parser();
                    return parser.parse(con.getInputStream());
            }
        } catch (Exception e) {
            Log.e(toString(), "failed to parse", e);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    private String getPayload(String url) {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-length", "0");
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setConnectTimeout(60000);
            con.setReadTimeout(60000);
            con.connect();
            switch (con.getResponseCode()) {
                case 200:
                case 201:
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
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
            if (con != null) {
                con.disconnect();
            }
        }
        return null;
    }

    private String createLoginURL(String email) {
        StringBuilder stringBuilder = new StringBuilder("https://www.8a.nu/API/AuthenticateUser.aspx?UserEmail=");
        stringBuilder.append(email);
        stringBuilder.append("&SessionId=");
        stringBuilder.append(sessionId);
        return stringBuilder.toString();
    }

    private String createSessionId(String password) {
        StringBuilder stringBuilder = new StringBuilder();
        String salt = "rghfm4j4wz3mcfqxdvptdl55";
        String salted = new StringBuilder(password).append(salt).toString();
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            instance.update(salted.getBytes());
            byte[] digest = instance.digest();
            for (byte b : digest) {
                stringBuilder.append(Integer.toString((b & 255) + 256, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private long importAscents(Context context, EightAAscent[] ascents) {
        InternalDB db = new InternalDB(context);
        int added = 0;
        for (int i = 0; i < ascents.length; i++) {
            if (!db.contains(ascents[i].getId())) {
                Ascent ascent = importAscent(db, context, ascents[i]);
                Log.i(this.getClass().getName(), String.format("imported %d : %s", i, ascent));
                added++;
            } else {
                Log.i(this.getClass().getName(), String.format("not imported %d, already in", i));
            }
        }
        Log.i(this.getClass().getName(), String.format("imported %d ascents", added));
        return added;
    }

    private Ascent importAscent(InternalDB db, Context context, EightAAscent ascent) {
        Crag crag = db.getCrag(ascent.getCrag(), ascent.getCountryCode());
        if (crag == null) {
            crag = db.addCrag(ascent.getCrag(), ascent.getCountryCode());
        }
        Route route = db.getRoute(new Route(0, ascent.getName(), ascent.getGrade(), crag, 0, null));
        if (route == null) {
            route = db.addRoute(ascent.getName(), translateGrade(ascent.getGrade()), crag, ascent.getSector());
        }
        int attempts = ascent != null && ascent.getNote().equals("2") ? 2: 1;
        return db.addAscent(route, ascent.getDate(), attempts, translateStyle(ascent.getObjectClass(), ascent.isRepeat(), ascent.getStyle()), ascent.getComment(), ascent.getRating(), false, ascent.getId());
    }

    private String translateGrade(String grade) {
        if (grade != null && grade.startsWith("4")) {
            return "4";
        }
        if (grade != null && grade.startsWith("3")) {
            return "3";
        }
        return grade;
    }

    private int translateStyle(String objectClass, boolean repeat, int style) {
        if (objectClass != null && objectClass.equals("CLS_UserAscent_Try")) {
            return Ascent.STYLE_TRIED;
        }
        if (repeat) {
            return Ascent.STYLE_REPEAT;
        }
        switch (style) {
            case 1:
                return Ascent.STYLE_REDPOINT;
            case 2:
                return Ascent.STYLE_FLASH;
            case 3:
                return Ascent.STYLE_ONSIGHT;
            case 4:
                return Ascent.STYLE_TOPROPE;
        }
        return Ascent.STYLE_REDPOINT;
    }


}
