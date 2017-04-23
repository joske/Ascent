package be.sourcery.ascent.eighta;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jos on 16/02/17.
 *
 * Structure:
 * <Ascent>
 <Id>3080059</Id>
 <DBAscent>false</DBAscent>
 <CommentAsBlog>0</CommentAsBlog>
 <CragSector>Zeus</CragSector>
 <YellowId>255</YellowId>
 <FirstAscent>false</FirstAscent>
 <Repeat>0</Repeat>
 <ProjectAscentDate>0001-01-01T00:00:00</ProjectAscentDate>
 <UserId>19961</UserId>
 <Grade>25</Grade>
 <GradeNote>-1</GradeNote>
 <Rating>2</Rating>
 <ObjectClass>CLS_UserAscent_Try</ObjectClass>
 <GradeName>7c</GradeName>
 <Name>Blonde</Name>
 <Comment>boulder crux too hard for now</Comment>
 <CountryCode>GRC</CountryCode>
 <CragName>Kalymnos</CragName>
 <CragId>-1</CragId>
 <Type>0</Type>
 <Style>1</Style>
 <Note>0</Note>
 <TotalScore>900</TotalScore>
 <Date>2013-09-27T00:00:00</Date>
 <RecDate>2013-10-11T17:55:34</RecDate>
 <ExcludeFromRanking>0</ExcludeFromRanking>
 <UserRecommended>0</UserRecommended>
 <Chipped>0</Chipped>
 </Ascent>
 */

public class Parser {

    public EightAAscent[] parse(InputStream inputStream) {
        List arrayList = new ArrayList();
        try {
            XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
            newPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            newPullParser.setInput(inputStream, null);
            newPullParser.nextTag();
            newPullParser.require(XmlPullParser.START_TAG, null, "AscentList");
            while (newPullParser.next() != XmlPullParser.END_TAG) {
                if (newPullParser.getEventType() == XmlPullParser.START_TAG) {
                    arrayList.add(parseAscent(newPullParser));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (EightAAscent[]) arrayList.toArray(new EightAAscent[arrayList.size()]);
    }

    private EightAAscent parseAscent(XmlPullParser xmlPullParser) throws Exception {
        EightAAscent ascent = new EightAAscent();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        xmlPullParser.require(XmlPullParser.START_TAG, null, "Ascent");
        while (xmlPullParser.next() != XmlPullParser.END_TAG) {
            if (xmlPullParser.getEventType() == XmlPullParser.START_TAG) {
                String name = xmlPullParser.getName();
                if (name.equals("Name")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Name");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setName(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Name");
                } else if (name.equals("CragName")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "CragName");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setCrag(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "CragName");
                } else if (name.equals("CragSector")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "CragSector");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setSector(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "CragSector");
                } else if (name.equals("Date")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Date");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setDate(simpleDateFormat.parse(xmlPullParser.getText()));
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Date");
                } else if (name.equals("TotalScore")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "TotalScore");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setScore(Integer.valueOf(xmlPullParser.getText()).intValue());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "TotalScore");
                } else if (name.equals("Rating")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Rating");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setRating(Integer.valueOf(xmlPullParser.getText()).intValue());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Rating");
                } else if (name.equals("Style")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Style");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setStyle(Integer.valueOf(xmlPullParser.getText()).intValue());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Style");
                } else if (name.equals("GradeName")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "GradeName");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setGrade(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "GradeName");
                } else if (name.equals("ObjectClass")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "ObjectClass");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setObjectClass(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "ObjectClass");
                } else if (name.equals("Type")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Type");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setType(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Type");
                } else if (name.equals("CountryCode")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "CountryCode");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setCountryCode(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "CountryCode");
                } else if (name.equals("Comment")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Comment");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setComment(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Comment");
                } else if (name.equals("Repeat")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Repeat");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setRepeat(xmlPullParser.getText().equals("1"));
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Repeat");
                } else if (name.equals("Id")) {
                    xmlPullParser.require(XmlPullParser.START_TAG, null, "Id");
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        ascent.setId(xmlPullParser.getText());
                        xmlPullParser.nextTag();
                    }
                    xmlPullParser.require(XmlPullParser.END_TAG, null, "Id");
                } else {
                    readUninteresting(xmlPullParser);
                }
            }
        }
        return ascent;
    }

    private void readUninteresting(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        if (xmlPullParser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int i = 1;
        while (i != 0) {
            switch (xmlPullParser.next()) {
                case XmlPullParser.START_TAG:
                    i++;
                    break;
                case XmlPullParser.END_TAG:
                    i--;
                    break;
                default:
                    break;
            }
        }
    }

    private String readCode(XmlPullParser xmlPullParser) throws IOException, XmlPullParserException {
        if (!xmlPullParser.getName().equals("Code")) {
            return null;
        }
        String text;
        xmlPullParser.require(XmlPullParser.START_TAG, null, "Code");
        if (xmlPullParser.next() == XmlPullParser.TEXT) {
            text = xmlPullParser.getText();
            xmlPullParser.nextTag();
        } else {
            text = null;
        }
        xmlPullParser.require(XmlPullParser.END_TAG, null, "Code");
        return text;
    }

    public String readResult(StringReader stringReader) {
        String str = null;
        XmlPullParser newPullParser = Xml.newPullParser();
        try {
            newPullParser.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
            newPullParser.setInput(stringReader);
            newPullParser.nextTag();
            newPullParser.require(XmlPullParser.START_TAG, null, "Result");
            while (newPullParser.next() != XmlPullParser.END_TAG) {
                try {
                    if (newPullParser.getEventType() == XmlPullParser.START_TAG) {
                        str = readCode(newPullParser);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

}
