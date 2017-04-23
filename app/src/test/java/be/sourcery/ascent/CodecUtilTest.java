package be.sourcery.ascent;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
        assertEquals("Foo\t7b\tFreyr\tPucelles\tBelgium\t1\t1\t2017-02-06\tcool\t3\r\n", encoded);
    }

    @Test
    public void testEncodeWithSemicolon() {
        Calendar calendar = new GregorianCalendar();
        calendar.set(2017, 1, 6);
        Date date = calendar.getTime();
        Ascent ascent = new Ascent(-1, new Route(-1, "Foo", "7b", new Crag(-1, "Freyr", "Belgium"), 0, "Pucelles"), Ascent.STYLE_ONSIGHT, 1, date, "cool ;-)", 3, 0);
        String encoded = CodecUtil.encode(ascent);
        assertEquals("Foo\t7b\tFreyr\tPucelles\tBelgium\t1\t1\t2017-02-06\tcool ;-)\t3\r\n", encoded);
    }

    @Test
    public void testDecode() {
        String line = "Peche au Kiwi\t7b\tFreyr\tPucelles\tBelgium\t3\t44\t2017-02-06\tcool\t3";
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
        String line = "Peche au Kiwi\t7b\tFreyr\tPucelles\tBelgium\t3\t44\t2017-02-06\tcool ;-)\t3";
        Ascent ascent = CodecUtil.decode(line);
        assertNotNull(ascent);
        assertEquals("Peche au Kiwi", ascent.getRoute().getName());
        assertEquals("7b", ascent.getRoute().getGrade());
        assertEquals("Pucelles", ascent.getRoute().getSector());
        assertEquals("Freyr", ascent.getRoute().getCrag().getName());
        assertEquals("Belgium", ascent.getRoute().getCrag().getCountry());
        assertEquals("cool ;-)", ascent.getComment());
        assertEquals(3, ascent.getStars());
        assertEquals(3, ascent.getStyle());
        assertEquals(44, ascent.getAttempts());
    }

}