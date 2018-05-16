package nl.asymmetrics.droidshows.thetvdb.utils;

import nl.asymmetrics.droidshows.thetvdb.utils.XMLHandler;
import nl.asymmetrics.droidshows.utils.SQLiteStore;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.io.BufferedInputStream;

import android.util.Log;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class XMLParser {
        public List<String> parse(String urlstr) {
            try {
                    URL url = new URL(urlstr);

                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();

                    XMLReader xr = sp.getXMLReader();

                    XMLHandler handler = new XMLHandler();
                    xr.setContentHandler(handler);

                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(5000);
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = new BufferedInputStream(con.getInputStream());
                        InputSource inputSourceURL = new InputSource(inputStream);
                        xr.parse(inputSourceURL);
                    }
                    con.disconnect();

                    List<String> XMLData = handler.getParsedData();

                    return XMLData;
            } catch (Exception e) {
                    Log.e(SQLiteStore.TAG, "Error opening or parsing the URL");
                    return null;
            }
        }
}