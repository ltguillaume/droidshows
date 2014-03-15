package org.droidseries.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.droidseries.droidseries;
import org.droidseries.thetvdb.model.Episode;
import org.droidseries.thetvdb.model.Serie;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.database.SQLException;
import android.text.TextUtils;
import android.util.Log;

public class SQLiteStore extends SQLiteOpenHelper {
	//TODO: change the hardcoded path
	private static String DB_PATH = "/data/data/org.droidseries/databases/";
	private static String DB_NAME = "droidseries.db";
	private SQLiteDatabase db;

	private final Context ctx;
	
	private final String MY_DEBUG_TAG = "DroidSeries";
	
	public SQLiteStore(Context context) {
    	super(context, DB_NAME, null, 1);
        this.ctx = context;
    }
	
	public void createDataBase() throws IOException {
		 
    	boolean dbExist = checkDataBase();
 
    	if(!dbExist){
        	//this.getReadableDatabase();
    		this.getWritableDatabase();
        	
        	//try {
    		//	copyDataBase();
    		//} catch (IOException e) {
        	//	throw new Error("Error copying database");
        	//}
    	}
    }
	
	private boolean checkDataBase(){ 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	} catch(SQLiteException e){
    		//database does't exist yet.
    	}
    	
    	if(checkDB != null){
    		checkDB.close();
    	}
 
    	return checkDB != null ? true : false;
    }
	
	private void copyDataBase() throws IOException{
    	//Open your local db as the input stream
    	InputStream myInput = ctx.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
	
	public void openDataBase() throws SQLException{
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
	
	/*
	 * Insert Methods
	 */
	
	public boolean execQuery(String query) {
		try{
			db.execSQL(query);
		} catch(SQLiteException e){
			return false;
		}
		return true;
	}
	
	public Cursor Query(String query) {
		Cursor c = null;
		try{
			c = db.rawQuery(query, null);			
		} catch(SQLiteException e){
			return null;
		}
		return c;
	}
	
	/*
	 * Get Methods
	 */
	public List<Episode> getEpisodes(String serieId) {
		List<Episode> episodes = null;
		
		try {
			episodes = new ArrayList<Episode>();
			
			Cursor c = Query("SELECT * FROM episodes WHERE serieId = '" + serieId + "'");
			
		    if (c != null) {
		    	int idCol = c.getColumnIndex("id");
			    int combinedEpisodeNumberCol = c.getColumnIndex("combinedEpisodeNumber");
			    int combinedSeasonCol = c.getColumnIndex("combinedSeason");
			    int dvdChapterCol = c.getColumnIndex("dvdChapter");
			    int dvdDiscIdCol = c.getColumnIndex("dvdDiscId");
			    int dvdEpisodeNumberCol = c.getColumnIndex("dvdEpisodeNumber");
			    int dvdSeasonCol = c.getColumnIndex("dvdSeason");
			    int epImgFlagCol = c.getColumnIndex("epImgFlag");
			    int episodeNameCol = c.getColumnIndex("episodeName");
			    int episodeNumberCol = c.getColumnIndex("episodeNumber");
			    int firstAiredCol = c.getColumnIndex("firstAired");
			    int imdbIdCol = c.getColumnIndex("imdbId");
			    int languageCol = c.getColumnIndex("language");
			    int overviewCol = c.getColumnIndex("overview");
			    int productionCodeCol = c.getColumnIndex("productionCode");
			    int ratingCol = c.getColumnIndex("rating");
			    int seasonNumberCol = c.getColumnIndex("seasonNumber");    
			    int absoluteNumberCol = c.getColumnIndex("absoluteNumber");
			    int filenameCol = c.getColumnIndex("filename");
			    int lastUpdatedCol = c.getColumnIndex("lastUpdated");
			    int seriesIdCol = c.getColumnIndex("serieId");
			    int seasonIdCol = c.getColumnIndex("seasonId");
			    int seenCol = c.getColumnIndex("seen");
			    c.moveToFirst();
			    
			    if (c.isFirst()) {
			    	do {
			    		Episode eTmp = new Episode();
			    		
			    		List<String> directors = new ArrayList<String>();
			    		Cursor cdirectors = Query("SELECT director FROM directors WHERE serieId='" + serieId + "' AND episodeId='" + c.getString(idCol) + "'");
			    		cdirectors.moveToFirst();
			    		int directorCol = cdirectors.getColumnIndex("director");
			    		if(cdirectors != null && cdirectors.isFirst()) {
			    			do {
			    				directors.add(cdirectors.getString(directorCol));
			    			} while ( cdirectors.moveToNext() );
			    		}
			    		cdirectors.close();
			    		
			    		List<String> guestStars = new ArrayList<String>();
			    		Cursor cguestStars = Query("SELECT guestStar FROM guestStars WHERE serieId='" + serieId + "' AND episodeId='" + c.getString(idCol) + "'");
			    		cguestStars.moveToFirst();
			    		int guestStarCol = cguestStars.getColumnIndex("guestStar");
			    		if(cguestStars != null && cguestStars.isFirst()) {
			    			do {
			    				guestStars.add(cguestStars.getString(guestStarCol));
			    			} while ( cguestStars.moveToNext() );
			    		}
			    		cguestStars.close();
			    		
			    		List<String> writers = new ArrayList<String>();
			    		Cursor cwriters = Query("SELECT writer FROM writers WHERE serieId='" + serieId + "' AND episodeId='" + c.getString(idCol) + "'");
			    		cwriters.moveToFirst();
			    		int writersCol = cwriters.getColumnIndex("writer");
			    		if(cwriters != null && cwriters.isFirst()) {
			    			do {
			    				writers.add(cwriters.getString(writersCol));
			    			} while ( cwriters.moveToNext() );
			    		}
			    		cwriters.close();
			    		
			    		eTmp.setDirectors(directors);
			    		eTmp.setGuestStars(guestStars);
			    		eTmp.setWriters(writers);
			    		
			    		eTmp.setId(c.getString(idCol));
			    		eTmp.setCombinedEpisodeNumber(c.getString(combinedEpisodeNumberCol));
			    		eTmp.setCombinedSeason(c.getString(combinedSeasonCol));
			    		eTmp.setDvdChapter(c.getString(dvdChapterCol));
			    		eTmp.setDvdDiscId(c.getString(dvdDiscIdCol));
			    		eTmp.setDvdEpisodeNumber(c.getString(dvdEpisodeNumberCol));
			    		eTmp.setDvdSeason(c.getString(dvdSeasonCol));
			    		eTmp.setEpImgFlag(c.getString(epImgFlagCol));
			    		eTmp.setEpisodeName(c.getString(episodeNameCol));
			    		eTmp.setEpisodeNumber(c.getInt(episodeNumberCol));
			    		eTmp.setFirstAired(c.getString(firstAiredCol));
			    		eTmp.setImdbId(c.getString(imdbIdCol));
			    		eTmp.setLanguage(c.getString(languageCol));
			    		eTmp.setOverview(c.getString(overviewCol));
			    		eTmp.setProductionCode(c.getString(productionCodeCol));
			    		eTmp.setRating(c.getString(ratingCol));
			    		eTmp.setSeasonNumber(c.getInt(seasonNumberCol));
			    		eTmp.setAbsoluteNumber(c.getString(absoluteNumberCol));
			    		eTmp.setFilename(c.getString(filenameCol));
			    		eTmp.setLastUpdated(c.getString(lastUpdatedCol));
			    		eTmp.setSeriesId(c.getString(seriesIdCol));
			    		eTmp.setSeasonId(c.getString(seasonIdCol));
			    		
			    		if (c.getInt(seenCol) == 0) {
			    			eTmp.setSeen(false);
			    		}
			    		else {
			    			eTmp.setSeen(true);
			    		}
			    		
			    		episodes.add(eTmp);
			        } while ( c.moveToNext() );
			    }
		    }
		    c.close();
		} catch(SQLiteException e){
			//nao faz nada
		}
		return episodes;
	}
	
	private List<String> getSeries(String orderBy) {
		List<String> series = new ArrayList<String>();
		try {
			Cursor cseries = Query("SELECT id FROM series ORDER BY " + orderBy + " ASC");
			cseries.moveToFirst();
			if (cseries != null && cseries.isFirst()) {
				do {
					series.add(cseries.getString(0));
				} while ( cseries.moveToNext() );
			}
			
			cseries.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return series;
	}
	
	public List<String> getSeries() {
		return getSeriesByName();
	}
	
	public List<String> getSeriesByName() {
		return getSeries("serieName");
	}
	
	public List<String> getSeriesByNextEpisode() {
		List<String> series = new ArrayList<String>();
		try {
			// Note: completely seen series won't be returned by this query
			Cursor cseries = Query("SELECT s.id, min(e.firstAired) AS nextAir FROM series AS s LEFT JOIN episodes AS e ON e.serieId = s.id WHERE e.seen = 0 AND seasonNumber <> 0 GROUP BY s.id ORDER BY nextAir ASC");
			cseries.moveToFirst();
			if (cseries != null && cseries.isFirst()) {
				do {
					series.add(cseries.getString(0));
					Log.d("DroidSeries", "Adding serie [" + cseries.getString(0) + "] next air [" + cseries.getShort(1) + "]");
				} while ( cseries.moveToNext() );
			}
			
			cseries.close();
			// This completes the method by adding the completely seen series
			StringBuilder sb = new StringBuilder();
			for (String sid : series) {
				sb.append("'").append(sid).append("',");
			}
			// remove the extra comma
			sb.delete(sb.length() - 1, sb.length());
			cseries = Query("SELECT s.id FROM series AS s WHERE s.id NOT IN (" + sb.toString() + ") ORDER BY serieName ASC");
			cseries.moveToFirst();
			if (cseries != null && cseries.isFirst()) {
				do {
					series.add(cseries.getString(0));
					Log.d("DroidSeries", "Adding completely seen serie [" + cseries.getString(0) + "]");
				} while ( cseries.moveToNext() );
			}
			
			cseries.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return series;
	}
	
	public List<String> getEpisodes(String serieId, int seasonNumber) {
		List<String> episodes = new ArrayList<String>();
		try {
			Cursor c = Query("SELECT id FROM episodes WHERE serieId='" + serieId + "' AND seasonNumber=" + seasonNumber + " ORDER BY episodeNumber ASC");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				do {
					episodes.add(c.getString(0));
				} while ( c.moveToNext() );
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return episodes;
	}
	
	public String getSerieName(String serieId) {
		String sname = "";
		try {
			Cursor c = Query("SELECT serieName FROM series WHERE id='" + serieId  + "'");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				sname = c.getString(0);
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return sname;
	}

	public Integer getSerieStatus(String serieId) {
		Integer sstatus = 0;
		Cursor c = Query("SELECT passiveStatus FROM series WHERE id='" + serieId  + "'");
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				sstatus = c.getInt(0);
			}
			c.close();
		} catch(SQLiteException e){
			c.close();
			Log.e("DroidSeries", e.getMessage());
			return null;
		}
		return sstatus;
	}

	public String getSeriePoster(String serieId) {
		String sposter = "";
		try {
			Cursor c = Query("SELECT posterInCache FROM series WHERE id='" + serieId  + "'");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				sposter = c.getString(0);
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return sposter;
	}
	
	public int getEPUnwatched(String serieId) {
		int unwatched = 0;
		try {
			Cursor c = Query("SELECT count(id) FROM episodes WHERE serieId='" + serieId + "' AND seen=0 AND seasonNumber <> 0");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				unwatched = c.getInt(0);
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return unwatched;
	}
	
	public int getSeasonEPUnwatched(String serieId, int snumber) {
		int unwatched = 0;
		try {
			Cursor c = Query("SELECT count(id) FROM episodes WHERE serieId='" + serieId + "' AND seasonNumber=" + snumber + " AND seen=0");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				unwatched = c.getInt(0);
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return unwatched;
	}
	
	public Date getNextAir(String serieId, int snumber) {
		Date na = null;
		Cursor c = null;
		try {
			if(snumber == -1) {
				c = Query("SELECT firstAired FROM episodes WHERE serieId='" + serieId + "' and seen=0 and seasonNumber <> 0 ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			}
			else {
				c = Query("SELECT firstAired FROM episodes WHERE serieId='" + serieId + "' and seen=0 and seasonNumber=" + snumber + " ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			}
			
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				
				int faCol = c.getColumnIndex("firstAired");
				
				if(!c.getString(faCol).equals("")) {
    				try {
    					SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    					na = SDF.parse(c.getString(faCol));
					} catch (ParseException e) {
						Log.e(MY_DEBUG_TAG, e.getMessage());
					}
				}
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return na;
	}
	
	public String getNextEpisodeId(String serieId, int snumber) {
		int id=-1;
		Cursor c = null;
		try {
			if(snumber == -1) {
				c = Query("SELECT id FROM episodes WHERE serieId='" + serieId + "' and seen=0 and seasonNumber <> 0 ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			}
			else {
				c = Query("SELECT id FROM episodes WHERE serieId='" + serieId + "' and seen=0 and seasonNumber=" + snumber + " ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			}
			
			c.moveToFirst();
			if(c!=null && c.isFirst()){
				int index = c.getColumnIndex("id");
				id = c.getInt(index);
			}
		}catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		
		return ""+id;
	}
	
	public String getNextEpisode(String serieId, int snumber) {
		String ne = "";
		Cursor c = null;
		try {
			if(snumber == -1) {
				c = Query("SELECT firstAired, episodeNumber, seasonNumber FROM episodes WHERE serieId='" + serieId + "' and seen=0 and seasonNumber <> 0 ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			}
			else {
				c = Query("SELECT firstAired, episodeNumber, seasonNumber FROM episodes WHERE serieId='" + serieId + "' and seen=0 and seasonNumber=" + snumber + " ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			}
			
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				
				int faCol = c.getColumnIndex("firstAired");
				int enCol = c.getColumnIndex("episodeNumber");
				int snCol = c.getColumnIndex("seasonNumber");
				
				String epDataStr = ""; 
				if(!c.getString(faCol).equals("")) {
    				try {
    					SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
    					Date epDate = SDF.parse(c.getString(faCol));
    					
    					Format formatter = new SimpleDateFormat("dd");
						epDataStr = formatter.format(epDate) + " ";
						
						formatter = new SimpleDateFormat("MMM");
						epDataStr += formatter.format(epDate) + " ";
						
						formatter = new SimpleDateFormat("yyyy");
						epDataStr += formatter.format(epDate);	
					} catch (ParseException e) {
						Log.e(MY_DEBUG_TAG, e.getMessage());
					}
				}
				
				String enumber = "";
				if ( c.getInt(enCol) < 10 ) {
					enumber = "0" + c.getInt(enCol);
				}
				else {
					enumber = "" + c.getInt(enCol);
				}
				
				if(!epDataStr.equals("")) {
					ne = c.getInt(snCol) + "x" + enumber + " on " + epDataStr;
				}
				else {
					ne = c.getInt(snCol) + "x" + enumber;
				}
			}
			c.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return ne;
	}
	
	public int getSeasonCount(String serieId) {
		int tmpSeasons = 0;
		try {
        	Cursor cseasons = Query("SELECT season FROM serie_seasons WHERE serieId = '" + serieId + "' AND season <> 0");
           	if (cseasons != null) {
           		tmpSeasons = cseasons.getCount();
           	}
           	cseasons.close();
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
		return tmpSeasons;
	}
	
	/*
	 * Update Methods
	 */
	
	public void updateUnwatchedSeason(String serieId, int nseason) {
		try {
			db.execSQL("UPDATE episodes SET seen=1 WHERE serieId='" + serieId + "' AND seasonNumber=" + nseason);
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
	}
	
	public void updateWatchedSeason(String serieId, int nseason) {
		try {
			db.execSQL("UPDATE episodes SET seen=0 WHERE serieId='" + serieId + "' AND seasonNumber=" + nseason);
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
	}
	
	public void updateUnwatchedEpisode(String serieId, String episodeId) {
		Cursor c = null;
		try {
			c = Query("SELECT seen FROM episodes WHERE serieId='" + serieId + "' AND id='" + episodeId + "'");
			c.moveToFirst();
			int seen = c.getInt(0);
			c.close();
			
			if(seen == 0) {
				db.execSQL("UPDATE episodes SET seen=1 WHERE serieId='" + serieId + "' AND id='" + episodeId + "'");
			}
			else {
				db.execSQL("UPDATE episodes SET seen=0 WHERE serieId='" + serieId + "' AND id='" + episodeId + "'");
			}
			
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
	}

	public void updateSerieStatus(String serieId, int passiveStatus) {
		try {
			db.execSQL("UPDATE series SET passiveStatus=" + passiveStatus +
                                   " WHERE id='" + serieId + "'");
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
	}

	//UPDATE table_name SET column1=value, column2=value2,... WHERE some_column=some_value
	public void updateSerie(Serie s, boolean last_season) {
		if(s == null) {
			Log.e("DroidSeries", "Error: Serie is null");
			return;
		}
		
		try {
			String tmpSOverview = "";
			if(s.getOverview() != null) {
				if(!TextUtils.isEmpty(s.getOverview())) {
					tmpSOverview = s.getOverview().replace("\"", "'");
				}
			}
			
			String tmpSName = "";
			if(!TextUtils.isEmpty(s.getSerieName())) {
				tmpSName = s.getSerieName().replace("\"", "'"); 
			}
			
			//get the last season
			Cursor cms = null;
			int max_season = -1;
			if(last_season) {
				try {
					cms = Query("SELECT season FROM serie_seasons WHERE serieID='" + s.getId() + "'");
					cms.moveToFirst();
					if (cms != null && cms.isFirst()) {
						do {
							if(max_season < cms.getInt(0)) {
								max_season = cms.getInt(0);
							}
							//Log.d("DroidSeries", "SEASON: " + max_season);
						} while ( cms.moveToNext() );
						
					}
					cms.close();
				} catch(SQLiteException e){
					Log.e("DroidSeries", e.getMessage());
				}
			}
			
			//Log.d("DroidSeries", "MAX SEASON: " + max_season);
			
			db.execSQL("UPDATE series SET language='" + s.getLanguage() + "', serieName=\"" + tmpSName + "\", overview=\"" + tmpSOverview + "\", " +
					   "firstAired='" + s.getFirstAired() + "', imdbId='" + s.getImdbId() + "', zap2ItId='" + s.getZap2ItId() + "', " +
					   "airsDayOfWeek='" + s.getAirsDayOfWeek() + "', airsTime='" + s.getAirsTime() + "', contentRating='" + s.getContentRating() + "', " +
					   "network='" + s.getNetwork() + "db', rating='" + s.getRating() + "', runtime='" + s.getRuntime() + "', " +
					   "status='" + s.getStatus() + "', lastUpdated='" + s.getLastUpdated() + "' WHERE id='" + s.getId() + "'"); 
			
			//seasons
			db.execSQL("DELETE FROM serie_seasons WHERE serieId='" + s.getId() + "'");
			for(int n=0; n < s.getNSeasons().size(); n++){
	    		execQuery("INSERT INTO serie_seasons (serieId, season) " +
						  "VALUES ('" + s.getId()  + "', '" + s.getNSeasons().get(n) + "');");
			}
			
			//actors
			db.execSQL("DELETE FROM actors WHERE serieId='" + s.getId() + "'");
			for(int a=0; a < s.getActors().size(); a++){
	    		execQuery("INSERT INTO actors (serieId, actor) " +
						  "VALUES ('" + s.getId()  + "', \"" + s.getActors().get(a) + "\");");
			}
			
			//genres
			db.execSQL("DELETE FROM genres WHERE serieId='" + s.getId() + "'");
			for(int g=0; g < s.getGenres().size(); g++){
	    		execQuery("INSERT INTO genres (serieId, genre) " +
						  "VALUES ('" + s.getId()  + "', '" + s.getGenres().get(g) + "');");
			}
			
			//episodes
			//TODO: implement threads for episode update. Must know when they finish. (?)
			for(int e=0; e < s.getEpisodes().size(); e++) {
				if(max_season != -1) {
					if(s.getEpisodes().get(e).getSeasonNumber() < max_season) {
						continue;
					}
				}
				//ver se o episodios existe na base de dados
				Cursor c = Query("SELECT id FROM episodes WHERE id='" + s.getEpisodes().get(e).getId()  + "' AND serieId='" + s.getId() + "'");
				c.moveToFirst();
				if(c.getCount() == 1) {
					String tmpEOverview = "";
		    		if(!TextUtils.isEmpty(s.getEpisodes().get(e).getOverview())) {
		    			tmpEOverview = s.getEpisodes().get(e).getOverview().replace("\"", "'");
		    		}
		    		
		    		String tmpEName = "";
		    		if(!TextUtils.isEmpty(s.getEpisodes().get(e).getEpisodeName())) {
		    			tmpEName = s.getEpisodes().get(e).getEpisodeName().replace("\"", "'");
		    		}
		    		
		    		if(!tmpEName.equals("")) {
			    		//Log.d("DroidSeries", "Updating episode " + tmpEName);
						db.execSQL("UPDATE episodes SET combinedEpisodeNumber='" + s.getEpisodes().get(e).getCombinedEpisodeNumber() + "', combinedSeason='" + s.getEpisodes().get(e).getCombinedSeason() + "', dvdChapter='" + s.getEpisodes().get(e).getDvdChapter() + "', " +
								   "dvdChapter='" + s.getEpisodes().get(e).getDvdChapter() + "', dvdDiscId='" + s.getEpisodes().get(e).getDvdDiscId() + "', dvdEpisodeNumber=" + s.getEpisodes().get(e).getEpisodeNumber() + ", " +
								   "dvdSeason='" + s.getEpisodes().get(e).getDvdSeason() + "', epImgFlag='" + s.getEpisodes().get(e).getEpImgFlag() + "', episodeName=\"" + tmpEName + "\", " +
								   "episodeNumber=" + s.getEpisodes().get(e).getEpisodeNumber() + ", firstAired='" + s.getEpisodes().get(e).getFirstAired() + "', imdbId='" + s.getEpisodes().get(e).getImdbId() + "', " +
								   "language='" + s.getEpisodes().get(e).getLanguage() + "', overview=\"" + tmpEOverview + "\", productionCode='" + s.getEpisodes().get(e).getProductionCode() + "', " +
								   "rating='" + s.getEpisodes().get(e).getRating() + "', seasonNumber=" + s.getEpisodes().get(e).getSeasonNumber() + ", absoluteNumber='" + s.getEpisodes().get(e).getAbsoluteNumber() + "', " +
								   "lastUpdated='" + s.getEpisodes().get(e).getLastUpdated() + "', seasonId='" + s.getEpisodes().get(e).getSeasonId() + "' WHERE id='" + s.getEpisodes().get(e).getId() + "' AND serieId='" + s.getId() + "'");
						
						db.execSQL("DELETE FROM directors WHERE serieId='" + s.getId() + "'");
						for(int d=0; d < s.getEpisodes().get(e).getDirectors().size(); d++){
				    		execQuery("INSERT INTO directors (serieId, episodeId, director) " +
									  "VALUES ('" + s.getId() + "', '" + s.getEpisodes().get(e).getId() + "', \"" + s.getEpisodes().get(e).getDirectors().get(d) + "\");");
			    		}
						
						db.execSQL("DELETE FROM guestStars WHERE serieId='" + s.getId() + "'");
						for(int g=0; g < s.getEpisodes().get(e).getGuestStars().size(); g++){
				    		execQuery("INSERT INTO guestStars (serieId, episodeId, guestStar) " +
									  "VALUES ('" + s.getId() + "', '" + s.getEpisodes().get(e).getId() + "', \"" + s.getEpisodes().get(e).getGuestStars().get(g) + "\");");
			    		}
						
						db.execSQL("DELETE FROM writers WHERE serieId='" + s.getId() + "'");
						for(int w=0; w < s.getEpisodes().get(e).getWriters().size(); w++){
				    		execQuery("INSERT INTO writers (serieId, episodeId, writer) " +
									  "VALUES ('" + s.getId() + "', '" + s.getEpisodes().get(e).getId() + "', \"" + s.getEpisodes().get(e).getWriters().get(w) + "\");");
			    		}
		    		}
				}
				else {
					for(int d=0; d < s.getEpisodes().get(e).getDirectors().size(); d++){
			    		execQuery("INSERT INTO directors (serieId, episodeId, director) " +
								  "VALUES ('" + s.getId() + "', '" + s.getEpisodes().get(e).getId() + "', \"" + s.getEpisodes().get(e).getDirectors().get(d) + "\");");
		    		}
					
		    		for(int g=0; g < s.getEpisodes().get(e).getGuestStars().size(); g++){
			    		execQuery("INSERT INTO guestStars (serieId, episodeId, guestStar) " +
								  "VALUES ('" + s.getId() + "', '" + s.getEpisodes().get(e).getId() + "', \"" + s.getEpisodes().get(e).getGuestStars().get(g) + "\");");
		    		}
		    		
		    		for(int w=0; w < s.getEpisodes().get(e).getWriters().size(); w++){
			    		execQuery("INSERT INTO writers (serieId, episodeId, writer) " +
								  "VALUES ('" + s.getId() + "', '" + s.getEpisodes().get(e).getId() + "', \"" + s.getEpisodes().get(e).getWriters().get(w) + "\");");
		    		}
		    		
		    		int iseen = 0;
		    		if(s.getEpisodes().get(e).getSeen()) {
		    			iseen = 1;
		    		}
		    		
		    		String tmpOverview = "";
		    		if(s.getEpisodes().get(e).getOverview() != null) {
			    		if(!TextUtils.isEmpty(s.getEpisodes().get(e).getOverview())) {
			    			tmpOverview = s.getEpisodes().get(e).getOverview().replace("\"", "'");
			    		}
		    		}
		    		
		    		String tmpName = "";
		    		if(s.getEpisodes().get(e).getEpisodeName() != null) {
			    		if(!TextUtils.isEmpty(s.getEpisodes().get(e).getEpisodeName())) {
			    			tmpName = s.getEpisodes().get(e).getEpisodeName().replace("\"", "'");
			    		}
		    		}
		    		
		    		if(!tmpName.equals("")) {
			    		execQuery("INSERT INTO episodes (serieId, id, combinedEpisodeNumber, combinedSeason, " +
			    				 "dvdChapter, dvdDiscId, dvdEpisodeNumber, dvdSeason, epImgFlag, episodeName, " +
			    				 "episodeNumber, firstAired, imdbId, language, overview, productionCode, rating, seasonNumber, " +
			    				 "absoluteNumber, filename, lastUpdated, seasonId, seen) VALUES (" +
			    				 "'" + s.getId() + "', " + "'" + s.getEpisodes().get(e).getId() + "', " + "'" + s.getEpisodes().get(e).getCombinedEpisodeNumber() + "', " +
			    				 "'" + s.getEpisodes().get(e).getCombinedSeason() + "', " + "'" + s.getEpisodes().get(e).getDvdChapter() + "', " + "'" + s.getEpisodes().get(e).getDvdDiscId() + "', " +
			    				 "'" + s.getEpisodes().get(e).getEpisodeNumber() + "', " + "'" + s.getEpisodes().get(e).getDvdSeason() + "', " + "'" + s.getEpisodes().get(e).getEpImgFlag() + "', " +
			    				 "\"" + tmpName + "\", " + "" + s.getEpisodes().get(e).getEpisodeNumber() + ", " + "'" + s.getEpisodes().get(e).getFirstAired() + "', " +
			    				 "'" + s.getEpisodes().get(e).getImdbId() + "', " + "'" + s.getEpisodes().get(e).getLanguage() + "', " + "\"" + tmpOverview + "\", " +
			    				 "'" + s.getEpisodes().get(e).getProductionCode() + "', " +
			    				 "'" + s.getEpisodes().get(e).getRating() + "', " + "" + s.getEpisodes().get(e).getSeasonNumber() + ", " + "'" + s.getEpisodes().get(e).getAbsoluteNumber() + "', " +
			    				 "'" + s.getEpisodes().get(e).getFilename() + "', " + "'" + s.getEpisodes().get(e).getLastUpdated() + "', " + "'" + s.getEpisodes().get(e).getSeasonId() + "', " +
			    				 "" + iseen + 
			    				 ");");
		    		}
				}
				c.close();
			}
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
	}
	
	/*
	 * Delete Methods
	 */
	
	//DELETE FROM table_name WHERE some_column=some_value
	public void deleteSerie(String serieId){
		try {
			db.execSQL("DELETE FROM directors WHERE serieId='" + serieId + "'");
			db.execSQL("DELETE FROM guestStars WHERE serieId='" + serieId + "'");
			db.execSQL("DELETE FROM writers WHERE serieId='" + serieId + "'");
			db.execSQL("DELETE FROM episodes WHERE serieId='" + serieId + "'");
			db.execSQL("DELETE FROM actors WHERE serieId='" + serieId + "'");
			db.execSQL("DELETE FROM genres WHERE serieId='" + serieId + "'");
			db.execSQL("DELETE FROM serie_seasons WHERE serieId='" + serieId + "'");
			
			Cursor c = Query("SELECT posterInCache, posterThumb FROM series WHERE id='" + serieId  + "'");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				File cacheImage = new File(c.getString(0));
				cacheImage.delete();
				
				File thumbImage = new File(c.getString(1));
				thumbImage.delete();
			}
			c.close();
			db.execSQL("DELETE FROM series WHERE id='" + serieId + "'");
		} catch(SQLiteException e){
			Log.e("DroidSeries", e.getMessage());
		}
	}
	
	/*
	 ************************************************************************************
	 */
	
	@Override
	public synchronized void close() {
    	    if(db != null)
    		    db.close();
    	    super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase dbase) {
		//tabela dos directors
		dbase.execSQL("CREATE TABLE IF NOT EXISTS directors (" +
    			   "serieId VARCHAR, " +
    			   "episodeId VARCHAR, " +
    			   "director VARCHAR" +
    			   ");");
    	
    	//tabela dos guestStars
		dbase.execSQL("CREATE TABLE IF NOT EXISTS guestStars (" +
    			   "serieId VARCHAR, " +
    			   "episodeId VARCHAR, " +
    			   "guestStar VARCHAR" +
    			   ");");
    	
    	//tabela dos writers
		dbase.execSQL("CREATE TABLE IF NOT EXISTS writers (" +
    			   "serieId VARCHAR, " +
    			   "episodeId VARCHAR, " +
    			   "writer VARCHAR" +
    			   ");");
    	
    	//tabela dos episodios
		dbase.execSQL("CREATE TABLE IF NOT EXISTS episodes (" +
    		   "serieId VARCHAR, " +
 			   "id VARCHAR, " + 
 			   "combinedEpisodeNumber VARCHAR, " +
 			   "combinedSeason VARCHAR, " +
 			   "dvdChapter VARCHAR, " +
 			   "dvdDiscId VARCHAR, " +
 			   "dvdEpisodeNumber VARCHAR, " +
 			   "dvdSeason VARCHAR, " +
 			   "epImgFlag VARCHAR, " +
 			   "episodeName VARCHAR, " +
 			   "episodeNumber INT, " +
 			   "firstAired VARCHAR, " +
 			   "imdbId VARCHAR, " +
 			   "language VARCHAR, " +
 			   "overview TEXT, " +
 			   "productionCode VARCHAR, " +
 			   "rating VARCHAR, " +
 			   "seasonNumber INT, " +
 			   "absoluteNumber VARCHAR, " +
 			   "filename VARCHAR," +
 			   "lastUpdated VARCHAR, " +
 			   "seasonId VARCHAR, " +
 			   "seen INT" +
 			   ");");
    	
    	//tabela dos actores
		dbase.execSQL("CREATE TABLE IF NOT EXISTS actors (" +
    			   "serieId VARCHAR, " +
    			   "actor VARCHAR" +
    			   ");");
    	
    	//tabela dos genres
		dbase.execSQL("CREATE TABLE IF NOT EXISTS genres (" +
 			   "serieId VARCHAR, " +
 			   "genre VARCHAR" +
 			   ");");
    	
		//tabela das seasons
		dbase.execSQL("CREATE TABLE IF NOT EXISTS serie_seasons (" +
	 			   "serieId VARCHAR, " +
	 			   "season VARCHAR" +
	 			   ");");
	    		
    	//criar as tabelas
		dbase.execSQL("CREATE TABLE IF NOT EXISTS series (" +
    			   "id VARCHAR PRIMARY KEY, " +
    			   "serieId VARCHAR, " +
    			   "language VARCHAR, " +
    			   "serieName VARCHAR, " +
    			   "banner VARCHAR, " +
    			   "overview TEXT, " +
    			   "firstAired VARCHAR, " +
    			   "imdbId VARCHAR, " +
    			   "zap2ItId VARCHAR, " +			 
    			   "airsDayOfWeek VARCHAR, " +
    			   "airsTime VARCHAR, " +
    			   "contentRating VARCHAR, " +
    			   "network VARCHAR, " +
    			   "rating VARCHAR, " +
    			   "runtime VARCHAR, " +
    			   "status VARCHAR, " +
    			   "fanart VARCHAR, " +
    			   "lastUpdated VARCHAR, " +
    			   "passiveStatus INT, " +
    			   "poster VARCHAR," +	 
    			   "posterInCache VARCHAR, " +
    			   "posterThumb VARCHAR" +
    			   ");");
		
		dbase.execSQL("CREATE TABLE IF NOT EXISTS droidseries (version VARCHAR)");
		dbase.execSQL("INSERT INTO droidseries (version) VALUES ('" + droidseries.VERSION + "')");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
}
