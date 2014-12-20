package org.droidseries.thetvdb.model;

public class Season {
        private String serieid;
        private int snumber;
        private String season;
        private int unwatchedAired;
        private int unwatched;
        private boolean completelyWatched;
        private String nextEpisode;

        public Season(String serieid, int snumber, String season, int unwatchedAired, int unwatched, boolean completelyWatched, String nextEpisode) {
                this.serieid = serieid;
                this.snumber = snumber;
                this.season = season;
                this.unwatchedAired = unwatchedAired;
                this.unwatched = unwatched;
                this.completelyWatched = completelyWatched;
                this.nextEpisode = nextEpisode;
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

        public boolean getCompletelyWatched() {
                return this.completelyWatched;
        }

        public void setCompletelyWatched(boolean completelyWatched) {
                this.completelyWatched = completelyWatched;
        }
}
