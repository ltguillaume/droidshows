package nl.asymmetrics.droidshows.thetvdb.utils;

import nl.asymmetrics.droidshows.thetvdb.utils.XMLHandler;

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
		private String TAG = "DroidShows";
        public List<String> parse(String urlstr) {
            try {
                    URL url = new URL(urlstr);

                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    SAXParser sp = spf.newSAXParser();

                    XMLReader xr = sp.getXMLReader();

                    XMLHandler handler = new XMLHandler();
                    xr.setContentHandler(handler);

                    int retry = 0;
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    con.setReadTimeout(10000);
                    while(retry < 5) {
                        if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream inputStream = new BufferedInputStream(url.openStream());
                            InputSource inputSourceURL = new InputSource(inputStream);
                            xr.parse(inputSourceURL);
                            break;
                        }
                        else {
                            retry++;
                        }
                    }

                    List<String> XMLData = handler.getParsedData();

                    return XMLData;
            } catch (Exception e) {
                    Log.e(TAG, "Error opening or parsing the URL");
                    return null;
            }
        }
}