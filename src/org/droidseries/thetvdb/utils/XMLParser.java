package org.droidseries.thetvdb.utils;

import org.droidseries.thetvdb.utils.XMLHandler;
import org.xml.sax.InputSource; 
import org.xml.sax.XMLReader;


import android.util.Log;

import javax.xml.parsers.SAXParser; 
import javax.xml.parsers.SAXParserFactory; 

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class XMLParser {
	
	private final String MY_DEBUG_TAG = "DroidSeries";
	
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
            con.setRequestMethod("HEAD");
            while(retry < 2) {
            	if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            		InputSource inputSourceURL =  new InputSource(url.openStream());
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
			Log.e(MY_DEBUG_TAG, "Error opening or parsing the URL");
			return null;
		}
	}
}