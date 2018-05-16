package nl.asymmetrics.droidshows.thetvdb.model;

import java.util.Date;

public class Season {
	private String serieId;
	private int sNumber;
	private String season;
	private int unwatchedAired;
	private int unwatched;
	private String nextEpisode;
	private Date nextAir;

	public Season(String serieId, int sNumber, String season, int unwatchedAired, int unwatched, String nextEpisode, Date nextAir) {
		this.serieId = serieId;
		this.sNumber = sNumber;
		this.season = season;
		this.unwatchedAired = unwatchedAired;
		this.unwatched = unwatched;
		this.nextEpisode = nextEpisode;
		this.nextAir = nextAir;
	}

	public String getSerieId() {
		return this.serieId;
	}

	public int getSNumber() {
		return this.sNumber;
	}

	public String getSeason() {
		return this.season;
	}

	public void setSeason(String season) {
		this.season = season;
	}

	public String getNextEpisode() {
		return this.nextEpisode;
	}

	public void setNextEpisode(String nextEpisode) {
		this.nextEpisode = nextEpisode;
	}

	public int getUnwatched() {
		return this.unwatched;
	}

	public void setUnwatched(int unwatched) {
		this.unwatched = unwatched;
	}

	public int getUnwatchedAired() {
		return this.unwatchedAired;
	}

	public void setUnwatchedAired(int unwatchedAired) {
		this.unwatchedAired = unwatchedAired;
	}

	public Date getNextAir() {
		return this.nextAir;
	}

	public void setNextAir(Date nextAir) {
		this.nextAir = nextAir;
	}
}