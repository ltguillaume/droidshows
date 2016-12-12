package nl.asymmetrics.droidshows.utils;

import nl.asymmetrics.droidshows.DroidShows;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Update
{
	private SQLiteStore db;
	private Context context;
	
	public Update(Context context) {
		this.context = context;
	}
	
	public boolean updateDroidShows() {
		db = SQLiteStore.getInstance(context);
		db.execQuery("CREATE TABLE IF NOT EXISTS droidseries (version VARCHAR)");
		String version = getVersion();
		boolean done = false;
		if (version.equals("0.1.5-6")) {
			done = u0156To0157();
			version = getVersion();
		}
		if (version.equals("0.1.5-7")) {
			done = u0157To0157G();
		}
		if (version.equals("0.1.5-7G")) {
			done = u0157GTo0157G2();
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
			Log.e(SQLiteStore.TAG, e.getMessage());
		}
		return version;
	}

	private boolean u0156To0157() {
		Log.d(SQLiteStore.TAG, "UPDATING TO VERSION 0.1.5-7");
		try {
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN passiveStatus INTEGER DEFAULT 0");
			DroidShows.db.execQuery("UPDATE droidseries SET version='0.1.5-7'");
			return true;
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, "Error updating database");
			e.printStackTrace();
			return false;
		}
	}

	private boolean u0157To0157G() {
		Log.d(SQLiteStore.TAG, "UPDATING TO VERSION 0.1.5-7G");
		try {
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN seasonCount INTEGER DEFAULT -1");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN unwatchedAired INTEGER DEFAULT -1");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN unwatched INTEGER DEFAULT -1");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN nextEpisode VARCHAR DEFAULT '-1'");
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN nextAir VARCHAR DEFAULT '-1'");
			DroidShows.db.execQuery("UPDATE droidseries SET version='0.1.5-7G'");
			return true;
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, "Error updating database");
			e.printStackTrace();
			return false;
		}
	}

	private boolean u0157GTo0157G2() {
		Log.d(SQLiteStore.TAG, "UPDATING TO VERSION 0.1.5-7G2");
		try {
			DroidShows.db.execQuery("ALTER TABLE series ADD COLUMN extResources VARCHAR NOT NULL DEFAULT ''");
			DroidShows.db.execQuery("UPDATE droidseries SET version='0.1.5-7G2'");
			return true;
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, "Error updating database");
			e.printStackTrace();
			return false;
		}
	}
}