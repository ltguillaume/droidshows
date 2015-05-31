package nl.asymmetrics.droidshows.thetvdb.model;

import java.util.ArrayList;
import java.util.List;

public class Banners {

    private List<Banner> seriesList = new ArrayList<Banner>();
    private List<Banner> seasonList = new ArrayList<Banner>();
    private List<Banner> posterList = new ArrayList<Banner>();
    private List<Banner> fanartList = new ArrayList<Banner>();

    public List<Banner> getSeriesList() {
        return seriesList;
    }

    public void setSeriesList(List<Banner> seriesList) {
        this.seriesList = seriesList;
    }

    public void addSeriesBanner(Banner banner) {
        this.seriesList.add(banner);
    }

    public List<Banner> getSeasonList() {
        return seasonList;
    }

    public void setSeasonList(List<Banner> seasonList) {
        this.seasonList = seasonList;
    }

    public void addSeasonBanner(Banner banner) {
        this.seasonList.add(banner);
    }

    public List<Banner> getPosterList() {
        return posterList;
    }

    public void setPosterList(List<Banner> posterList) {
        this.posterList = posterList;
    }

    public void addPosterBanner(Banner banner) {
        this.posterList.add(banner);
    }

    public List<Banner> getFanartList() {
        return fanartList;
    }

    public void setFanartList(List<Banner> fanartList) {
        this.fanartList = fanartList;
    }

    public void addFanartBanner(Banner banner) {
        this.fanartList.add(banner);
    }

    public void addBanner(Banner banner) {
        if (banner != null) {
            if (Banner.TYPE_SERIES.equalsIgnoreCase(banner.getBannerType())) {
                addSeriesBanner(banner);
            } else if (Banner.TYPE_SEASON.equalsIgnoreCase(banner.getBannerType())) {
                addSeasonBanner(banner);
            } else if (Banner.TYPE_POSTER.equalsIgnoreCase(banner.getBannerType())) {
                addPosterBanner(banner);
            } else if (Banner.TYPE_FANART.equalsIgnoreCase(banner.getBannerType())) {
                addFanartBanner(banner);
            }
        }
    }
}
