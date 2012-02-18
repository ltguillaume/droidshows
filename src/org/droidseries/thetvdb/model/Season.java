package org.droidseries.thetvdb.model;

public class Season {
	private String serieid;
	private int snumber;
	private String season;
	private int epNotSeen;
	private boolean completelyWatched;
	private String nextEpisode;
	private int visibility;
	
	public Season(String serieid, int snumber, String season, int epNotSeen, boolean completelyWatched, String nextEpisode, int visibility) {
		this.serieid = serieid;
		this.snumber = snumber;
		this.season = season;
		this.epNotSeen = epNotSeen;
		this.completelyWatched = completelyWatched;
		this.nextEpisode = nextEpisode;
		this.visibility = visibility;
	}
	
	public String getSerieId() {
		return this.serieid;
	}
	
	public int getSNumber() {
		return this.snumber;
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
	
	public int getVisibility() {
		return this.visibility;
	}
	
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	
	public int getEpNotSeen() {
		return this.epNotSeen;
	}
	
	public void setEpNotSeen(int epNotSeen) {
		this.epNotSeen = epNotSeen;
	}
	
	public boolean getCompletelyWatched() {
		return this.completelyWatched;
	}
	
	public void setCompletelyWatched(boolean completelyWatched) {
		this.completelyWatched = completelyWatched;
	}
}