package org.droidseries.thetvdb;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.droidseries.thetvdb.model.*;
import org.droidseries.thetvdb.utils.*;



import android.text.TextUtils;
import android.util.Log;


public class TheTVDB {

    private String apiKey;
    private String xmlMirror;
    private String bannerMirror;
    //private String zipMirror;
    private final String TAG = "DroidSeries";   

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
    
    /*public Serie updateSerie(Serie serie) {
    	Serie updated_serie = null;
    	
        try {
        	//fazer update da serie
        	XMLParser xmlparser = new XMLParser();
        	List<String> XMLData = xmlparser.parse(xmlMirror + apiKey + "/series/" + serie.getId() + "/" + serie.getLanguage() + ".xml"); 
        	updated_serie = parseSeries(XMLData);
        	
        	//fazer update dos episodios
        	List<Episode> updated_episodes = new ArrayList<Episode>();
        	List<Episode> episodes = serie.getEpisodes();
        	for(int i = 0; i < episodes.size(); i++) {
        		Episode ep = getEpisode(episodes.get(i).getId(), episodes.get(i).getSeasonNumber(), episodes.get(i).getEpisodeNumber(), episodes.get(i).getLanguage());
        		ep.setSeen(episodes.get(i).getSeen());
        		updated_episodes.add( ep );
        	}
        	
        	updated_serie.setEpisodes(updated_episodes);
        	
        	updated_serie.setNSeasons(parseNSeasons(episodes));
        	
        	updated_serie.setUnwatched(parseUnwatchedEpisodes(episodes));
        
        } catch (Exception e) {
        	Log.e(MY_DEBUG_TAG, e.getMessage());
        }
        
        return updated_serie;
    }*/
    
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

    /*public Episode getDVDEpisode(String id, int seasonNbr, int episodeNbr, String language) {
        Episode episode = null;

        XMLEventReader xmlReader = null;
        try {
            xmlReader = XMLHelper.getEventReader(xmlMirror + apiKey + "/series/" + id + "/dvd/" + seasonNbr + "/" + episodeNbr + "/" + (language!=null?language+".xml":""));

            episode = parseNextEpisode(xmlReader);
        } catch (Exception error) {
            logger.warning("DVDEpisode error: " + error.getMessage());
        } finally {
            XMLHelper.closeEventReader(xmlReader);
        }

        return episode;
    }*/

    public String getSeasonYear(String id, int seasonNbr, String language) {
        String year = null;

        Episode episode = getEpisode(id, seasonNbr, 1, language);
        if (episode != null) {
            if (episode.getFirstAired() != null && !TextUtils.isEmpty(episode.getFirstAired()) ) {
                try {
                    Date date = dateFormat.parse(episode.getFirstAired());
                    if (date != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        year = ""+cal.get(Calendar.YEAR);
                    }
                } catch (Exception e) {
                	Log.e(TAG, e.getMessage());
                }
            }
        }

        return year;
    }

    /*@SuppressWarnings("unchecked")
	public Banners getBanners(String id) {
        Banners banners = new Banners();
        
        try {
        	XMLParser xmlparser = new XMLParser();
        	List<String> XMLData = xmlparser.parse(xmlMirror + apiKey + "/series/" + id + "/banners.xml");

        	Vector<Object> xml_banners = parseMultiple(XMLData, "<Banner>", "</Banner>");
        	
        	for (int i = 0; i < xml_banners.size(); i++) {
        		Banner banner = parseBanner( (List<String>)xml_banners.get(i) );
        		if (banner != null) {
        			banners.addBanner(banner);
                }
        	}
        } catch (Exception e) {
        	Log.e(MY_DEBUG_TAG, e.getMessage());
        }
        
        return banners;
    }*/
    
    /*@SuppressWarnings("unchecked")
	public List<Actor> getActors(String id) {
        List<Actor> results = new ArrayList<Actor>();
        
        try {
        	XMLParser xmlparser = new XMLParser();
        	List<String> XMLData = xmlparser.parse(xmlMirror + apiKey + "/series/" + id + "/actors.xml");
        	
        	Vector<Object> xml_actors = parseMultiple(XMLData, "<Actor>", "</Actor>");
        	
        	for (int i = 0; i < xml_actors.size(); i++) { 
        		Actor actor = parseActor( (List<String>)xml_actors.get(i) );
        		if (actor != null) {
        			results.add(actor);
                }
        	}
        } catch (Exception e) {
        	Log.e(MY_DEBUG_TAG, e.getMessage());
        } 
        
        Collections.sort(results);
        return results;
    }*/

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
        	
        	//cria um array de listas de strings com as series através de um Vector<Object>
        	//<Serie> e </Serie>
        	
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
    	//fazer download do zip em:
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
        				if(episode.getEpisodeName() != null) {
	        				if(!episode.getEpisodeName().equals("")) {
	        					episodes.add(episode);
	        				}
        				}
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
    
    /*
     * TODO: refactor the xml parse code
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
	        		series.setFirstAired(xmldata.get(i+1).trim());
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
    
    /*private Banner parseBanner(List<String> xmldata) {
    	Banner banner = null;
    	
    	boolean bannertag = false;
        for (int i = 0; i < xmldata.size(); i++) {
        	if (xmldata.get(i).contentEquals("<Banner>")) {
        		bannertag = true;
        		banner = new Banner();
        	}
        	if (bannertag) {
	        	if ( xmldata.get(i).contentEquals("<BannerPath>") && !xmldata.get(i+1).contentEquals("</BannerPath>") ) {
	        		String s = xmldata.get(i+1).trim();
                    if (!TextUtils.isEmpty(s)) {
                        banner.setUrl(bannerMirror + s);
                    }
	        	}
	        	else if ( xmldata.get(i).contentEquals("<VignettePath>") && !xmldata.get(i+1).contentEquals("</VignettePath>") ) {
	        		String s = xmldata.get(i+1).trim();
                    if (!TextUtils.isEmpty(s)) {
                    	banner.setVignette(bannerMirror + s);
                    }
	        	}
	        	else if ( xmldata.get(i).contentEquals("<ThumbnailPath>") && !xmldata.get(i+1).contentEquals("</ThumbnailPath>") ) {
	        		String s = xmldata.get(i+1).trim();
                    if (!TextUtils.isEmpty(s)) {
                    	banner.setThumb(bannerMirror + s);
                    }
	        	}
	        	else if ( xmldata.get(i).contentEquals("<BannerType>") && !xmldata.get(i+1).contentEquals("</BannerType>") ) {
	        		banner.setBannerType(xmldata.get(i+1).trim());
	        	}
	        	else if ( xmldata.get(i).contentEquals("<BannerType2>") && !xmldata.get(i+1).contentEquals("</BannerType2>") ) {
	        		banner.setBannerType2(xmldata.get(i+1).trim());
	        	}
	        	else if ( xmldata.get(i).contentEquals("<Language>") && !xmldata.get(i+1).contentEquals("</Language>") ) {
	        		banner.setLanguage(xmldata.get(i+1).trim());
	        	}
	        	else if ( xmldata.get(i).contentEquals("<Season>") && !xmldata.get(i+1).contentEquals("</Season>") ) {
	        		banner.setSeason(Integer.parseInt(xmldata.get(i+1).trim()));
	        	}
        	}
        	
        	if (xmldata.get(i).contentEquals("</Banner>")) {
        		break;
        	}
        }
        return banner;
    }*/
    
    /*private Actor parseActor(List<String> xmldata) {
        Actor actor = null;
        
        boolean actortag = false;
        for (int i = 0; i < xmldata.size(); i++) {
        	if (xmldata.get(i).contentEquals("<Actor>")) {
        		actortag = true;
        		actor = new Actor();
        	}
        	if (actortag) {
	        	if ( xmldata.get(i).contentEquals("<Image>") && !xmldata.get(i+1).contentEquals("</Image>") ) {
	        		String s = xmldata.get(i+1).trim();
                    if (!TextUtils.isEmpty(s)) {
                    	actor.setImage(bannerMirror + s);
                    }
	        	}
	        	else if ( xmldata.get(i).contentEquals("<Name>") && !xmldata.get(i+1).contentEquals("</Name>") ) {
	        		actor.setName(xmldata.get(i+1).trim());
	        	}
	        	else if ( xmldata.get(i).contentEquals("<Role>") && !xmldata.get(i+1).contentEquals("</Role>") ) {
	        		actor.setRole(xmldata.get(i+1).trim());
	        	}
	        	else if ( xmldata.get(i).contentEquals("<SortOrder>") && !xmldata.get(i+1).contentEquals("</SortOrder>") ) {
	        		actor.setSortOrder(Integer.parseInt(xmldata.get(i+1).trim()));
	        	}
        	}
        	
        	if (xmldata.get(i).contentEquals("</Actor>")) {
        		break;
        	}
        }
        return actor;
    }*/
    
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
	        		episode.setFirstAired(xmldata.get(i+1).trim());
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
	        		String tmpOverview = xmldata.get(i+1).trim();
	        		int count = 1;
	        		
	        		while(!xmldata.get(i+count).contentEquals("</Overview>")) {
	        			count++;
	        			if(!xmldata.get(i+count).contentEquals("</Overview>")) {
	        				tmpOverview += xmldata.get(i+count).trim();
	        			}
	        		}

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
