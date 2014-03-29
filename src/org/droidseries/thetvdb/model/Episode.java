package org.droidseries.thetvdb.model;

import java.util.ArrayList;
import java.util.List;

import org.droidseries.utils.SQLiteStore;


import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

public class Episode {

    private String id;
    private String combinedEpisodeNumber;
    private String combinedSeason;
    private String dvdChapter;
    private String dvdDiscId;
    private String dvdEpisodeNumber;
    private String dvdSeason;
    private List<String> directors = new ArrayList<String>();
    private String epImgFlag;
    private String episodeName;
    private int episodeNumber;
    private String firstAired;
    private List<String> guestStars = new ArrayList<String>();
    private String imdbId;
    private String language;
    private String overview;
    private String productionCode;
    private String rating;
    private int seasonNumber;
    private List<String> writers = new ArrayList<String>();
    private String absoluteNumber;
    private String filename;
    private String lastUpdated;
    private String seriesId;
    private String seasonId;
    // for user interaction
    private boolean seen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCombinedEpisodeNumber() {
        return combinedEpisodeNumber;
    }

    public void setCombinedEpisodeNumber(String combinedEpisodeNumber) {
        this.combinedEpisodeNumber = combinedEpisodeNumber;
    }

    public String getCombinedSeason() {
        return combinedSeason;
    }

    public void setCombinedSeason(String combinedSeason) {
        this.combinedSeason = combinedSeason;
    }

    public String getDvdChapter() {
        return dvdChapter;
    }

    public void setDvdChapter(String dvdChapter) {
        this.dvdChapter = dvdChapter;
    }

    public String getDvdDiscId() {
        return dvdDiscId;
    }

    public void setDvdDiscId(String dvdDiscId) {
        this.dvdDiscId = dvdDiscId;
    }

    public String getDvdEpisodeNumber() {
        return dvdEpisodeNumber;
    }

    public void setDvdEpisodeNumber(String dvdEpisodeNumber) {
        this.dvdEpisodeNumber = dvdEpisodeNumber;
    }

    public String getDvdSeason() {
        return dvdSeason;
    }

    public void setDvdSeason(String dvdSeason) {
        this.dvdSeason = dvdSeason;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public void setDirectors(List<String> directors) {
        this.directors = directors;
    }

    public void addDirector(String director) {
        this.directors.add(director);
    }

    public String getEpImgFlag() {
        return epImgFlag;
    }

    public void setEpImgFlag(String epImgFlag) {
        this.epImgFlag = epImgFlag;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getFirstAired() {
        return firstAired;
    }

    public void setFirstAired(String firstAired) {
        this.firstAired = firstAired;
    }

    public List<String> getGuestStars() {
        return guestStars;
    }

    public void setGuestStars(List<String> guestStars) {
        this.guestStars = guestStars;
    }

    public void addGuestStar(String guestStar) {
        this.guestStars.add(guestStar);
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getProductionCode() {
        return productionCode;
    }

    public void setProductionCode(String productionCode) {
        this.productionCode = productionCode;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public List<String> getWriters() {
        return writers;
    }

    public void setWriters(List<String> writers) {
        this.writers = writers;
    }

    public void addWriter(String writer) {
        this.writers.add(writer);
    }

    public String getAbsoluteNumber() {
        return absoluteNumber;
    }

    public void setAbsoluteNumber(String absoluteNumber) {
        this.absoluteNumber = absoluteNumber;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public boolean getSeen() {
        return this.seen;
    }

    public void setSeen(boolean visto) {
        this.seen = visto;
    }

    public boolean saveToDB(SQLiteStore SQLS) {
        try{
            for(int d=0; d < this.directors.size(); d++){
                SQLS.execQuery("INSERT INTO directors (serieId, episodeId, director) " +
                               "VALUES ('" + this.seriesId + "', '" + this.id + "', \"" + this.directors.get(d) + "\");");
            }

            for(int g=0; g < this.guestStars.size(); g++){
                SQLS.execQuery("INSERT INTO guestStars (serieId, episodeId, guestStar) " +
                               "VALUES ('" + this.seriesId + "', '" + this.id + "', \"" + this.guestStars.get(g) + "\");");
            }

            for(int w=0; w < this.writers.size(); w++){
                SQLS.execQuery("INSERT INTO writers (serieId, episodeId, writer) " +
                               "VALUES ('" + this.seriesId + "', '" + this.id + "', \"" + this.writers.get(w) + "\");");
            }

            int iseen = 0;
            if(this.seen) {
                iseen = 1;
            }

            String tmpOverview = "";
            if(!TextUtils.isEmpty(this.overview)) {
                tmpOverview = this.overview.replace("\"", "'");
            }

            String tmpName = "";
            if(!TextUtils.isEmpty(this.episodeName)) {
                tmpName = this.episodeName.replace("\"", "'");
            }

            SQLS.execQuery("INSERT INTO episodes (serieId, id, combinedEpisodeNumber, combinedSeason, " +
                           "dvdChapter, dvdDiscId, dvdEpisodeNumber, dvdSeason, epImgFlag, episodeName, " +
                           "episodeNumber, firstAired, imdbId, language, overview, productionCode, rating, seasonNumber, " +
                           "absoluteNumber, filename, lastUpdated, seasonId, seen) VALUES (" +
                           "\"" + this.seriesId + "\", " + "'" + this.id + "', " + "'" + this.combinedEpisodeNumber + "', " +
                           "'" + this.combinedSeason + "', " + "'" + this.dvdChapter + "', " + "'" + this.dvdDiscId + "', " +
                           "'" + this.dvdEpisodeNumber + "', " + "'" + this.dvdSeason + "', " + "'" + this.epImgFlag + "', " +
                           "\"" + tmpName + "\", " + "" + this.episodeNumber + ", " + "'" + this.firstAired + "', " +
                           "'" + this.imdbId + "', " + "'" + this.language + "', " + "\"" + tmpOverview + "\", " +
                           "'" + this.productionCode + "', " +
                           "'" + this.rating + "', " + "" + this.seasonNumber + ", " + "'" + this.absoluteNumber + "', " +
                           "'" + this.filename + "', " + "'" + this.lastUpdated + "', " + "'" + this.seasonId + "', " +
                           "" + iseen +
                           ");");
        } catch(SQLiteException e){
            Log.e("DroidSeries", e.getMessage());
            return false;
        }

        return true;
    }
}
