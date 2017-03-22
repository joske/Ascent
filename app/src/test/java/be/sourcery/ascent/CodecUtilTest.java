package be.sourcery.ascent;

import android.util.Log;
import android.util.Xml;

import org.junit.Test;
import org.mockito.Mock;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import be.sourcery.ascent.eighta.EightAAscent;
import be.sourcery.ascent.eighta.Login;

import static org.junit.Assert.*;

/**
 * Created by jos on 06/02/17.
 */
public class CodecUtilTest {

    @Test
    public void testEncode() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2017, 1, 6);
        Date date = calendar.getTime();
        Ascent ascent = new Ascent(-1, new Route(-1, "Foo", "7b", new Crag(-1, "Freyr", "Belgium"), 0, "Pucelles"), Ascent.STYLE_ONSIGHT, 1, date, "cool", 3, 0);
        String encoded = CodecUtil.encode(ascent);
        assertEquals("Foo\t7b\tFreyr\tBelgium\t1\t1\t2017-02-06\tcool\t3\r\n", encoded);
    }

    @Test
    public void testEncodeWithSemicolon() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2017, 1, 6);
        Date date = calendar.getTime();
        Ascent ascent = new Ascent(-1, new Route(-1, "Foo", "7b", new Crag(-1, "Freyr", "Belgium"), 0, "Pucelles"), Ascent.STYLE_ONSIGHT, 1, date, "cool ;-)", 3, 0);
        String encoded = CodecUtil.encode(ascent);
        assertEquals("Foo\t7b\tFreyr\tBelgium\t1\t1\t2017-02-06\tcool ;-)\t3\r\n", encoded);
    }

    @Test
    public void testDecode() {
        String line = "Peche au Kiwi\t7b\tFreyr\tBelgium\t3\t44\t2017-02-06\tcool\t3";
        Ascent ascent = CodecUtil.decode(line);
        assertEquals("Peche au Kiwi", ascent.getRoute().getName());
        assertEquals("7b", ascent.getRoute().getGrade());
        assertEquals("Freyr", ascent.getRoute().getCrag().getName());
        assertEquals("Belgium", ascent.getRoute().getCrag().getCountry());
        assertEquals("cool", ascent.getComment());
        assertEquals(3, ascent.getStars());
        assertEquals(3, ascent.getStyle());
        assertEquals(44, ascent.getAttempts());
    }

    @Test
    public void testDecodeWithSemicolon() {
        String line = "Peche au Kiwi\t7b\tFreyr\tBelgium\t3\t44\t2017-02-06\tcool ;-)\t3";
        Ascent ascent = CodecUtil.decode(line);
        assertNotNull(ascent);
        assertEquals("Peche au Kiwi", ascent.getRoute().getName());
        assertEquals("7b", ascent.getRoute().getGrade());
        assertEquals("Freyr", ascent.getRoute().getCrag().getName());
        assertEquals("Belgium", ascent.getRoute().getCrag().getCountry());
        assertEquals("cool ;-)", ascent.getComment());
        assertEquals(3, ascent.getStars());
        assertEquals(3, ascent.getStyle());
        assertEquals(44, ascent.getAttempts());
    }

    @Test
    public void testParseXML() {
        ClassLoader classLoader = CodecUtilTest.class.getClassLoader();
        InputStream stream = classLoader.getResourceAsStream("8a-list.xml" );
        XMLParser parser = new XMLParser();
        Document document = parser.parse(new InputStreamReader(stream));
        assertNotNull(document);
        NodeList nl = document.getElementsByTagName("Ascent");
        for (int i = 0; i < nl.getLength(); i++) {
            Node e = nl.item(i);
            String id = parser.getValue((Element)e, "Id");
            String name = parser.getValue((Element)e, "Name");
            assertNotNull(id);
            assertNotNull(name);
            System.out.println(id + "  " + name);
        }
    }

//    @Test
    public void testLogin() throws Exception {
        Login login = new Login("jos_dehaes@fastmail.fm", "jefke");
        String sessionId = login.getSessionId();
        String result = login.login();
        assertNotNull(result);
        System.out.println("userId:" + result);
        String url = login.getAscentsURL();
        assertNotNull(url);
        assertEquals("https://www.8a.nu/scorecard/xml/19961_routes.xml", url);
        XMLParser parser = new XMLParser();
        EightAAscent[] routeAscents = login.getRouteAscents();
        assertNotNull(routeAscents);
//        NodeList nl = document.getElementsByTagName("Ascent");
//        for (int i = 0; i < nl.getLength(); i++) {
//            Node e = nl.item(i);
//            String id = parser.getValue((Element)e, "Id");
//            String name = parser.getValue((Element)e, "Name");
//            assertNotNull(id);
//            assertNotNull(name);
//            System.out.println(id + "  " + name);
//        }

    }

}