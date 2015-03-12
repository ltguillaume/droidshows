package org.droidseries.utils;

import org.droidseries.droidseries;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Update
{
	public boolean updateDroidSeries() {
		droidseries.db.execQuery("CREATE TABLE IF NOT EXISTS droidseries (version VARCHAR)");
		String version = getVersion();
		boolean done = false;
		if (version.equals("0.1.5-7")) {
			done = u0157To0157G();
			if (done)
				droidseries.db.execQuery("UPDATE droidseries SET version='"+ droidseries.VERSION +"'");
		}
		return done;
	}

	private String getVersion() {
		String version = "";
		try {
			Cursor c = droidseries.db.Query("SELECT version FROM droidseries");
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
			Log.e("DroidSeries", e.getMessage());
		}
		return version;
	}

	private boolean u0157To0157G() {
		Log.d("DroidSeries", "UPDATING TO VERSION 0.1.5-7G");
		try {
			droidseries.db.execQuery("ALTER TABLE series ADD COLUMN seasonCount INTEGER");
			droidseries.db.execQuery("ALTER TABLE series ADD COLUMN unwatchedAired INTEGER");
			droidseries.db.execQuery("ALTER TABLE series ADD COLUMN unwatched INTEGER");
			droidseries.db.execQuery("ALTER TABLE series ADD COLUMN nextEpisode VARCHAR");
			droidseries.db.execQuery("ALTER TABLE series ADD COLUMN nextAir VARCHAR");
			return true;
		} catch (Exception e) {
			Log.e("DroidSeries", "Error updating database");
			e.printStackTrace();
			return false;
		}
	}
}