package be.sourcery.ascent;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jos on 06/02/17.
 */

public class CodecUtil {

    private static DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

    public static String encode(Ascent ascent) {
        StringBuilder line = new StringBuilder(200);
        line.append(ascent.getRoute().getName()).append("\t");
        line.append(ascent.getRoute().getGrade()).append("\t");
        line.append(ascent.getRoute().getCrag().getName()).append("\t");
        line.append(ascent.getRoute().getSector()).append("\t");
        line.append(ascent.getRoute().getCrag().getCountry()).append("\t");
        line.append(ascent.getStyle()).append("\t");
        line.append(ascent.getAttempts()).append("\t");
        line.append(fmt.format(ascent.getDate())).append("\t");
        line.append(ascent.getComment()).append("\t");
        line.append(ascent.getStars()).append("\r\n");
        return line.toString();
    }

    public static Ascent decode(String line) {
        String[] strings = line.split("\t");
        if (strings.length == 10) {
            try {
                String routeName = strings[0];
                String routeGrade = strings[1];
                String cragName = strings[2];
                String sector = strings[3];
                String cragCountry = strings[4];
                int style = Integer.parseInt(strings[5]);
                int attempts = Integer.parseInt(strings[6]);
                Date date = fmt.parse(strings[7]);
                String comments = strings[8];
                int stars = Integer.parseInt(strings[9]);

                Crag crag = new Crag(-1, cragName, cragCountry);
                Route route = new Route(-1, routeName, routeGrade, crag, 0, sector);
                return new Ascent(-1, route, style, attempts, date, comments, stars, 0);
            } catch (Exception e) {
                Log.e(CodecUtil.class.getName(), e.getMessage(), e);
            }
        } else {
            Log.w(CodecUtil.class.getName(), "failed to parse string: " + line);
        }
        return null;
    }

}
