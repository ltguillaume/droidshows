package org.droidseries.thetvdb.model;

import java.util.Date;

import android.graphics.drawable.Drawable;

public class TVShowItem {
	private String serieid;
	private String icon;
	private Drawable dicon;
	private String name;
	private int snumber;
	private String nextEpisode;
	private Date nextAir;
	private boolean completelyWatched;
	private boolean passiveStatus;
	private int epNotSeen;

	public TVShowItem(String serieid, String icon, Drawable dicon, String
			  name, int snumber, String nextEpisode, Date nextAir,
			  int epNotSeen, boolean passiveStatus,
			  boolean completelyWatched) {
		this.serieid = serieid;
		this.icon = icon;
		this.dicon = dicon;
		this.name = name;
		this.snumber = snumber;
		this.nextEpisode = nextEpisode;
		this.nextAir = nextAir;
		this.epNotSeen = epNotSeen;
		this.passiveStatus = passiveStatus;
		this.completelyWatched = completelyWatched;
	}

	public String getSerieId() {
		return this.serieid;
	}
	
	public String getIcon() {
		return this.icon;
	}
	
	public void setIcon (String icon) {
		this.icon = icon;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getSNumber() {
		return this.snumber;
	}
	
	public void setSNumber(int snumber) {
		this.snumber = snumber;
	}
	
	public Date getNextAir() {
		return this.nextAir;
	}
	
	public void setNextAir(Date nextAir) {
		this.nextAir = nextAir;
	}
	
	public String getNextEpisode() {
		return this.nextEpisode;
	}
	
	public void setNextEpisode(String nextEpisode) {
		this.nextEpisode = nextEpisode;
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
	
	public boolean getPassiveStatus() {
		return this.passiveStatus;
	}
	
	public void setPassiveStatus(boolean passiveStatus) {
		this.passiveStatus = passiveStatus;
	}
	
	public Drawable getDIcon() {
		return this.dicon;
	}
	
	public void setDIcon(Drawable dicon) {
		this.dicon = dicon;
	}
}
