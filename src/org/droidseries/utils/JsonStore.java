package org.droidseries.utils;

import android.content.Context;
import android.util.Log;

import org.droidseries.thetvdb.model.Episode;
import org.droidseries.thetvdb.model.Serie;
import org.json.*;


import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


public class JsonStore {
	
	private String storagedb;
	
	private final String MY_DEBUG_TAG = "DroidSeries";
	
	public JsonStore(String storagedb) {
		this.storagedb = storagedb;
	}

	
    public JSONObject createJSONSerie(List<Serie> series) {
    	JSONObject jObjfinal = null;
    	JSONArray jArray = null;
    	
    	
		try {
			jObjfinal = new JSONObject();
			jArray = new JSONArray();
			
			for(int i=0;i<series.size();i++){
				JSONObject jObj = new JSONObject();
				jObj.put("id", series.get(i).getId());
				jObj.put("serieId", series.get(i).getSerieId());
				jObj.put("language", series.get(i).getLanguage()); 
				jObj.put("serieName", series.get(i).getSerieName());
				jObj.put("overview", series.get(i).getOverview());
				jObj.put("firstAired", series.get(i).getFirstAired());
				jObj.put("imdbId", series.get(i).getImdbId() );
				jObj.put("zap2ItId", series.get(i).getZap2ItId() );
				jObj.put("actors", ListToJSONArray(series.get(i).getActors()) );
				jObj.put("airsDayOfWeek", series.get(i).getAirsDayOfWeek() );
				jObj.put("airsTime", series.get(i).getAirsTime() );
				jObj.put("contentRating", series.get(i).getContentRating() );
				jObj.put("genres", ListToJSONArray(series.get(i).getGenres()) );
				jObj.put("network", series.get(i).getNetwork() );
				jObj.put("rating", series.get(i).getRating() );
				jObj.put("runtime", series.get(i).getRuntime() );
				jObj.put("status", series.get(i).getStatus() );
				jObj.put("fanart", series.get(i).getFanart() );
				jObj.put("lastUpdated", series.get(i).getLastUpdated() );
				jObj.put("poster", series.get(i).getPoster() );
				
				//adicionar os episodios
				//JSONArray de JSONObject de Episodios .toString() (?)
				jObj.put("episodes", createJSONSEpisode(series.get(i).getEpisodes()) );
				//jObj.put("nseasons", series.get(i).getNSeasons() );
				jObj.put("nseasons", ListToJSONArrayInt(series.get(i).getNSeasons()) );
				//jObj.put("unwatched", series.get(i).getUnwatched() );
				jObj.put("posterInCache", series.get(i).getPosterInCache() );
				
				jArray.put(jObj);
			}
			jObjfinal.put("series",jArray);
		} catch (Exception e) {
			//e.printStackTrace();
			Log.e(MY_DEBUG_TAG, e.getMessage());
		}
		
    	return jObjfinal;
    }

    public JSONArray createJSONSEpisode(List<Episode> episodes) {
    	JSONArray aJson = null;
    	
    	try {
    		aJson = new JSONArray();
    		
    		for (int i = 0; i < episodes.size(); i++) {
    			JSONObject jObj = new JSONObject();
    			jObj.put("id", episodes.get(i).getId() );
    			jObj.put("combinedEpisodeNumber", episodes.get(i).getCombinedEpisodeNumber() );
    			jObj.put("combinedSeason", episodes.get(i).getCombinedSeason() );
    			jObj.put("dvdChapter", episodes.get(i).getDvdChapter() );
    			jObj.put("dvdDiscId", episodes.get(i).getDvdDiscId() );
    			jObj.put("dvdEpisodeNumber", episodes.get(i).getDvdEpisodeNumber() );
    			jObj.put("dvdSeason", episodes.get(i).getDvdSeason() );
    			jObj.put("directors", ListToJSONArray(episodes.get(i).getDirectors()) );
    			jObj.put("epImgFlag", episodes.get(i).getEpImgFlag() );
    			jObj.put("episodeName", episodes.get(i).getEpisodeName() );
    			jObj.put("episodeNumber", episodes.get(i).getEpisodeNumber() );
    			jObj.put("firstAired", episodes.get(i).getFirstAired() );
    			jObj.put("guestStars", ListToJSONArray(episodes.get(i).getGuestStars()) );
    			jObj.put("imdbId", episodes.get(i).getImdbId() );
    			jObj.put("language", episodes.get(i).getLanguage() );
    			jObj.put("overview", episodes.get(i).getOverview() );
    			jObj.put("productionCode", episodes.get(i).getProductionCode() );
    			jObj.put("rating", episodes.get(i).getRating() );
    			jObj.put("seasonNumber", episodes.get(i).getSeasonNumber() );
    			jObj.put("writers", ListToJSONArray(episodes.get(i).getWriters()) );
    			jObj.put("absoluteNumber", episodes.get(i).getAbsoluteNumber() );
    			jObj.put("filename", episodes.get(i).getFilename() );
    			jObj.put("lastUpdated", episodes.get(i).getLastUpdated() );
    			jObj.put("seriesId", episodes.get(i).getSeriesId() );
    			jObj.put("seasonId", episodes.get(i).getSeasonId() );
    			jObj.put("seen", episodes.get(i).getSeen() );
    			
    			aJson.put(jObj);
    		}
    		
    	} catch (Exception e) {
			//e.printStackTrace();
    		Log.e(MY_DEBUG_TAG, e.getMessage());
		}
    	
    	return aJson;
    }
    
    public JSONArray ListToJSONArray(List<String> list) {
    	JSONArray jArray = null;
    	try {
    		jArray = new JSONArray();
    		for(int i=0; i < list.size(); i++){
    			jArray.put(list.get(i));
    		}
    	} catch (Exception e) {
    		//e.printStackTrace();
    		Log.e(MY_DEBUG_TAG, e.getMessage());
    	}
    	return jArray;
    }
    
    public JSONArray ListToJSONArrayInt(List<Integer> list) {
    	JSONArray jArray = null;
    	try {
    		jArray = new JSONArray();
    		for(int i=0; i < list.size(); i++){
    			jArray.put(list.get(i));
    		}
    	} catch (Exception e) {
    		//e.printStackTrace();
    		Log.e(MY_DEBUG_TAG, e.getMessage());
    	}
    	return jArray;
    }
    
    
    public void writeJSONFile(Context context, List<Serie> series){
    	JSONObject jObj = null; 
    	FileOutputStream fOut = null;
    	OutputStreamWriter osw = null;

    	try{
    		jObj = createJSONSerie(series);
    		fOut = context.openFileOutput(storagedb, Context.MODE_PRIVATE);
    		osw = new OutputStreamWriter(fOut);
    		osw.write(jObj.toString());
    		osw.flush();
    		Log.i(MY_DEBUG_TAG, "JSON Data saved.");
    	} catch (Exception e) {      
    		//e.printStackTrace();
    		Log.e(MY_DEBUG_TAG, "JSON Data not saved.");
    	}
    	finally {
    		try {
    			osw.close();
    			fOut.close();
            } catch (IOException e) {
            	//e.printStackTrace();
            }
    	}
    }
    
	public List<Serie> readJSONFile(Context context){
		InputStream isr = null;
		List<Serie> series = null;
	    	
	    	try{
	    		File jsonfile = new File(context.getFilesDir().getAbsolutePath() + "/" + storagedb);
	    		
	    		if (!jsonfile.exists()) {
	    		    series = new ArrayList<Serie>();
	    		}
	    		else {
		    		isr = new FileInputStream(jsonfile);
		    		
		    		long file_size = jsonfile.length();
		    		//Log.i("DroidSeries", "json file lenght: " + jsonfile.length());
		    		if(jsonfile.length() == 0) {
		    			series = new ArrayList<Serie>();
		    			return series;
		    		}
		    		
		    		byte[] bytes = new byte[(int)file_size];
		    		
		    		int offset = 0;
		            int numRead = 0;
		            while (offset < bytes.length
		                   && (numRead=isr.read(bytes, offset, bytes.length-offset)) >= 0) {
		                offset += numRead;
		            }
		            
		    		JSONObject json = new JSONObject(new String(bytes));
		    		
		    		series = convertJSONtoSeries(json.optJSONArray("series"));
	    		}
	    	} catch (Exception e) {      
	    		//e.printStackTrace();
	    		Log.e(MY_DEBUG_TAG, e.getMessage());
	    	}
	    	finally {
	    		try {
	    			isr.close();
	            } catch (IOException e) {
	            	//e.printStackTrace();
	            	Log.e(MY_DEBUG_TAG, e.getMessage());
	            }
	    	}
	    	
	    	return series;
	    }
    
    public List<Serie> convertJSONtoSeries(JSONArray jArray){
    	JSONObject jObj = null;
    	List<Serie> series = null;
    	
    	try {
    		series = new ArrayList<Serie>();
    		for(int i=0; i < jArray.length(); i++) {
    			jObj=jArray.optJSONObject(i);
    			series.add(convertJObjToSerie(jObj));
    		}
    		    		
    	} catch (Exception e) {      
    		//e.printStackTrace();
    		Log.e(MY_DEBUG_TAG, e.getMessage());
    	}
    	
    	return series;
    }   
    
   public Serie convertJObjToSerie(JSONObject jObj) {
	   Serie serie = null;
	   
	   try {
   			serie = new Serie();
   			serie.setId(jObj.optString("id"));
   			serie.setLanguage(jObj.optString("language")); 
   			serie.setSerieName(jObj.optString("serieName")); 
   			serie.setBanner(jObj.optString("banner"));
   			serie.setOverview(jObj.optString("overview"));
   			serie.setFirstAired(jObj.optString("firstAired"));
   		 	serie.setImdbId(jObj.optString("imdbId")); 
   		 	serie.setZap2ItId(jObj.optString("zap2ItId")); 
   		  	serie.setActors( JSONArrayToList(jObj.optJSONArray("actors")) );
   		 	serie.setAirsDayOfWeek(jObj.optString("airsDayOfWeek"));
   		  	serie.setAirsTime(jObj.optString("airsTime"));
   		  	serie.setContentRating(jObj.optString("contentRating"));
   		  	serie.setGenres( JSONArrayToList(jObj.optJSONArray("genres")) );
   		  	serie.setNetwork(jObj.optString("network"));
   		  	serie.setRating(jObj.optString("rating"));
   		  	serie.setRuntime(jObj.optString("runtime"));
   		  	serie.setStatus(jObj.optString("status"));
   		  	serie.setFanart(jObj.optString("fanart"));
   			serie.setLastUpdated(jObj.optString("lastUpdate"));
   		  	serie.setPoster(jObj.optString("poster"));
   		  	
   		  	//obter os episodios
   		    serie.setEpisodes( convertJSONtoEpisodes(jObj.optJSONArray("episodes")) );
   		    
   		    //serie.setNSeasons(Integer.parseInt(jObj.optString("nseasons")));
   		    serie.setNSeasons( JSONArrayToListInt(jObj.optJSONArray("nseasons")) );
   		 
   		    //serie.setUnwatched(Integer.parseInt(jObj.optString("unwatched")));
   		    serie.setPosterInCache(jObj.optString("posterInCache"));
   		} catch (Exception e) {
   			Log.e(MY_DEBUG_TAG, e.getMessage());
	   		//e.printStackTrace();
	   	}
   		return serie;
   }
   
   public List<Episode> convertJSONtoEpisodes(JSONArray jArray) {
	   List<Episode> episodes = null;
	   
	   try {
		   episodes = new ArrayList<Episode>();
		   
		   for(int i=0; i < jArray.length(); i++){
			   JSONObject jObj = jArray.getJSONObject(i);
			   
			   Episode episode = new Episode();
			   episode.setId(jObj.optString("id"));
			   episode.setCombinedEpisodeNumber(jObj.optString("combinedEpisodeNumber"));
			   episode.setCombinedSeason(jObj.optString("combinedSeason"));
			   episode.setDvdChapter(jObj.optString("dvdChapter"));
			   episode.setDvdDiscId(jObj.optString("dvdDiscId" ));
			   episode.setDvdEpisodeNumber(jObj.optString("dvdEpisodeNumber"));
			   episode.setDvdSeason(jObj.optString("DvdSeason" ));
			   episode.setDirectors( JSONArrayToList(jObj.optJSONArray("directors")) ); 
			   episode.setEpisodeName(jObj.optString("episodeName")); 
			   episode.setEpImgFlag(jObj.optString("epImgFlag"));
			   episode.setEpisodeNumber(Integer.parseInt(jObj.optString("episodeNumber" )));
			   episode.setFirstAired(jObj.optString("firstAired" ));
			   episode.setGuestStars( JSONArrayToList(jObj.optJSONArray("guestStars")) );
			   episode.setImdbId(jObj.optString("imdbId"));
			   episode.setLanguage(jObj.optString("language")); 
			   episode.setOverview(jObj.optString("overview"));
			   episode.setProductionCode(jObj.optString("productionCode")); 
			   episode.setRating(jObj.optString("rating"));
			   episode.setSeasonNumber(Integer.parseInt(jObj.optString("seasonNumber")));
			   episode.setWriters( JSONArrayToList(jObj.optJSONArray("writers")) );
			   episode.setAbsoluteNumber(jObj.optString("absoluteNumber"));
			   episode.setFilename(jObj.optString("filename"));
			   episode.setLastUpdated(jObj.optString("lastUpdated"));
			   episode.setSeriesId(jObj.optString("seriesId"));
			   episode.setSeasonId(jObj.optString("seasonsId"));
			   episode.setSeen(jObj.optBoolean("seen"));
			   
			   episodes.add(episode);
		   }
	   } catch (Exception e) {      
		   //e.printStackTrace();
		   Log.e(MY_DEBUG_TAG, e.getMessage());
	   }
	   return episodes;
   }
   
   public List<String> JSONArrayToList(JSONArray jArray){
	   List<String> list = null;
	   try {
		   list = new ArrayList<String>();
		   for(int i=0; i<jArray.length(); i++) {
			   list.add(jArray.getString(i));
		   }
		   
	   } catch (Exception e) {
		   //e.printStackTrace();
		   Log.e(MY_DEBUG_TAG, e.getMessage());
	   }
	   return list;
   }
   
   public List<Integer> JSONArrayToListInt(JSONArray jArray){
	   List<Integer> list = null;
	   try {
		   list = new ArrayList<Integer>();
		   for(int i=0; i<jArray.length(); i++) {
			   list.add(jArray.getInt(i));
		   }
		   
	   } catch (Exception e) {
		   //e.printStackTrace();
		   Log.e(MY_DEBUG_TAG, e.getMessage());
	   }
	   return list;
   }
}
