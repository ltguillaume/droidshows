package nl.asymmetrics.droidshows.thetvdb;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import nl.asymmetrics.droidshows.thetvdb.model.*;
import nl.asymmetrics.droidshows.thetvdb.utils.*;
import android.text.TextUtils;
import android.util.Log;

public class TheTVDB {
	private String TAG = "DroidShows";
    private String apiKey;
    private String xmlMirror;
    private String bannerMirror;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public TheTVDB(String apiKey) {
        Mirrors mirrors = new Mirrors(apiKey);
        xmlMirror = mirrors.getMirror(Mirrors.TYPE_XML) + "/api/";
        bannerMirror = mirrors.getMirror(Mirrors.TYPE_BANNER) + "/banners/";
        //zipMirror = mirrors.getMirror(Mirrors.TYPE_ZIP);

        this.apiKey = apiKey;
    }

    public String getMirror() {
        return xmlMirror;
    }

    public Episode getEpisode(String id, int seasonNbr, int episodeNbr, String language) {
        Episode episode = null;

        try {
            XMLParser xmlparser = new XMLParser();
            String urlToXML = xmlMirror + apiKey + "/series/" + id + "/default/" + seasonNbr + "/" + episodeNbr + "/" + (language!=null?language+".xml":"");
            List<String> XMLData = xmlparser.parse(urlToXML);

            episode = parseEpisode(XMLData);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return episode;
    }

    public String getSeasonYear(String id, int seasonNbr, String language) {
        String year = null;

        Episode episode = getEpisode(id, seasonNbr, 1, language);
        if (episode != null) {
            if (episode.getFirstAired() != null && !TextUtils.isEmpty(episode.getFirstAired()) ) {
                try {
                    Date date = dateFormat.parse(episode.getFirstAired());
                    if (date != null) {
                        year = date.getYear()+1900 +"";
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        return year;
    }

    @SuppressWarnings("unchecked")
    public List<Serie> searchSeries(String title, String language) {
        List<Serie> results = new ArrayList<Serie>();

        try {
            XMLParser xmlparser = new XMLParser();
            String urlToXML = xmlMirror + "GetSeries.php?seriesname=" + URLEncoder.encode(title, "UTF-8") + (language!=null?"&language="+language:"");
            List<String> XMLData = xmlparser.parse(urlToXML);
            if (XMLData == null) {
                return null;
            }

            Vector<Object> xml_series = parseMultiple(XMLData, "<Series>", "</Series>");

            for (int i = 0; i < xml_series.size(); i++) {
                Serie series = parseSeries( (List<String>)xml_series.get(i) );
                if (series != null) {
                    results.add(series);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    public Serie getSerie(String id, String language) {
        Serie serie = null;
        //<mirrorpath_zip>/api/<apikey>/series/<seriesid>/all/<language>.zip
        //http://thetvdb.com/api/8AC675886350B3C3/series/79349/all/en.xml

        try {
            XMLParser xmlparser = new XMLParser();
            String urlToXML = xmlMirror + apiKey + "/series/" + id + "/all/" + (language!=null?language+".xml":"");
            List<String> XMLData = xmlparser.parse(urlToXML);
            if (XMLData == null) {
                return null;
            }

            serie = parseSeries(XMLData);

            Vector<Object> xml_episodes = parseMultiple(XMLData, "<Episode>", "</Episode>");

            List<Episode> episodes = new ArrayList<Episode>();
            for (int i = 0; i < xml_episodes.size(); i++) {
                try{
                    Episode episode = parseEpisode( (List<String>)xml_episodes.get(i) );
                    if (episode != null) {
                        if(episode.getEpisodeName() == null || episode.getEpisodeName().equals(""))
                        	episode.setEpisodeName(" ");
                        episodes.add(episode);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error gathering the episodes info from the XML file");
                }
            }

            serie.setEpisodes(episodes);

            //gets the season numbers
            serie.setNSeasons(parseNSeasons(episodes));
        } catch (Exception e) {
        Log.e(TAG, "Error gathering the TV show info from the XML file");
        }

        return serie;
    }

    /*
     * Private functions that help do stuff
     */

    private Serie parseSeries(List<String> xmldata) {
        Serie series = null;

        boolean seriestag = false;
        for (int i = 0; i < xmldata.size(); i++) {
                if (xmldata.get(i).contentEquals("<Series>")) {
                        seriestag = true;
                        series = new Serie();
                }
                if (seriestag) {
                        if ( xmldata.get(i).contentEquals("<SeriesID>") && !xmldata.get(i+1).contentEquals("</SeriesID>")) {
                                series.setSerieId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Language>") && !xmldata.get(i+1).contentEquals("</Language>") ) {
                                series.setLanguage(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<SeriesName>") && !xmldata.get(i+1).contentEquals("</SeriesName>") ) {
                                String tmpSName = xmldata.get(i+1).trim();
                                int count = 1;

                                while(!xmldata.get(i+count).contentEquals("</SeriesName>")) {
                                        count++;
                                        if(!xmldata.get(i+count).contentEquals("</SeriesName>")) {
                                                tmpSName += xmldata.get(i+count).trim();
                                        }
                                }

                                series.setSerieName(tmpSName);
                        }
                        else if ( xmldata.get(i).contentEquals("<banner>") && !xmldata.get(i+1).contentEquals("</banner>") ) {
                                String s = xmldata.get(i+1).trim();
                                if (!TextUtils.isEmpty(s)) {
                                        series.setBanner(bannerMirror + s);
                                }
                        }
                        else if ( xmldata.get(i).contentEquals("<Overview>") && !xmldata.get(i+1).contentEquals("</Overview>") ) {
                                String tmpOverview = xmldata.get(i+1).trim();
                                int count = 1;

                                while(!xmldata.get(i+count).contentEquals("</Overview>")) {
                                        count++;
                                        if(!xmldata.get(i+count).contentEquals("</Overview>")) {
                                                tmpOverview += xmldata.get(i+count).trim();
                                        }
                                }

                                series.setOverview( tmpOverview );
                        }
                        else if ( xmldata.get(i).contentEquals("<FirstAired>") && !xmldata.get(i+1).contentEquals("</FirstAired>") ) {
                        	String tmpFirstAired = "";
                        	do {
                        		i++;
                        		tmpFirstAired += xmldata.get(i).trim();
                        	} while(!xmldata.get(i+1).contentEquals("</FirstAired>"));
                            series.setFirstAired(tmpFirstAired);
                        }
                        else if ( xmldata.get(i).contentEquals("<IMDB_ID>") && !xmldata.get(i+1).contentEquals("</IMDB_ID>") ) {
                                series.setImdbId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<zap2it_id>") && !xmldata.get(i+1).contentEquals("</zap2it_id>") ) {
                                series.setZap2ItId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<id>") && !xmldata.get(i+1).contentEquals("</id>") ) {
                                series.setId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Actors>") && !xmldata.get(i+1).contentEquals("</Actors>") ) {
                                series.setActors( parseList(xmldata.get(i+1).trim(), "|,") );
                        }
                        else if ( xmldata.get(i).contentEquals("<Airs_DayOfWeek>") && !xmldata.get(i+1).contentEquals("</Airs_DayOfWeek>") ) {
                                series.setAirsDayOfWeek(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Airs_Time>") && !xmldata.get(i+1).contentEquals("</Airs_Time>") ) {
                                series.setAirsTime(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<ContentRating>") && !xmldata.get(i+1).contentEquals("</ContentRating>") ) {
                                series.setContentRating(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Genre>") && !xmldata.get(i+1).contentEquals("</Genre>") ) {
                                series.setGenres( parseList(xmldata.get(i+1).trim(), "|,") );
                        }
                        else if ( xmldata.get(i).contentEquals("<Network>") && !xmldata.get(i+1).contentEquals("</Network>") ) {
                                series.setNetwork(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Rating>") && !xmldata.get(i+1).contentEquals("</Rating>") ) {
                                series.setRating(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Runtime>") && !xmldata.get(i+1).contentEquals("</Runtime>") ) {
                                series.setRuntime(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Status>") && !xmldata.get(i+1).contentEquals("</Status>") ) {
                                series.setStatus(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<fanart>") && !xmldata.get(i+1).contentEquals("</fanart>") ) {
                                String s = xmldata.get(i+1).trim();
                                if (!TextUtils.isEmpty(s)) {
                                    series.setFanart(bannerMirror + s);
                                }
                        }
                        else if ( xmldata.get(i).contentEquals("<lastupdated>") && !xmldata.get(i+1).contentEquals("</lastupdated>") ) {
                                series.setLastUpdated(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<poster>") && !xmldata.get(i+1).contentEquals("</poster>") ) {
                                String s = xmldata.get(i+1).trim();
                                if (!TextUtils.isEmpty(s)) {
                                    series.setPoster(bannerMirror + s);
                                }
                        }
                }

                if (xmldata.get(i).contentEquals("</Series>")) {
                        break;
                }
        }
        return series;
    }

   private Episode parseEpisode(List<String> xmldata) {
        Episode episode = null;

        boolean episodetag = false;
        for (int i = 0; i < xmldata.size(); i++) {
                if (xmldata.get(i).contentEquals("<Episode>")) {
                        episodetag = true;
                        episode = new Episode();
                }

                if (episodetag) {
                        if ( xmldata.get(i).contentEquals("<id>") && !xmldata.get(i+1).contentEquals("</id>") ) {
                                episode.setId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Combined_episodenumber>") && !xmldata.get(i+1).contentEquals("</Combined_episodenumber") ) {
                                episode.setCombinedEpisodeNumber(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Combined_season>") && !xmldata.get(i+1).contentEquals("</Combined_season>") ) {
                                episode.setCombinedSeason(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<DVD_chapter>") && !xmldata.get(i+1).contentEquals("</DVD_chapter>") ) {
                                episode.setDvdChapter(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<DVD_discid>") && !xmldata.get(i+1).contentEquals("</DVD_discid>") ) {
                                episode.setDvdDiscId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<DVD_episodenumber>") && !xmldata.get(i+1).contentEquals("</DVD_episodenumber>") ) {
                                episode.setDvdEpisodeNumber(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<DVD_season>") && !xmldata.get(i+1).contentEquals("</DVD_season>") ) {
                                episode.setDvdSeason(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Director>") && !xmldata.get(i+1).contentEquals("</Director>") ) {
                                episode.setDirectors( parseList(xmldata.get(i+1).trim(), "|,") );
                        }
                        else if ( xmldata.get(i).contentEquals("<EpImgFlag>") && !xmldata.get(i+1).contentEquals("</EpImgFlag>") ) {
                                episode.setEpImgFlag(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<EpisodeName>") && !xmldata.get(i+1).contentEquals("</EpisodeName>") ) {
                                episode.setEpisodeName(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<EpisodeNumber>") && !xmldata.get(i+1).contentEquals("</EpisodeNumber>") ) {
                                episode.setEpisodeNumber(Integer.parseInt(xmldata.get(i+1).trim()));
                        }
                        else if ( xmldata.get(i).contentEquals("<FirstAired>") && !xmldata.get(i+1).contentEquals("</FirstAired>") ) {
                        	String tmpFirstAired = "";
                        	do {
                        		i++;
                        		tmpFirstAired += xmldata.get(i).trim();
                        	} while (!xmldata.get(i+1).contentEquals("</FirstAired>"));
                            episode.setFirstAired(tmpFirstAired);
                        }
                        else if ( xmldata.get(i).contentEquals("<GuestStars>") && !xmldata.get(i+1).contentEquals("</GuestStars>") ) {
                                episode.setGuestStars( parseList(xmldata.get(i+1).trim(), "|,") );
                        }
                        else if ( xmldata.get(i).contentEquals("<IMDB_ID>") && !xmldata.get(i+1).contentEquals("</IMDB_ID>") ) {
                                episode.setImdbId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Language>") && !xmldata.get(i+1).contentEquals("</Language>") ) {
                                episode.setLanguage(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Overview>") && !xmldata.get(i+1).contentEquals("</Overview>") ) {
                        	String tmpOverview = "";
                        	do {
                        		i++;
                        		tmpOverview += xmldata.get(i).trim();
                        	} while (!xmldata.get(i+1).contentEquals("</Overview>"));
                            episode.setOverview( tmpOverview );
                        }
                        else if ( xmldata.get(i).contentEquals("<ProductionCode>") && !xmldata.get(i+1).contentEquals("</ProductionCode>") ) {
                                episode.setProductionCode(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<Rating>") && !xmldata.get(i+1).contentEquals("</Rating>") ) {
                                episode.setRating(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<SeasonNumber>") && !xmldata.get(i+1).contentEquals("</SeasonNumber>") ) {
                                episode.setSeasonNumber(Integer.parseInt(xmldata.get(i+1).trim()));
                        }
                        else if ( xmldata.get(i).contentEquals("<Writer>") && !xmldata.get(i+1).contentEquals("</Writer>") ) {
                                episode.setWriters( parseList(xmldata.get(i+1).trim(), "|,") );
                        }
                        else if ( xmldata.get(i).contentEquals("<absolute_number>") && !xmldata.get(i+1).contentEquals("</absolute_number>") ) {
                                episode.setAbsoluteNumber(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<filename>") && !xmldata.get(i+1).contentEquals("</filename>") ) {
                                String s = xmldata.get(i+1).trim();
                                if (!TextUtils.isEmpty(s)) {
                                    episode.setFilename(bannerMirror + s);
                                }
                        }
                        else if ( xmldata.get(i).contentEquals("<lastupdated>") && !xmldata.get(i+1).contentEquals("</lastupdated>") ) {
                                episode.setLastUpdated(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<seasonid>") && !xmldata.get(i+1).contentEquals("</seasonid>") ) {
                                episode.setSeasonId(xmldata.get(i+1).trim());
                        }
                        else if ( xmldata.get(i).contentEquals("<seriesid>") && !xmldata.get(i+1).contentEquals("</seriesid>") ) {
                                episode.setSeriesId(xmldata.get(i+1).trim());
                        }
                }

                if (xmldata.get(i).contentEquals("</Episode>")) {
                        break;
                }
        }
        return episode;
    }

    private List<String> parseList(String input, String delim) {
        List<String> result = new ArrayList<String>();

        StringTokenizer st = new StringTokenizer(input, delim);
        while (st.hasMoreTokens()) {
            String token = st.nextToken().trim();
            if (token.length() > 0) {
                result.add(token);
            }
        }

        return result;
    }

    private Vector<Object> parseMultiple(List<String> xmldata, String tagstart, String tagend) {
        int count = 0;

        for (int i = 0; i < xmldata.size(); i++) {
            if (xmldata.get(i).contentEquals(tagstart)) {
                count++;
            }
        }

        Vector<Object> xml_series = new Vector<Object>(count);
        List<String> series = new ArrayList<String>();
        boolean seriestag = false;

        for (int j = 0; j < xmldata.size(); j++) {
            if (xmldata.get(j).contentEquals(tagstart)) {
                series = new ArrayList<String>();
                seriestag = true;
            }

            if (seriestag) {
                series.add(xmldata.get(j).trim());
            }

            if (xmldata.get(j).contentEquals(tagend)) {
                seriestag = false;
                xml_series.add(series);
            }
        }

        return xml_series;
    }

    private List<Integer> parseNSeasons(List<Episode> episodes) {

        ArrayList<Integer> nseasons = new ArrayList<Integer>();

        for(int i = 0; i < episodes.size(); i++){
            int n = episodes.get(i).getSeasonNumber();
            if( !nseasons.contains( n ) ) {
                nseasons.add( n );
            }
        }

        return nseasons;
    }
}