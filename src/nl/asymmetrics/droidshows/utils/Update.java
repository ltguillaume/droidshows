package nl.asymmetrics.droidshows.utils;

import nl.asymmetrics.droidshows.DroidShows;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Update
{
	public boolean updateDroidShows() {
		DroidShows.db.execQuery("CREATE TABLE IF NOT EXISTS droidseries (version VARCHAR)");
		String version = getVersion();
		boolean done = false;
		if (version.equals("0.1.5-7")) {
			done = u0157To0157G();
			if (done)
				DroidShows.db.execQuery("UPDATE droidseries SET version='"+ DroidShows.VERSION +"'");
		}
		return done;
	}

	private String getVersion() {
		String version = "";
		try {
			Cursor c = DroidShows.db.Query("SELECT version FROM droidseries");
			try {
				c.moveToFirst();
				if (c != null && c.isFirst()) {
					if (c.getCount() != 0) {
						version = c.getString(0);
					}
				}
			} catch (Exception e) {}
			c.close();
		} catch (SQLiteException e) {
			Log.e(DroidShows.TAG, e.getMessage());
		}
		return version;
	}

	private boolean u0157To0157G() {
		Log.d(DroidShows.TAG, "UPDATING TO VERSION 0.1.5-7G");
		try {
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN seasonCount INTEGER");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN unwatchedAired INTEGER");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN unwatched INTEGER");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN nextEpisode VARCHAR");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN nextAir VARCHAR");
			return true;
		} catch (Exception e) {
			Log.e(DroidShows.TAG, "Error updating database");
			e.printStackTrace();
			return false;
		}
	}
}