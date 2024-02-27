package nl.asymmetrics.droidshows.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Update
{
	private SQLiteStore db;
	private String currentVersion = "0.1.5-7G3";
	
	public Update(SQLiteStore db) {
		this.db = db;
	}
	
	public boolean needsUpdate() {
		return !getVersion().equals(currentVersion);
	}
	
	public boolean updateDroidShows() {
		String version = getVersion();
		boolean done = false;
		if (version.equals("0.1.5-6") || version.equals("0")) {
			done = u0156To0157();
			version = getVersion();
		}
		if (version.equals("0.1.5-7")) {
			done = u0157To0157G();
			version = getVersion();
		}
		if (version.equals("0.1.5-7G")) {
			done = u0157GTo0157G2();
			version = getVersion();
		}
		if (version.equals("0.1.5-7G2")) {
			done = u0157GTo0157G3();
		}
		return done;
	}

	private String getVersion() {
		String version = "";
		try {
			Cursor c = db.Query("SELECT version FROM droidseries");
			if (c != null && c.moveToFirst()) {
				version = c.getString(0);
				return version;
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(SQLiteStore.TAG, e.getMessage());
		}
		db.execQuery("INSERT INTO droidseries (version) VALUES ('0');");
		Log.d(SQLiteStore.TAG, "DB version blank. All updates will be run; please ignore errors.");
		return "0";
	}

	private boolean u0156To0157() {
		Log.d(SQLiteStore.TAG, "UPDATING TO VERSION 0.1.5-7");
		try {
			db.execQuery("ALTER TABLE series ADD COLUMN passiveStatus INTEGER DEFAULT 0");
			db.execQuery("UPDATE droidseries SET version='0.1.5-7'");
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
			db.execQuery("ALTER TABLE series ADD COLUMN seasonCount INTEGER DEFAULT -1");
			db.execQuery("ALTER TABLE series ADD COLUMN unwatchedAired INTEGER DEFAULT -1");
			db.execQuery("ALTER TABLE series ADD COLUMN unwatched INTEGER DEFAULT -1");
			db.execQuery("ALTER TABLE series ADD COLUMN nextEpisode VARCHAR DEFAULT '-1'");
			db.execQuery("ALTER TABLE series ADD COLUMN nextAir VARCHAR DEFAULT '-1'");
			db.execQuery("UPDATE droidseries SET version='0.1.5-7G'");
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
			db.execQuery("ALTER TABLE series ADD COLUMN extResources VARCHAR NOT NULL DEFAULT ''");
			db.execQuery("UPDATE droidseries SET version='0.1.5-7G2'");
			return true;
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, "Error updating database");
			e.printStackTrace();
			return false;
		}
	}

	private boolean u0157GTo0157G3() {
		Log.d(SQLiteStore.TAG, "UPDATING TO VERSION 0.1.5-7G3");
		try {
			if (!db.convertSeenTimestamps()) return false;
			db.execQuery("UPDATE droidseries SET version='0.1.5-7G3'");
			return true;
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, "Error updating database");
			e.printStackTrace();
			return false;
		}
	}
}