package nl.asymmetrics.droidshows.utils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.thetvdb.model.Episode;
import nl.asymmetrics.droidshows.thetvdb.model.Serie;
import nl.asymmetrics.droidshows.thetvdb.model.TVShowItem;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.text.TextUtils;
import android.util.Log;

public class SQLiteStore extends SQLiteOpenHelper
{
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final String TAG = "DroidShows";
	private static SQLiteStore instance = null;
	private static String DB_PATH = "";
	private static String DB_NAME = "DroidShows.db";
	private SQLiteDatabase db;
	private static String today = dateFormat.format(Calendar.getInstance().getTime());	// Get today's date;

	public static SQLiteStore getInstance(Context context) {
		if (instance == null)
			instance = new SQLiteStore(context.getApplicationContext());
		return instance;
	}
	
	private SQLiteStore(Context context) {
		super(context, DB_NAME, null, 1);
		DB_PATH = context.getApplicationInfo().dataDir +"/databases/";
		try {
			openDataBase();
		} catch (SQLException sqle) {
			try {
				createDataBase();
				close();
				try {
					openDataBase();
				} catch (SQLException sqle2) {
					Log.e(TAG, sqle2.getMessage());
				}
			} catch (IOException e) {
				Log.e(TAG, "Unable to create database");
			}
		}
	}

	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (!dbExist) {
			this.getWritableDatabase();
		}
	}

	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			Log.d(TAG, "Database does't exist yet.");
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	public void openDataBase() throws SQLException {
		// Open the database
		String myPath = DB_PATH + DB_NAME;
		db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
	}

	/* Insert Methods */
	public boolean execQuery(String query) {
		try {
			db.execSQL(query);
		} catch (SQLiteException e) {
			return false;
		}
		return true;
	}

	public Cursor Query(String query) {
		Cursor c = null;
		try {
			c = db.rawQuery(query, null);
		} catch (SQLiteException e) {
			return null;
		}
		return c;
	}

	/* Get Methods */
	public TVShowItem createTVShowItem(String serieId) {
		String name = "", tmpPoster = "", showStatus = "", tmpNextEpisode = "", nextEpisode = "", tmpNextAir = "", extResources = "";
		int tmpStatus = 0, seasonCount = 0, unwatched = 0, unwatchedAired = 0;
		Date nextAir = null;
		String query = "SELECT serieName, posterThumb, status, passiveStatus, seasonCount, unwatchedAired, unwatched, nextEpisode, nextAir, extResources FROM series WHERE id = '" + serieId + "'";
		Cursor c = Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			name = c.getString(c.getColumnIndex("serieName"));
			tmpPoster = c.getString(c.getColumnIndex("posterThumb"));
			showStatus = c.getString(c.getColumnIndex("status"));
			tmpStatus = c.getInt(c.getColumnIndex("passiveStatus"));
			seasonCount = c.getInt(c.getColumnIndex("seasonCount"));
			unwatchedAired = c.getInt(c.getColumnIndex("unwatchedAired"));
			unwatched = c.getInt(c.getColumnIndex("unwatched"));
			tmpNextEpisode = c.getString(c.getColumnIndex("nextEpisode"));
			tmpNextAir = c.getString(c.getColumnIndex("nextAir"));
			extResources = c.getString(c.getColumnIndex("extResources"));
		}
		c.close();
		if (!tmpNextEpisode.equals("-1"))
			nextEpisode = tmpNextEpisode;
		if (!tmpNextAir.isEmpty() && !tmpNextAir.equals("null")) {
			try {
				nextAir = SQLiteStore.dateFormat.parse(tmpNextAir);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		boolean status = (tmpStatus == 1);
		TVShowItem tvsi = new TVShowItem(serieId, tmpPoster, null, name, seasonCount, nextEpisode, nextAir, unwatchedAired, unwatched, status, showStatus, extResources);
		return tvsi;
	}

	
	public List<Episode> getEpisodes(String serieId) {
		List<Episode> episodes = null;
		episodes = new ArrayList<Episode>();
		Cursor c = Query("SELECT * FROM episodes WHERE serieId = '"+ serieId +"'");
		try {
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
						Cursor cdirectors = Query("SELECT director FROM directors WHERE serieId='"+ serieId
							+"' AND episodeId='"+ c.getString(idCol) +"'");
						cdirectors.moveToFirst();
						int directorCol = cdirectors.getColumnIndex("director");
						if (cdirectors != null && cdirectors.isFirst()) {
							do {
								directors.add(cdirectors.getString(directorCol));
							} while (cdirectors.moveToNext());
						}
						cdirectors.close();
						List<String> guestStars = new ArrayList<String>();
						Cursor cguestStars = Query("SELECT guestStar FROM guestStars WHERE serieId='"+ serieId
							+"' AND episodeId='"+ c.getString(idCol) +"'");
						cguestStars.moveToFirst();
						int guestStarCol = cguestStars.getColumnIndex("guestStar");
						if (cguestStars != null && cguestStars.isFirst()) {
							do {
								guestStars.add(cguestStars.getString(guestStarCol));
							} while (cguestStars.moveToNext());
						}
						cguestStars.close();
						List<String> writers = new ArrayList<String>();
						Cursor cwriters = Query("SELECT writer FROM writers WHERE serieId='"+ serieId
							+"' AND episodeId='"+ c.getString(idCol) +"'");
						cwriters.moveToFirst();
						int writersCol = cwriters.getColumnIndex("writer");
						if (cwriters != null && cwriters.isFirst()) {
							do {
								writers.add(cwriters.getString(writersCol));
							} while (cwriters.moveToNext());
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
						} else {
							eTmp.setSeen(true);
						}
						episodes.add(eTmp);
					} while (c.moveToNext());
				}
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
			c.close();
		}
		return episodes;
	}

	public List<String> getSeries(int showArchive) {
		List<String> series = new ArrayList<String>();
		String showArchiveString = "";
		if (showArchive < 2)
			showArchiveString = " WHERE passiveStatus"
					+(showArchive == 0 ? "=0 OR passiveStatus IS NULL" : ">=1");	// Solves issue with former bug when adding show directly after restoring backup
		Cursor cseries = Query("SELECT id FROM series"+ showArchiveString);
		try {
			cseries.moveToFirst();
			if (cseries != null && cseries.isFirst()) {
				do {
					series.add(cseries.getString(0));
				} while (cseries.moveToNext());
			}
			cseries.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
			cseries.close();
		}
		return series;
	}

	public EpisodeRow getEpisodeRow(String serieId, int seasonNumber, String episodeId) {
		return getEpisodeRows(serieId, seasonNumber, episodeId).get(0);
	}

	public List<EpisodeRow> getEpisodeRows(String serieId, int seasonNumber) {
		return getEpisodeRows(serieId, seasonNumber, "");
	}
	
	private List<EpisodeRow> getEpisodeRows(String serieId, int seasonNumber, String episodeId) {
		List<EpisodeRow> episodes = new ArrayList<EpisodeRow>();
		Cursor c = Query("SELECT id, episodeName, episodeNumber, seen, firstAired FROM episodes WHERE "
			+ (episodeId.isEmpty() ? "" : "id="+ episodeId +" AND ")
			+ "serieId='"+ serieId +"' AND seasonNumber="+ seasonNumber
			+" ORDER BY episodeNumber ASC");
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				do {
					String id = c.getString(c.getColumnIndex("id"));
					String name = c.getInt(c.getColumnIndex("episodeNumber")) +". "
							+ c.getString(c.getColumnIndex("episodeName"));
					String aired = c.getString(c.getColumnIndex("firstAired"));
					Date airedDate = null;
					if (!aired.isEmpty() && !aired.equals("null")) {
							try { 
								airedDate = dateFormat.parse(aired);
								aired = SimpleDateFormat.getDateInstance().format(airedDate);
							} catch (ParseException e) { e.printStackTrace(); }
					} else
						aired = "";
					int seen = c.getInt(c.getColumnIndex("seen"));
					
					episodes.add(new EpisodeRow(id, name, aired, airedDate, seen));
				} while (c.moveToNext());
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
		return episodes;
	}

	private List<EpisodeSeen> getSeen(String serieId, int max_season) {
		List<EpisodeSeen> episodesSeen = new ArrayList<EpisodeSeen>();
		Cursor c = Query("SELECT seasonNumber, episodeNumber, seen FROM episodes WHERE serieId='"+ serieId +"'"
			+ (max_season != -1 ? " AND (seasonNumber="+ max_season +" OR seasonNumber=0)": "")
			+" AND seen>0");
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				do {
					episodesSeen.add(new EpisodeSeen(c.getInt(0) +"x"+ c.getInt(1), c.getInt(2)));
				} while (c.moveToNext());
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
		return episodesSeen;
	}
	
	public String getSerieIMDbId(String serieId) {
		String imdbId = "";
		Cursor c = Query("SELECT imdbId, serieName FROM series WHERE id = '" + serieId + "'");
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			imdbId = c.getString(0);
			c.close();
		}
		return imdbId;
	}

	public String getSerieName(String serieId) {
		String sname = "";
		Cursor c = Query("SELECT serieName FROM series WHERE id='"+ serieId +"'");
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				sname = c.getString(0);
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
			return null;
		}
		return sname;
	}

	public int getEPUnwatchedAired(String serieId) {
		int unwatchedAired = 0;
		Cursor c = Query("SELECT count(id) FROM episodes WHERE serieId='"+ serieId
			+"' AND seen=0 AND firstAired < '"+ today +"' AND firstAired <> ''"
			+ (DroidShows.includeSpecialsOption ? "" : " AND seasonNumber <> 0"));
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				unwatchedAired = c.getInt(0);
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
//		Log.d(TAG, "serie="+serieId+" getEPUnwatchedAired"+" today="+today+" | unwatchedAired="+unwatchedAired);
		return unwatchedAired;
	}

	public int getSeasonEPUnwatchedAired(String serieId, int snumber) {
		int unwatched = -1;
		Cursor c = Query("SELECT count(id) FROM episodes WHERE serieId='"+ serieId +"' AND seasonNumber="+ snumber
				+" AND seen=0 AND firstAired < '"+ today +"' AND firstAired <> ''");
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				unwatched = c.getInt(0);
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
//		Log.d(TAG, "serie="+serieId+" getSeasonEPUnwatchedAired"+" today="+today+" | season ="+snumber+" | unwatchedAired="+unwatched);
		return unwatched;
	}

	public int getEPUnwatched(String serieId) {
		int unwatched = -1;
		Cursor c = Query("SELECT count(id) FROM episodes WHERE serieId='"+ serieId
			+"' AND seen=0 "+ (DroidShows.includeSpecialsOption ? "" : "AND seasonNumber <> 0"));
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				unwatched = c.getInt(0);
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
		return unwatched;
	}

	public int getSeasonEPUnwatched(String serieId, int snumber) {
		int unwatched = 0;
		Cursor c = Query("SELECT count(id) FROM episodes WHERE serieId='"+ serieId
			+"' AND seasonNumber="+ snumber +" AND seen=0");
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				unwatched = c.getInt(0);
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
		return unwatched;
	}

	public String getNextEpisodeId(String serieId) {
		return getNextEpisodeId(serieId, false);
	}

	public String getNextEpisodeId(String serieId, boolean noFutureEp) {
		int id = -1;
		Cursor c = null;
		try {
				c = Query("SELECT id FROM episodes WHERE serieId='"+ serieId +"' AND seen=0"
						+ (DroidShows.includeSpecialsOption ? "" : " AND seasonNumber <> 0")
						+ (noFutureEp ? " AND firstAired <= '"+ today +"' AND firstAired <> ''": "")
						+" ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				int index = c.getColumnIndex("id");
				id = c.getInt(index);
			}
			c.close();
		} catch (SQLiteException e) {
			if (c != null) {
				c.close();
			}
			Log.e(TAG, e.getMessage());
		}
		return ""+ id;
	}

	public NextEpisode getNextEpisode(String serieId) {
		return getNextEpisode(serieId, -1, false);
	}
		
	public NextEpisode getNextEpisode(String serieId, boolean showNextAiring) {
		return getNextEpisode(serieId, -1, showNextAiring);
	}

	public NextEpisode getNextEpisode(String serieId, int snumber) {
		return getNextEpisode(serieId, snumber, false);
	}

	private NextEpisode getNextEpisode(String serieId, int snumber, boolean showNextAiring) {
		NextEpisode nextEpisode = null;
		Cursor c = null;
		try {
			if (snumber == -1) {
				c = Query("SELECT seasonNumber, episodeNumber, firstAired FROM episodes WHERE serieId='"+ serieId
					+"' AND seen=0"+ (DroidShows.includeSpecialsOption ? "" : " AND seasonNumber <> 0")
					+ (showNextAiring ? " AND firstAired >= '"+ today +"'" : "")
					+" ORDER BY seasonNumber, episodeNumber ASC LIMIT 1");
			} else {
				c = Query("SELECT seasonNumber, episodeNumber, firstAired FROM episodes WHERE serieId='"+ serieId
					+"' AND seasonNumber="+ snumber +" AND seen=0"
					+" ORDER BY episodeNumber ASC LIMIT 1");
			}
			c.moveToFirst();
			if (c != null && c.isFirst())
				nextEpisode = new NextEpisode(serieId, c.getInt(0), c.getInt(1), c.getString(2));
			c.close();
		} catch (SQLiteException e) {
			if (c != null) {
				c.close();
			}
			Log.e(TAG, e.getMessage());
		}
		if (showNextAiring && (nextEpisode == null || nextEpisode.firstAired.equals("null")))
			return null;
		if (nextEpisode == null)
			return new NextEpisode(serieId, -1, -1, "");
		return nextEpisode;
	}
	
	public String getNextEpisodeString(NextEpisode nextEpisode) {
		return getNextEpisodeString(nextEpisode, false);
	}

	public String getNextEpisodeString(NextEpisode nextEpisode, boolean showNextAiring) {
		if (nextEpisode.episode == -1)
			return "";
		
		String nextEpisodeString = nextEpisode.season +"x"
			+ (nextEpisode.episode < 10 ? "0" : "") + nextEpisode.episode;

		if (showNextAiring) {
			NextEpisode nextEpisodeAiring = getNextEpisode(nextEpisode.serieId, true);
			if (nextEpisodeAiring != null)
				return nextEpisodeString + " | [na] "+ nextEpisodeAiring.season +"x"
					+ (nextEpisodeAiring.episode < 10 ? "0" : "") + nextEpisodeAiring.episode
					+ (nextEpisode.firstAiredDate != null ? " [on] "
						+ SimpleDateFormat.getDateInstance().format(nextEpisodeAiring.firstAiredDate) : "");
		} 
		
		return "[ne] "+ nextEpisodeString
				+ (nextEpisode.firstAiredDate != null ? " [on] "
					+ SimpleDateFormat.getDateInstance().format(nextEpisode.firstAiredDate) : "");
	}

	public int getSeasonCount(String serieId) {
		int count = 0;
		Cursor c = Query("SELECT count(season) FROM serie_seasons WHERE serieId = '"+ serieId +"' AND season <> 0");
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				count = c.getInt(0);
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
		return count;
	}

	public int getSeasonEpisodeCount(String serieId, int sNumber) {
		int count = -1;
		Cursor c = Query("SELECT count(id) FROM episodes WHERE serieId='"+ serieId +"' AND seasonNumber="+ sNumber);
		try {
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				count = c.getInt(0);
			}
			c.close();
		} catch (SQLiteException e) {
			c.close();
			Log.e(TAG, e.getMessage());
		}
		return count;
	}
	
	/* Update Methods */
	public void updateUnwatchedSeason(String serieId, int nseason) {
		try {
			Calendar cal = Calendar.getInstance();
			int seen = 10000 * cal.get(Calendar.YEAR) + 100 * (cal.get(Calendar.MONTH) +1) + cal.get(Calendar.DAY_OF_MONTH);
			//Date date = new Date();
			//int seen = 10000 * (1900 + date.getYear()) + 100 * (date.getMonth() + 1) + date.getDate();
			db.execSQL("UPDATE episodes SET seen="+ seen +" WHERE serieId='"+ serieId +"' AND seasonNumber="+ nseason
			+" AND firstAired < '"+ today +"' AND firstAired <> '' AND seen < 1");
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
		}
		updateShowStats(serieId);
	}

	public void updateWatchedSeason(String serieId, int nseason) {
		try {
			db.execSQL("UPDATE episodes SET seen=0 WHERE serieId='"+ serieId +"' AND seasonNumber="
				+ nseason);
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
		}
		updateShowStats(serieId);
	}

	public String updateUnwatchedEpisode(String serieId, String episodeId) {
		return updateUnwatchedEpisode(serieId, episodeId, -1);
	}

	public String updateUnwatchedEpisode(String serieId, String episodeId, int newSeen) {
		Cursor c = null;
		String episodeMarked = "";
		try {
			c = Query("SELECT seen, seasonNumber, episodeNumber FROM episodes WHERE serieId='"+ serieId+"' AND id='"+ episodeId +"'");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				int seen = c.getInt(0);
				int season = c.getInt(1);
				int episode = c.getInt(2);
				episodeMarked =  season +"x"+ (episode < 10 ? "0" : "") + episode;
				c.close();
				if (newSeen > -1) {
					seen = newSeen;
				} else {
					if (seen > 0)
						seen = 0;
					else {
						Calendar cal = Calendar.getInstance();
						seen = 10000 * cal.get(Calendar.YEAR) + 100 * (cal.get(Calendar.MONTH) +1) + cal.get(Calendar.DAY_OF_MONTH);
	//					Date date = new Date();
	//					seen = 10000 * (1900 + date.getYear()) + 100 * (date.getMonth() + 1) + date.getDate();
					}
				}
				db.execSQL("UPDATE episodes SET seen="+ seen +" WHERE serieId='"+ serieId +"' AND id='"+ episodeId +"'");
			}
		} catch (SQLiteException e) {
			if (c != null) c.close();
			Log.e(TAG, e.getMessage());
		}
		updateShowStats(serieId);
		return episodeMarked;
	}

	public void updateSerieStatus(String serieId, int passiveStatus) {
		try {
			db.execSQL("UPDATE series SET passiveStatus="+ passiveStatus +" WHERE id='"+ serieId +"'");
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public void updateExtResources(String serieId, String extResources) {
		try {
			db.execSQL("UPDATE series SET extResources='"+ extResources +"' WHERE id='"+ serieId +"'");
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public void updateSerie(Serie s, boolean last_season) {
		if (s == null) {
			Log.e(TAG, "Error: Serie is null");
			return;
		}
		try {
			String tmpSOverview = "";
			if (s.getOverview() != null) {
				if (!TextUtils.isEmpty(s.getOverview())) {
					tmpSOverview = s.getOverview();
				}
			}
			String tmpSName = "";
			if (!TextUtils.isEmpty(s.getSerieName())) {
				tmpSName = s.getSerieName();
			}
			Cursor cms = null;
			int max_season = -1;
			if (last_season) {
				try {
					cms = Query("SELECT season FROM serie_seasons WHERE serieID='"+ s.getId() +"'");
					cms.moveToFirst();
					if (cms != null && cms.isFirst()) {
						do {
							if (max_season < cms.getInt(0)) {
								max_season = cms.getInt(0);
							}
						} while (cms.moveToNext());
					}
					cms.close();
					Log.d(TAG, "Updating only last season "+ max_season +" and specials of "+ tmpSName);
				} catch (SQLiteException e) {
					if (cms != null) {
						cms.close();
					}
					Log.e(TAG, e.getMessage());
				}
			} else {
				Log.d(TAG, "Updating all seasons of "+ tmpSName);
			}
			// Log.d(TAG, "MAX SEASON: "+ max_season);
			db.beginTransaction();
			db.execSQL("UPDATE series SET language='"+ s.getLanguage() +"', serieName="+ DatabaseUtils.sqlEscapeString(tmpSName)
				+", overview="+ DatabaseUtils.sqlEscapeString(tmpSOverview) +", "+"firstAired='"+ s.getFirstAired()
				+"', imdbId='"+ s.getImdbId() +"', zap2ItId='"+ s.getZap2ItId()
				+"', airsDayOfWeek='"+ s.getAirsDayOfWeek() +"', airsTime='"+ s.getAirsTime()
				+"', contentRating='"+ s.getContentRating() +"', "+"network='"+ s.getNetwork()
				+"', rating='"+ s.getRating() +"', runtime='"+ s.getRuntime() +"', "+"status='"
				+ s.getStatus() +"', lastUpdated='"+ s.getLastUpdated() +"' WHERE id='"+ s.getId() +"'");
			db.execSQL("DELETE FROM serie_seasons WHERE serieId='"+ s.getId() +"'");
			for (int n = 0; n < s.getNSeasons().size(); n++) {
				execQuery("INSERT INTO serie_seasons (serieId, season) "+"VALUES ('"+ s.getId() +"', '"
					+ s.getNSeasons().get(n) +"');");
			}
			db.execSQL("DELETE FROM actors WHERE serieId='"+ s.getId() +"'");
			for (int a = 0; a < s.getActors().size(); a++) {
				execQuery("INSERT INTO actors (serieId, actor) "+"VALUES ('"+ s.getId()
				+"',"+ DatabaseUtils.sqlEscapeString(s.getActors().get(a)) +");");
			}
			db.execSQL("DELETE FROM genres WHERE serieId='"+ s.getId() +"'");
			for (int g = 0; g < s.getGenres().size(); g++) {
				execQuery("INSERT INTO genres (serieId, genre) "+"VALUES ('"+ s.getId()
				+"',"+ DatabaseUtils.sqlEscapeString(s.getGenres().get(g)) +");");
			}
			
			if (max_season != -1) {
				String episodes = "";
				try {
					cms = Query("SELECT id FROM episodes WHERE serieId='"+ s.getId() +"'"
					+" AND (seasonNumber="+ max_season +" OR seasonNumber=0)");
					cms.moveToFirst();
					if (cms != null && cms.isFirst()) {
						do {
							episodes += "'"+ cms.getString(0) +"', ";
						} while (cms.moveToNext());
					}
					cms.close();
					episodes = episodes.substring(0, episodes.length() - 2);
					db.execSQL("DELETE FROM directors WHERE serieId='"+ s.getId() +"' AND episodeId IN ("+ episodes +")");
					db.execSQL("DELETE FROM guestStars WHERE serieId='"+ s.getId() +"' AND episodeId IN ("+ episodes +")");
					db.execSQL("DELETE FROM writers WHERE serieId='"+ s.getId() +"' AND episodeId IN ("+ episodes +")");
				} catch (SQLiteException e) {
					if (cms != null) {
						cms.close();
					}
					Log.e(TAG, e.getMessage());
				}
			} else {
				db.execSQL("DELETE FROM directors WHERE serieId='"+ s.getId() +"'");
				db.execSQL("DELETE FROM guestStars WHERE serieId='"+ s.getId() +"'");
				db.execSQL("DELETE FROM writers WHERE serieId='"+ s.getId() +"'");
			}

			List<EpisodeSeen> seenEpisodes = getSeen(s.getId(), max_season);
			db.execSQL("DELETE FROM episodes WHERE serieId='"+ s.getId() +"'"
					+(max_season != -1 ? " AND (seasonNumber="+ max_season +" OR seasonNumber=0)" : ""));
			
			for (int e = 0; e < s.getEpisodes().size(); e++) {
				if (max_season != -1) {
					int season = s.getEpisodes().get(e).getSeasonNumber();
					if (season < max_season && season != 0) {	// include specials in update
						continue;
					}
				}
				
				Episode ep = s.getEpisodes().get(e); 
				
				if (ep.getEpisodeNumber() == 0 && ep.getEpisodeName().equals(" ") && ep.getOverview() == null) {
					continue;
				}
								
				for (int d = 0; d < ep.getDirectors().size(); d++) {
					execQuery("INSERT INTO directors (serieId, episodeId, director) "+"VALUES ('"
						+ s.getId() +"', '"+ ep.getId()
						+"',"+ DatabaseUtils.sqlEscapeString(ep.getDirectors().get(d)) +");");
				}
				for (int g = 0; g < ep.getGuestStars().size(); g++) {
					execQuery("INSERT INTO guestStars (serieId, episodeId, guestStar) "+"VALUES ('"
						+ s.getId() +"', '"+ ep.getId()
						+"',"+ DatabaseUtils.sqlEscapeString(ep.getGuestStars().get(g)) +");");
				}
				for (int w = 0; w < ep.getWriters().size(); w++) {
					execQuery("INSERT INTO writers (serieId, episodeId, writer) "+"VALUES ('"+ s.getId()
						+"', '"+ ep.getId()
						+"',"+ DatabaseUtils.sqlEscapeString(ep.getWriters().get(w)) +");");
				}
				String tmpOverview = "";
				if (ep.getOverview() != null) {
					if (!TextUtils.isEmpty(ep.getOverview())) {
						tmpOverview = ep.getOverview();
					}
				}
				String tmpName = "";
				if (ep.getEpisodeName() != null) {
					if (!TextUtils.isEmpty(ep.getEpisodeName())) {
						tmpName = ep.getEpisodeName();
					}
				}
				
				int iseen = 0;
				String epCode = ep.getSeasonNumber() +"x"+ ep.getEpisodeNumber();
				for (EpisodeSeen es : seenEpisodes) {
					if (epCode.equals(es.episode)) {
						iseen = es.seen;
						break;
					}
				}
								
				if (!tmpName.equals("")) {
					execQuery("INSERT INTO episodes (serieId, id, combinedEpisodeNumber, combinedSeason, "
						+"dvdChapter, dvdDiscId, dvdEpisodeNumber, dvdSeason, epImgFlag, episodeName, "
						+"episodeNumber, firstAired, imdbId, language, overview, productionCode, rating, seasonNumber, "
						+"absoluteNumber, filename, lastUpdated, seasonId, seen) VALUES ('"
						+ s.getId()
						+"', '"
						+ ep.getId()
						+"', '"
						+ ep.getCombinedEpisodeNumber()
						+"', '"
						+ ep.getCombinedSeason()
						+"', '"
						+ ep.getDvdChapter()
						+"', '"
						+ ep.getDvdDiscId()
						+"', '"
						+ ep.getEpisodeNumber()
						+"', '"
						+ ep.getDvdSeason()
						+"', '"
						+ ep.getEpImgFlag()
						+"',"
						+ DatabaseUtils.sqlEscapeString(tmpName)
						+", "
						+ ep.getEpisodeNumber()
						+", '"
						+ ep.getFirstAired()
						+"', '"
						+ ep.getImdbId()
						+"', '"
						+ ep.getLanguage()
						+"',"
						+ DatabaseUtils.sqlEscapeString(tmpOverview)
						+", '"
						+ ep.getProductionCode()
						+"', '"
						+ ep.getRating()
						+"', "
						+ ep.getSeasonNumber()
						+", '"
						+ ep.getAbsoluteNumber()
						+"', '"
						+ ep.getFilename()
						+"', '"
						+ ep.getLastUpdated()
						+"', '"
						+ ep.getSeasonId() +"', "+ iseen +");");
				}
			}
			db.setTransactionSuccessful();
			db.endTransaction();
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
		}
		updateShowStats(s.getId());
	}

	/* Delete Methods */
	// DELETE FROM table_name WHERE some_column=some_value
	public void deleteSerie(String serieId) {
		try {
			db.execSQL("DELETE FROM directors WHERE serieId='"+ serieId +"'");
			db.execSQL("DELETE FROM guestStars WHERE serieId='"+ serieId +"'");
			db.execSQL("DELETE FROM writers WHERE serieId='"+ serieId +"'");
			db.execSQL("DELETE FROM episodes WHERE serieId='"+ serieId +"'");
			db.execSQL("DELETE FROM actors WHERE serieId='"+ serieId +"'");
			db.execSQL("DELETE FROM genres WHERE serieId='"+ serieId +"'");
			db.execSQL("DELETE FROM serie_seasons WHERE serieId='"+ serieId +"'");
			Cursor c = Query("SELECT posterThumb FROM series WHERE id='"+ serieId +"'");
			c.moveToFirst();
			if (c != null && c.isFirst()) {
				File thumbImage = new File(c.getString(0));
				thumbImage.delete();
			}
			c.close();
			db.execSQL("DELETE FROM series WHERE id='"+ serieId +"'");
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	public void deleteEpisode(String serieId, String episodeId) {
		try {
			db.execSQL("DELETE FROM directors WHERE serieId='"+ serieId +"' AND episodeId='"+ episodeId +"'");
			db.execSQL("DELETE FROM guestStars WHERE serieId='"+ serieId +"' AND episodeId='"+ episodeId +"'");
			db.execSQL("DELETE FROM writers WHERE serieId='"+ serieId +"' AND episodeId='"+ episodeId +"'");
			db.execSQL("DELETE FROM episodes WHERE serieId='"+ serieId +"' AND id='"+ episodeId +"'");			
		} catch (SQLiteException e) {
			Log.e(TAG, e.getMessage());
		}
		updateShowStats(serieId);
	}

	/* *********************************************************************************** */
	@Override
	public synchronized void close() {
		if (db != null) db.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase dbase) {
		dbase.execSQL("CREATE TABLE IF NOT EXISTS droidseries (version VARCHAR);");
		dbase.execSQL("INSERT INTO droidseries (version) VALUES ('0.1.5-7G2');");
		// tabela dos directors
		dbase.execSQL("CREATE TABLE IF NOT EXISTS directors (serieId VARCHAR, episodeId VARCHAR, director VARCHAR);");
		// tabela dos guestStars
		dbase.execSQL("CREATE TABLE IF NOT EXISTS guestStars (serieId VARCHAR, episodeId VARCHAR, guestStar VARCHAR);");
		// tabela dos writers
		dbase.execSQL("CREATE TABLE IF NOT EXISTS writers (serieId VARCHAR, episodeId VARCHAR, writer VARCHAR);");
		// tabela dos episodios
		dbase.execSQL("CREATE TABLE IF NOT EXISTS episodes (serieId VARCHAR, id VARCHAR, "
			+"combinedEpisodeNumber VARCHAR, combinedSeason VARCHAR, dvdChapter VARCHAR, "
			+"dvdDiscId VARCHAR, dvdEpisodeNumber VARCHAR, dvdSeason VARCHAR, "
			+"epImgFlag VARCHAR, episodeName VARCHAR, episodeNumber INT, "
			+"firstAired VARCHAR, imdbId VARCHAR, language VARCHAR, overview TEXT, "
			+"productionCode VARCHAR, rating VARCHAR, seasonNumber INT, "
			+"absoluteNumber VARCHAR, filename VARCHAR,lastUpdated VARCHAR, "
			+"seasonId VARCHAR, seen INT);");
		// tabela dos actores
		dbase.execSQL("CREATE TABLE IF NOT EXISTS actors (serieId VARCHAR, actor VARCHAR);");
		// tabela dos genres
		dbase.execSQL("CREATE TABLE IF NOT EXISTS genres (serieId VARCHAR, genre VARCHAR);");
		// tabela das seasons
		dbase.execSQL("CREATE TABLE IF NOT EXISTS serie_seasons (serieId VARCHAR, season VARCHAR);");
		// create tables
		dbase.execSQL("CREATE TABLE IF NOT EXISTS series (id VARCHAR PRIMARY KEY, "
			+"serieId VARCHAR, language VARCHAR, serieName VARCHAR, banner VARCHAR, "
			+"overview TEXT, firstAired VARCHAR, imdbId VARCHAR, zap2ItId VARCHAR, "
			+"airsDayOfWeek VARCHAR, airsTime VARCHAR, contentRating VARCHAR, "
			+"network VARCHAR, rating VARCHAR, runtime VARCHAR, status VARCHAR, "
			+"fanart VARCHAR, lastUpdated VARCHAR, passiveStatus INTEGER DEFAULT 0, poster VARCHAR, "
			+"posterInCache VARCHAR, posterThumb VARCHAR, "
			+"seasonCount INTEGER, unwatchedAired INTEGER, unwatched INTEGER, nextEpisode VARCHAR, nextAir VARCHAR, "
			+"extResources VARCHAR NOT NULL DEFAULT '');");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}
	
	public void updateShowStats() {
		List<String> series = getSeries(2);	// 2 = archive and current shows
		for (int i = 0; i < series.size(); i += 1) {
			updateShowStats(series.get(i));
		}
	}
	
	public void updateShowStats(String serieId) {
		int seasonCount = getSeasonCount(serieId);
		int unwatchedAired = getEPUnwatchedAired(serieId);
		int unwatched = getEPUnwatched(serieId);
		NextEpisode nextEpisode = getNextEpisode(serieId);
		String nextEpisodeString = getNextEpisodeString(nextEpisode, DroidShows.showNextAiring && 0 < unwatchedAired && unwatchedAired < unwatched);
		execQuery("UPDATE series SET seasonCount="+ seasonCount +", unwatchedAired="+ unwatchedAired +", unwatched="+ unwatched +", nextEpisode='"+ nextEpisodeString +"', nextAir='"+ nextEpisode.firstAired +"' WHERE id="+ serieId);
	}
	
	public void updateToday(String newToday) {
		today = newToday;
	}
	
	private class EpisodeSeen {
		public String episode;
		public int seen;
		
		public EpisodeSeen(String episode, int seen) {
			this.episode = episode;
			this.seen = seen;
		}
	}
	
	public class EpisodeRow {
		public String id;
		public String name;
		public String aired;
		public Date airedDate;
		public int seen;
		
		public EpisodeRow(String id, String name, String aired, Date airedDate, int seen) {
			this.id = id;
			this.name = name;
			this.aired = aired;
			this.airedDate = airedDate;
			this.seen = seen;
		}
	}
	
	public class NextEpisode {
		public String serieId;
		public int season;
		public int episode;
		public String firstAired;
		public Date firstAiredDate = null;
		
		public NextEpisode(String serieId, int season, int episode, String firstAired) {
			this.serieId = serieId;
			this.season = season;
			this.episode = episode;
			this.firstAired = firstAired;
			if (!firstAired.equals("") && !firstAired.equals("null")) {
				try { this.firstAiredDate = new SimpleDateFormat("yyyy-MM-dd").parse(firstAired);	// used by seasons AsyncTask, so shouldn't use dateFormat
				} catch (ParseException e) { e.printStackTrace(); }
			}
		}
	}
}