package nl.asymmetrics.droidshows.thetvdb.model;

import java.util.Date;

import android.graphics.drawable.Drawable;

public class TVShowItem {
        private String serieid;
        private String language;
        private String icon;
        private Drawable dicon;
        private String name;
        private int snumber;
        private String nextEpisode;
        private Date nextAir;
        private boolean passiveStatus;
        private int unwatchedAired;
        private int unwatched;
        private String showStatus;
        private String extResources;
        private String episodeId;
        private String episodeName;
        private String episodeSeen;

        public TVShowItem(String serieid, String language, String icon, Drawable dicon, String
                          name, int snumber, String nextEpisode, Date nextAir,
                          int unwatchedAired, int unwatched, boolean passiveStatus,
                          String showStatus, String extResources) {
                this.serieid = serieid;
                this.language = language;
                this.icon = icon;
                this.dicon = dicon;
                this.name = name;
                this.snumber = snumber;
                this.nextEpisode = nextEpisode;
                this.nextAir = nextAir;
                this.unwatchedAired = unwatchedAired;
                this.unwatched = unwatched;
                this.passiveStatus = passiveStatus;
                this.showStatus = showStatus;
                this.extResources = extResources;
        }

        public String getSerieId() {
                return this.serieid;
        }

        public String getLanguage() {
            return this.language;
        }

        public String getIcon() {
                return this.icon;
        }

	        public void setIcon(String icon) {
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

        public String getNextEpisode() {
                return this.nextEpisode;
        }

	        public void setNextEpisode(String nextEpisode) {
	                this.nextEpisode = nextEpisode;
	        }

        public Date getNextAir() {
          return this.nextAir;
        }

			  public void setNextAir(Date nextAir) {
			          this.nextAir = nextAir;
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
        
        public String getShowStatus() {
                return this.showStatus;
        }

	        public void setShowStatus(String showStatus) {
	                this.showStatus = showStatus;
	        }
        
        public String getExtResources() {
            return this.extResources;
	    }
	
		    public void setExtResources(String extResources) {
		            this.extResources = extResources;
		    }

        public String getEpisodeId() {
            return this.episodeId;
	    }
	
		    public void setEpisodeId(String episodeId) {
		            this.episodeId = episodeId;
		    }

        public String getEpisodeName() {
            return this.episodeName;
	    }
		
		    public void setEpisodeName(String episodeName) {
		            this.episodeName = episodeName;
		    }

        public String getEpisodeSeen() {
            return this.episodeSeen;
	    }
	
		    public void setEpisodeSeen(String episodeSeen) {
		            this.episodeSeen = episodeSeen;
		    }
}