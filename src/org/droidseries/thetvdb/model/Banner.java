package org.droidseries.thetvdb.model;

public class Banner {

    public static final String TYPE_SERIES = "series";
    public static final String TYPE_SEASON = "season";
    public static final String TYPE_POSTER = "poster";
    public static final String TYPE_FANART = "fanart";
    
    private String url;
    private String vignette;
    private String thumb;
    private String language;
    private int season = 0;
    private String bannerType;
    private String bannerType2;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVignette() {
        return vignette;
    }

    public void setVignette(String vignette) {
        this.vignette = vignette;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public String getBannerType() {
        return bannerType;
    }

    public void setBannerType(String bannerType) {
        this.bannerType = bannerType;
    }

    public String getBannerType2() {
        return bannerType2;
    }

    public void setBannerType2(String bannerType2) {
        this.bannerType2 = bannerType2;
    }
}
