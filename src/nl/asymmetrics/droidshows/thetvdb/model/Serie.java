package nl.asymmetrics.droidshows.thetvdb.model;

import java.util.ArrayList;
import java.util.List;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

public class Serie {

    private String id;
    private String serieId;
    private String language;
    private String serieName = "";
    private String banner;
    private String overview = "";
    private String firstAired;
    private String imdbId;
    private String zap2ItId;
    private List<String> actors = new ArrayList<String>();
    private String airsDayOfWeek;
    private String airsTime;
    private String contentRating;
    private List<String> genres = new ArrayList<String>();
    private String network;
    private String rating;
    private String runtime;
    private String status;
    private String fanart;
    private String lastUpdated;
    private String poster;
    //custom attributes
    private List<Episode> episodes = new ArrayList<Episode>();
    private List<Integer> nseasons;
    private String posterInCache = "";
    private String posterThumb = "";
    private int passiveStatus = 0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSerieId() {
        return serieId;
    }

    public void setSerieId(String serieId) {
        this.serieId = serieId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSerieName() {
        return serieName;
    }

    public void setSerieName(String seriesName) {
        this.serieName = seriesName;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getFirstAired() {
        return firstAired;
    }

    public void setFirstAired(String firstAired) {
        this.firstAired = firstAired;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public String getZap2ItId() {
        return zap2ItId;
    }

    public void setZap2ItId(String zap2ItId) {
        this.zap2ItId = zap2ItId;
    }

    public List<String> getActors() {
        return actors;
    }

    public void setActors(List<String> actors) {
        this.actors = actors;
    }

    public void addActor(String actor) {
        this.actors.add(actor);
    }

    public String getAirsDayOfWeek() {
        return airsDayOfWeek;
    }

    public void setAirsDayOfWeek(String airsDayOfWeek) {
        this.airsDayOfWeek = airsDayOfWeek;
    }

    public String getAirsTime() {
        return airsTime;
    }

    public void setAirsTime(String airsTime) {
        this.airsTime = airsTime;
    }

    public String getContentRating() {
        return contentRating;
    }

    public void setContentRating(String contentRating) {
        this.contentRating = contentRating;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public void addGenre(String genre) {
        this.genres.add(genre);
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFanart() {
        return fanart;
    }

    public void setFanart(String fanart) {
        this.fanart = fanart;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
    }

    public List<Integer> getNSeasons(){
        return nseasons;
    }

    public void setNSeasons(List<Integer> nseasons) {
        this.nseasons = nseasons;
    }

    public String getPosterInCache() {
        return posterInCache;
    }

    public void setPosterInCache(String posterInCache) {
        this.posterInCache = posterInCache;
    }

    public String getPosterThumb() {
        return posterThumb;
    }

    public void setPosterThumb(String posterThumb) {
        this.posterThumb = posterThumb;
    }

    public int getPassiveStatus() {
        return passiveStatus;
    }

    public void setPassiveStatus(int passiveStatus) {
        this.passiveStatus = passiveStatus;
    }

    public boolean saveToDB(SQLiteStore SQLS) {
        try{
            for(int a=0; a < this.actors.size(); a++){
                SQLS.execQuery("INSERT INTO actors (serieId, actor) "+
                               "VALUES ('"+ this.id  +"','"+ this.actors.get(a) +"');");
            }

            for(int g=0; g < this.genres.size(); g++){
                SQLS.execQuery("INSERT INTO genres (serieId, genre) "+
                               "VALUES ('"+ this.id  +"','"+ this.genres.get(g) +"');");
            }

            for(int n=0; n < this.nseasons.size(); n++){
                SQLS.execQuery("INSERT INTO serie_seasons (serieId, season) "+
                               "VALUES ('"+ this.id  +"','"+ this.nseasons.get(n) +"');");
            }

            if(TextUtils.isEmpty(this.overview))
              this.overview = "";
            if(TextUtils.isEmpty(this.serieName))
            	this.serieName = "";
            SQLS.execQuery("INSERT INTO series (id, serieId, language, serieName, banner, overview, "+
                           "firstAired, imdbId, zap2ItId, airsDayOfWeek, airsTime, contentRating, "+
                           "network, rating, runtime, status, fanart, lastUpdated, poster, "+
                           "posterInCache, posterThumb, passiveStatus) VALUES ('"+ this.id +"','"+ this.serieId +"','"+ this.language
                           +"',"+ DatabaseUtils.sqlEscapeString(this.serieName) +",'"+ this.banner
                           +"',"+ DatabaseUtils.sqlEscapeString(this.overview) +",'"+ this.firstAired
                           +"','"+ this.imdbId +"','"+ this.zap2ItId +"','"+ this.airsDayOfWeek +"','"+ this.airsTime
                           +"','"+ this.contentRating +"','"+ this.network +"','"+ this.rating +"','"+ this.runtime
                           +"','"+ this.status +"','"+ this.fanart +"','"+ this.lastUpdated +"','"+ this.poster
                           +"','"+ this.posterInCache +"','"+ this.posterThumb +"', '"+ this.passiveStatus +"');");

            for(int e=0; e < this.episodes.size(); e++) {
                this.episodes.get(e).setSeriesId(this.id);
                this.episodes.get(e).saveToDB(SQLS);
            }
            SQLS.updateShowStats(serieId);
        } catch(SQLiteException e){
            Log.e("DroidShows", e.getMessage());
            return false;
        }

        return true;
    }
}