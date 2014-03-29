package org.droidseries.thetvdb.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.droidseries.thetvdb.utils.*;
//import java.net.URL;

/**
 * TODO: review this model
 */
public class Mirrors {

    public static String TYPE_XML = "XML";
    public static String TYPE_BANNER = "BANNER";
    public static String TYPE_ZIP = "ZIP";

    private static final Random RNDM = new Random();

    private List<String> xmlList = new ArrayList<String>();
    private List<String> bannerList = new ArrayList<String>();
    private List<String> zipList = new ArrayList<String>();

    public Mirrors(String apiKey) {
        try {
            String url = "http://www.thetvdb.com/api/" + apiKey + "/mirrors.xml";

            XMLParser xmlparser = new XMLParser();
            List<String> XMLData = xmlparser.parse(url);

            /* variaveis auxiliares para popular os mirrors */
            String mirror = "";
            int typeMask = 0;
            boolean mirrortag = false;

            /* ciclo que percorre a List que contem o XML resultante do parse */
            for (int i = 0; i < XMLData.size(); i++) {
                if (XMLData.get(i).contentEquals("<Mirror>")) {
                    mirrortag = true;
                    mirror = "";
                    typeMask = 0;
                }
                if (mirrortag) {
                    if (XMLData.get(i).contentEquals("<mirrorpath>")) {
                        mirror = XMLData.get(i+1).trim();
                        typeMask = Integer.parseInt(XMLData.get(i+4).trim());
                        addMirror(typeMask, mirror);
                    }
                }
                else if (XMLData.get(i).contentEquals("</Mirror>"))
                {
                    mirrortag = false;
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: TheTVDB API -> " + e.getMessage());
        }
    }

    public String getMirror(String type) {
        String url = null;
        if (type.equals(TYPE_XML) && !xmlList.isEmpty()) {
            url = xmlList.get(RNDM.nextInt(xmlList.size()));
        } else if (type.equals(TYPE_BANNER) && !bannerList.isEmpty()) {
            url = bannerList.get(RNDM.nextInt(bannerList.size()));
        } else if (type.equals(TYPE_ZIP) && !zipList.isEmpty()) {
            url = zipList.get(RNDM.nextInt(zipList.size()));
        }
        return url;
    }

    private void addMirror(int typeMask, String url) {
        switch (typeMask) {
            case 1: xmlList.add(url);
                    break;
            case 2: bannerList.add(url);
                    break;
            case 3: xmlList.add(url);
                    bannerList.add(url);
                    break;
            case 4: zipList.add(url);
                    break;
            case 5: xmlList.add(url);
                    zipList.add(url);
                    break;
            case 6: bannerList.add(url);
                    zipList.add(url);
                    break;
            case 7: xmlList.add(url);
                    bannerList.add(url);
                    zipList.add(url);
                    break;
        }
    }
}
