package org.droidseries;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.droidseries.thetvdb.TheTVDB;
import org.droidseries.thetvdb.model.Serie;
import org.droidseries.thetvdb.model.TVShowItem;
import org.droidseries.ui.SerieSeasons;
//import org.droidseries.ui.SerieViewPoster; // Disabled by Guillaume
import org.droidseries.ui.ViewSerie;
import org.droidseries.utils.SQLiteStore;
import org.droidseries.utils.Utils;
import org.droidseries.utils.Update;
import org.droidseries.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class droidseries extends ListActivity
{
	public static String VERSION = "0.1.5-7G";
	/* Menus */
	private static final int ADD_SERIE_MENU_ITEM = Menu.FIRST;
	private static final int TOGGLE_FILTER_MENU_ITEM = ADD_SERIE_MENU_ITEM + 1;
	private static final int PREFERENCES_MENU_ITEM = TOGGLE_FILTER_MENU_ITEM + 1;
	private static final int SORT_MENU_ITEM = PREFERENCES_MENU_ITEM + 1;
	private static final int ABOUT_MENU_ITEM = SORT_MENU_ITEM + 1;
	private static final int EXIT_MENU_ITEM = ABOUT_MENU_ITEM + 1;
	private static final int UPDATEALL_MENU_ITEM = EXIT_MENU_ITEM + 1;
	/* Context Menus */
	private static final int MARK_NEXT_EPISODE_AS_SEEN_CONTEXT = Menu.FIRST;
	private static final int TOGGLE_SERIE_STATUS_CONTEXT = MARK_NEXT_EPISODE_AS_SEEN_CONTEXT + 1;
	private static final int VIEW_SERIEDETAILS_CONTEXT = TOGGLE_SERIE_STATUS_CONTEXT + 1;
	private static final int VIEW_IMDB_CONTEXT = VIEW_SERIEDETAILS_CONTEXT + 1;
	private static final int UPDATE_CONTEXT = VIEW_IMDB_CONTEXT + 1;
	private static final int DELETE_CONTEXT = UPDATE_CONTEXT + 1;
	// private static final int VIEW_POSTER_CONTEXT = DELETE_CONTEXT + 1; //
	public static String on;
	private static AlertDialog m_AlertDlg;
	private final static String TAG = "DroidSeries";
	private static ProgressDialog m_ProgressDialog = null;
	private static ProgressDialog updateAllSeriesPD = null;
	public static SeriesAdapter series_adapter;
	private static ListView listView = null;
	private static String backFromSeasonSerieId = null;
	private static int backFromSeasonPosition = -1;
	private static TheTVDB theTVDB;
	private Utils utils = new Utils();
	private Update updateDS = new Update();
	private static final String PREF_NAME = "DroidSeriesPref";
	private SharedPreferences sharedPrefs;
	private static final String SORT_PREF_NAME = "sort";
	private static final int SORT_BY_NAME = 0;
	private static final int SORT_BY_LAST_UNSEEN = 1;
	private static final int SORT_BY_NAME_ICON = android.R.drawable.ic_menu_sort_alphabetically;
	private static final int SORT_BY_LAST_UNSEEN_ICON = android.R.drawable.ic_menu_agenda;
	private static int sortOption = SORT_BY_LAST_UNSEEN;
	private static final String FILTER_PREF_NAME = "filter_passive";
	private static final int FILTER_DISABLED = 0;
	private static final int FILTER_ENABLED = 1;
	private static int filterOption = FILTER_DISABLED;
	private static final String LAST_SEASON_PREF_NAME = "last_season";
	private static final int UPDATE_ALL_SEASONS = 0;
	private static final int UPDATE_LAST_SEASON_ONLY = 1;
	private static int lastSeasonOption = UPDATE_ALL_SEASONS;
	public static Thread deleteTh = null;
	public static Thread updateShowTh = null;
	private static boolean bUpdateShowTh = false;
	public static Thread updateAllShowsTh = null;
	private static boolean bUpdateAllShowsTh = false;
	private String toastMessage;
	public static SQLiteStore db;
	public static List<TVShowItem> series = null;
	private static Thread statsTh;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.main);
		db = new SQLiteStore(this);
		try {
			db.openDataBase();
		} catch (SQLException sqle) {
			try {
				db.createDataBase();
				db.close();
				try {
					db.openDataBase();
				} catch (SQLException sqle2) {
					Log.e(TAG, sqle2.getMessage());
				}
			} catch (IOException e) {
				Log.e(TAG, "Unable to create database");
			}
		}
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		if(updateDS.updateDroidSeries(getApplicationContext(), display)) {
			db.updateShowStats();
		}

		// Preferences
		sharedPrefs = getSharedPreferences(PREF_NAME, 0);
		sortOption = sharedPrefs.getInt(SORT_PREF_NAME, SORT_BY_NAME);
		filterOption = sharedPrefs.getInt(FILTER_PREF_NAME, FILTER_DISABLED);
		lastSeasonOption = sharedPrefs.getInt(LAST_SEASON_PREF_NAME, UPDATE_ALL_SEASONS);

		series = new ArrayList<TVShowItem>();
		series_adapter = new SeriesAdapter(this, R.layout.row, series);
		setListAdapter(series_adapter);
		on = getString(R.string.messages_on);
		listView = getListView();
		getUserSeries();
		statsTh = new Thread(null, getShowInfo, "stats");
		statsTh.start();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				try {
					String serieId = series.get(position).getSerieId();
					backFromSeasonSerieId = serieId;
					backFromSeasonPosition = position;
					Intent serieSeasons = new Intent(droidseries.this, SerieSeasons.class);
					serieSeasons.putExtra("serieid", serieId);
					startActivity(serieSeasons);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}); 
		// register context menu
		registerForContextMenu(listView);
	}

	/* Options Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, TOGGLE_FILTER_MENU_ITEM, 0, getString(R.string.menu_show_toggled)).setIcon(android.R.drawable.ic_menu_view);
		menu.add(0, SORT_MENU_ITEM, 0, getString(R.string.menu_sort_last_unseen)).setIcon(SORT_BY_LAST_UNSEEN_ICON);
		menu.add(0, UPDATEALL_MENU_ITEM, 0, getString(R.string.menu_update_all)).setIcon(android.R.drawable.ic_menu_upload);
		menu.add(0, ABOUT_MENU_ITEM, 0, getString(R.string.menu_about)).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, EXIT_MENU_ITEM, 0, getString(R.string.menu_exit)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (sortOption == SORT_BY_LAST_UNSEEN) {
			menu.findItem(SORT_MENU_ITEM).setIcon(SORT_BY_NAME_ICON);
			menu.findItem(SORT_MENU_ITEM).setTitle(getString(R.string.menu_sort_az));
		} else {
			menu.findItem(SORT_MENU_ITEM).setIcon(SORT_BY_LAST_UNSEEN_ICON);
			menu.findItem(SORT_MENU_ITEM).setTitle(getString(R.string.menu_sort_last_unseen));
		}
		if (filterOption == FILTER_DISABLED) {
			menu.findItem(TOGGLE_FILTER_MENU_ITEM).setTitle(getString(R.string.menu_hide_toggled));
		} else {
			menu.findItem(TOGGLE_FILTER_MENU_ITEM).setTitle(getString(R.string.menu_show_toggled));
		}		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case ADD_SERIE_MENU_ITEM :
				onSearchRequested();
				break;
			case TOGGLE_FILTER_MENU_ITEM :
				toggleFilter();
				listView.post(updateListView);
				break;
			case SORT_MENU_ITEM :
				toggleSort();
				listView.post(updateListView);
				break;
			case UPDATEALL_MENU_ITEM :
				if (!db.unsetCleanUp()) updateAllSeries();
				listView.post(updateListView);
				break;
			case ABOUT_MENU_ITEM :
				if (m_AlertDlg != null) {
					m_AlertDlg.cancel();
				}
				View about = View.inflate(this, R.layout.alert_about, null);
				CheckBox checkbox = (CheckBox) about.findViewById(R.id.checkbox);
				checkbox.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						lastSeasonOption ^= 1;
					}
				});
				checkbox.setChecked(lastSeasonOption == UPDATE_LAST_SEASON_ONLY);
				m_AlertDlg = new AlertDialog.Builder(this)
					.setView(about)
					.setTitle(getString(R.string.layout_app_name)).setIcon(R.drawable.icon)
					.setPositiveButton(getString(R.string.dialog_clean_db), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						cleanUp();
					}
				}).setCancelable(true).show();
				break;
			case EXIT_MENU_ITEM :
				this.finish();
				System.exit(0);	// kill process
		}
		return super.onOptionsItemSelected(item);
	}

	public void toggleFilter() {
		filterOption ^= 1;
		series.clear();
		getUserSeries();
		statsTh.run();
	}

	public void toggleSort() {
		sortOption ^= 1;
	}

	/* context menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, MARK_NEXT_EPISODE_AS_SEEN_CONTEXT, 0, getString(R.string.menu_context_mark_next_episode_as_seen));
		menu.add(0, TOGGLE_SERIE_STATUS_CONTEXT, 0, getString(R.string.menu_toggle));
		menu.add(0, VIEW_SERIEDETAILS_CONTEXT, 0, getString(R.string.menu_context_view_serie_details));
		menu.add(0, VIEW_IMDB_CONTEXT, 0, getString(R.string.menu_context_view_imdb));
		menu.add(0, UPDATE_CONTEXT, 0, getString(R.string.menu_context_update));
		menu.add(0, DELETE_CONTEXT, 0, getString(R.string.menu_context_delete));
		// menu.add(0, VIEW_POSTER_CONTEXT, 0, getString(R.string.menu_context_viewposter)); // Disabled by Guillaume
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		TVShowItem serie;
		String serieid;
		switch (item.getItemId()) {
			case MARK_NEXT_EPISODE_AS_SEEN_CONTEXT :
				final int oldListPosition = info.position;
				serie = series.get(oldListPosition);
				serieid = serie.getSerieId();
				String nextEpisode = db.getNextEpisodeId(serieid, -1);
				db.updateUnwatchedEpisode(serieid, nextEpisode);
				final TVShowItem newSerie = createTVShowItem(serieid);
				series.set(oldListPosition, newSerie);
				listView.post(updateListView);
				if (sortOption == SORT_BY_LAST_UNSEEN) {
					final int padding = (int) (6 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
					listView.post(new Runnable() {
						public void run() {
							int pos = series_adapter.getPosition(newSerie);
							if (pos != oldListPosition) {
								listView.setSelection(pos);
								if (0 < pos && pos < listView.getCount() - 5)
									listView.smoothScrollBy(-padding, 500);
							}
						}
					});
				}
				return true;
			case TOGGLE_SERIE_STATUS_CONTEXT :
				serie = series.get(info.position);
				serieid = serie.getSerieId();
				Integer st = db.getSerieStatus(serieid);
				st ^= 1;
				db.updateSerieStatus(serieid, st);
				if (filterOption == FILTER_ENABLED && st == 1) {	// Remove show from list if passive and filter is enabled
					series.remove(series.get(info.position));
				} else {	// Update data to change the show's title on refresh
					series.get(info.position).setPassiveStatus(st == 1);
				}
				listView.post(updateListView);
				return true;
			case VIEW_SERIEDETAILS_CONTEXT :
				showDetails(series.get(info.position).getSerieId());
				return true;
			case VIEW_IMDB_CONTEXT :
				IMDbDetails(series.get(info.position).getSerieId());
				return true;
			case UPDATE_CONTEXT :
				updateSerie(series.get(info.position).getSerieId());
				listView.post(updateListView);
				return true;
			case DELETE_CONTEXT :
				// TODO (1): add a verification to be sure that the TV show was well eliminated
				// TODO (2): add the queue struct here, it may need to restart with pending actions to do (only half deleted)
				final int spos = info.position;
				final Runnable deleteserie = new Runnable() {
					public void run() {
						String sname = db.getSerieName(series.get(spos).getSerieId());
						db.deleteSerie(series.get(spos).getSerieId());
						series.remove(series.get(spos));
						listView.post(updateListView);
						Context context = getApplicationContext();
						CharSequence text = String.format(getString(R.string.messages_delete_sucessful), sname);
						Looper.prepare();
						Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
						toast.show();
						Looper.loop();
					}
				};
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
				alertDialog.setTitle(getString(R.string.dialog_title_delete));
				alertDialog.setMessage(String.format(getString(R.string.dialog_delete), db.getSerieName(series.get(info.position).getSerieId())));
				alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
				alertDialog.setCancelable(false);
				alertDialog.setPositiveButton(getString(R.string.dialog_OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						deleteTh = new Thread(null, deleteserie, "MagentoBackground");
						deleteTh.start();
						return;
					}
				});
				alertDialog.setNegativeButton(getString(R.string.dialog_Cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
				alertDialog.show();
				return true;
				/* case VIEW_POSTER_CONTEXT: String posterincache =
				 * db.getSeriePoster(series.get(info.position).getSerieId()); if(!posterincache.equals(""))
				 * { Intent viewPoster = new Intent(droidseries.this, SerieViewPoster.class);
				 * viewPoster.putExtra("seriename",
				 * db.getSerieName(series.get(info.position).getSerieId())); viewPoster.putExtra("poster",
				 * posterincache); startActivity(viewPoster); } return true; // Disabled by Guillaume */
			default :
				return super.onContextItemSelected(item);
		}
	}
	
	private Runnable changeMessage = new Runnable() {
		public void run() {
			m_ProgressDialog.setMessage(toastMessage);
		}
	};
	
	private void showDetails(String serieId) {
		Intent viewSerie = new Intent(droidseries.this, ViewSerie.class);
		String query = "SELECT serieName, overview, status, firstAired, airsDayOfWeek, airsTime, runtime, network, rating "
			+ "FROM series WHERE id = '" + serieId + "'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			int snameCol = c.getColumnIndex("serieName");
			int overviewCol = c.getColumnIndex("overview");
			int statusCol = c.getColumnIndex("status");
			int firstAiredCol = c.getColumnIndex("firstAired");
			int airsdayofweekCol = c.getColumnIndex("airsDayOfWeek");
			int airstimeCol = c.getColumnIndex("airsTime");
			int runtimeCol = c.getColumnIndex("runtime");
			int networkCol = c.getColumnIndex("network");
			int ratingCol = c.getColumnIndex("rating");
			viewSerie.putExtra("seriename", c.getString(snameCol));
			viewSerie.putExtra("serieoverview", c.getString(overviewCol));
			viewSerie.putExtra("status", c.getString(statusCol));
			viewSerie.putExtra("firstaired", c.getString(firstAiredCol));
			viewSerie.putExtra("airday", c.getString(airsdayofweekCol));
			viewSerie.putExtra("airtime", c.getString(airstimeCol));
			viewSerie.putExtra("runtime", c.getString(runtimeCol));
			viewSerie.putExtra("network", c.getString(networkCol));
			viewSerie.putExtra("rating", c.getString(ratingCol));
			c.close();
			List<String> genres = new ArrayList<String>();
			Cursor cgenres = db.Query("SELECT genre FROM genres WHERE serieId='"
				+ serieId + "'");
			cgenres.moveToFirst();
			if (cgenres != null && cgenres.isFirst()) {
				do {
					genres.add(cgenres.getString(0));
				} while (cgenres.moveToNext());
			}
			cgenres.close();
			viewSerie.putExtra("genre", genres.toString().replace("]", "").replace("[", ""));
			List<String> actors = new ArrayList<String>();
			Cursor cactors = db.Query("SELECT actor FROM actors WHERE serieId='"
				+ serieId + "'");
			cactors.moveToFirst();
			if (cactors != null && cactors.isFirst()) {
				do {
					actors.add(cactors.getString(0));
				} while (cactors.moveToNext());
			}
			cactors.close();
			viewSerie.putExtra("serieactors", actors.toString().replace("]", "").replace("[", ""));
			startActivity(viewSerie);
		}		
	}

	private void IMDbDetails(String serieId) {
		String query = "SELECT imdbId FROM series WHERE id = '" + serieId + "'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			String imdbId = c.getString(0);
			c.close();
			Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.imdb.com/title/"+ imdbId));
			startActivity(imdb);
		}
	}
	
	private void updateSerie(String serieId) {
		final String id = serieId;
		if (utils.isNetworkAvailable(droidseries.this)) {
			Runnable updateserierun = new Runnable() {
				public void run() {
					theTVDB = new TheTVDB("8AC675886350B3C3");
					if (theTVDB.getMirror() != null) {
						// Log.d(TAG, "* DBG * droidseries.java:updateSerie #1 - Running getSerie - " + id);
						Serie sToUpdate = theTVDB.getSerie(id, getString(R.string.lang_code));
						// Log.d(TAG, "* DBG * droidseries.java:updateSerie #2 - Running db.updateserie");
						toastMessage = getString(R.string.messages_title_updating_db) + " - " + sToUpdate.getSerieName();
						runOnUiThread(changeMessage);
						db.updateSerie(sToUpdate, lastSeasonOption == 1);
						// Log.d(TAG, "* DBG * droidseries.java:updateSerie #3 - Done running db.updateserie");
					} else {
						// TODO: add a message here
					}
					if (!bUpdateShowTh) {
						m_ProgressDialog.dismiss();
						bUpdateShowTh = false;
					}
					theTVDB = null;
				}
			};
			updateShowTh = new Thread(null, updateserierun, "MagentoBackground");
			updateShowTh.start();
			m_ProgressDialog = ProgressDialog.show(droidseries.this, getString(R.string.messages_title_updating_serie), getString(R.string.messages_update_serie), true);
		} else {
			CharSequence text = getString(R.string.messages_no_internet);
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
			toast.show();
		}
	}

	public synchronized void startUpdateAllShowsTh() {
		if (updateAllShowsTh == null) {
			updateAllShowsTh = new Thread();
			updateAllShowsTh.start();
		}
	}

	public synchronized void stopUpdateAllShowsTh() {
		if (updateAllShowsTh != null) {
			Thread moribund = updateAllShowsTh;
			updateAllShowsTh = null;
			moribund.interrupt();
		}
	}

	private void cleanUp() {
		if (db.setCleanUp()) updateAllSeries();
	}

	private void updateAllSeries() {
		if (!utils.isNetworkAvailable(droidseries.this)) {
			CharSequence text = getString(R.string.messages_no_internet);
			Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
			toast.show();
		} else {
			final Runnable updateMessage = new Runnable() {
				public void run() {
					updateAllSeriesPD.setMessage(toastMessage);
				}
			};
			final Runnable updateallseries = new Runnable() {
				public void run() {
					theTVDB = new TheTVDB("8AC675886350B3C3");
					for (int i = 0; i < series.size(); i++) {
						if (bUpdateAllShowsTh) {
							stopUpdateAllShowsTh();
							bUpdateAllShowsTh = false;
							return;
						}
						Log.d(TAG, "Getting updated info from TheTVDB for TV show " + series.get(i).getName() +" ["+ i +"/"+ series.size() +"]");
						toastMessage = getString(R.string.messages_title_updating_serie) + ":\n" + series.get(i).getName() + "…";
						runOnUiThread(updateMessage);
						Serie sToUpdate = theTVDB.getSerie(series.get(i).getSerieId(), getString(R.string.lang_code));
						Log.d(TAG, "Updating the database");
						try {
							db.updateSerie(sToUpdate, lastSeasonOption == 1);
						} catch (Exception e) {
							// does nothing
						}
						if (!bUpdateAllShowsTh) {
							updateAllSeriesPD.incrementProgressBy(1);
						}
					}
					if (!bUpdateAllShowsTh) {
						updateAllSeriesPD.dismiss();
						bUpdateAllShowsTh = false;
					}
					theTVDB = null;
				}
			};
			updateAllSeriesPD = new ProgressDialog(this);
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			alertDialog.setTitle(getString(R.string.messages_title_update_all_series));
			String updateMessageAD = getString(R.string.dialog_update_all_series) + (lastSeasonOption == UPDATE_ALL_SEASONS ? getString(R.string.dialog_update_speedup) : "");
			alertDialog.setMessage(updateMessageAD);
			alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
			alertDialog.setCancelable(false);
			alertDialog.setPositiveButton(getString(R.string.dialog_OK), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					updateAllSeriesPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					updateAllSeriesPD.setTitle(getString(R.string.messages_title_updating_all_series));
					updateAllSeriesPD.setMessage(getString(R.string.messages_update_all_series));
					updateAllSeriesPD.setCancelable(false);
					updateAllSeriesPD.setMax(series.size());
					updateAllSeriesPD.setProgress(0);
					updateAllSeriesPD.show();
					updateAllShowsTh = new Thread(null, updateallseries, "MagentoBackground");
					updateAllShowsTh.start();
					return;
				}
			});
			alertDialog.setNegativeButton(getString(R.string.dialog_Cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			alertDialog.show();
		}
	}

	private static TVShowItem createTVShowItemScaffold(String serieid) {
		String name = "";
		String query = "SELECT serieName FROM series WHERE id = '" + serieid + "'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			name = c.getString(c.getColumnIndex("serieName"));
		}
		c.close();
		TVShowItem tvsi = new TVShowItem(serieid, "", null, name, 0, "", null, 0, 0, false, false, "");
		return tvsi;
	}
	
	public static Runnable getShowInfo = new Runnable() {
		public void run() {
			for (int i = 0; i < series.size(); i++) {
				String tmpPoster = "", tmpNextEpisode = "", tmpNextAir = "";
				int unwatchedAired = 0;
				String query = "SELECT posterThumb, status, passiveStatus, seasonCount, unwatchedAired, unwatched, nextEpisode, nextAir FROM series WHERE id = '" + series.get(i).getSerieId() + "'";
				Cursor c = db.Query(query);
				c.moveToFirst();
				if (c != null && c.isFirst()) {
					tmpPoster = c.getString(c.getColumnIndex("posterThumb"));
					series.get(i).setShowStatus(c.getString(c.getColumnIndex("status")));
					series.get(i).setPassiveStatus(c.getInt(c.getColumnIndex("passiveStatus")) == 1);
					series.get(i).setSNumber(c.getInt(c.getColumnIndex("seasonCount")));
					unwatchedAired = c.getInt(c.getColumnIndex("unwatchedAired"));
					series.get(i).setUnwatchedAired(unwatchedAired);
					series.get(i).setUnwatched(c.getInt(c.getColumnIndex("unwatched")));
					tmpNextEpisode = c.getString(c.getColumnIndex("nextEpisode"));
					tmpNextAir = c.getString(c.getColumnIndex("nextAir"));
				}
				c.close();
				if (!tmpPoster.equals("")) {
					series.get(i).setIcon(tmpPoster);
					Drawable poster = Drawable.createFromPath(tmpPoster);
					series.get(i).setDIcon(poster);
				}
				series.get(i).setCompletelyWatched((unwatchedAired == 0 ? true : false));
				if (!tmpNextEpisode.equals("-1"))
					series.get(i).setNextEpisode(tmpNextEpisode.replace("[on]", on));
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				if (!tmpNextAir.isEmpty()) {
					try {
						series.get(i).setNextAir(dateFormat.parse(tmpNextAir));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			listView.post(new Runnable() {
				public void run() {
					series_adapter.notifyDataSetChanged();
				}
			});
		}
	};
	
	private static TVShowItem createTVShowItem(String serieid) {
		String name = "", tmpPoster = "", showStatus = "", tmpNextEpisode = "", nextEpisode = "", tmpNextAir = "";
		int tmpStatus = 0, seasonCount = 0, unwatched = 0, unwatchedAired = 0;
		Date nextAir = null;
		String query = "SELECT serieName, posterThumb, status, passiveStatus, seasonCount, unwatchedAired, unwatched, nextEpisode, nextAir FROM series WHERE id = '" + serieid + "'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			name = c.getString(c.getColumnIndex("serieName"));
			tmpPoster = c.getString(c.getColumnIndex("posterThumb"));
			showStatus = c.getString(c.getColumnIndex("status"));
			tmpStatus = c.getInt(c.getColumnIndex("passiveStatus"));
			seasonCount = c.getInt(c.getColumnIndex("seasonCount"));
			unwatchedAired = c.getInt(c.getColumnIndex("unwatchedAired"));
			unwatched = c.getInt(c.getColumnIndex("unwatched"));
			tmpNextEpisode = c.getString(c.getColumnIndex("nextEpisode"));
			tmpNextAir = c.getString(c.getColumnIndex("nextAir"));
		}
		c.close();
		boolean completelyWatched = (unwatchedAired == 0 ? true : false);
		if (!tmpNextEpisode.equals("-1"))
			nextEpisode = tmpNextEpisode.replace("[on]", on);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (!tmpNextAir.isEmpty()) {
			try {
				nextAir = dateFormat.parse(tmpNextAir);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		Drawable poster = Drawable.createFromPath(tmpPoster);
		boolean status = (tmpStatus == 1);
		TVShowItem tvsi = new TVShowItem(serieid, tmpPoster, poster, name, seasonCount, nextEpisode, nextAir, unwatchedAired, unwatched, status, completelyWatched, showStatus);
		return tvsi;
	}

	private void getUserSeries() {
		try {
			List<String> serieids;
			if (sortOption == SORT_BY_LAST_UNSEEN) {
				serieids = db.getSeriesByNextEpisode();
			} else {
				serieids = db.getSeriesByName();
			}
			for (int i = 0; i < serieids.size(); i++) {
				String serieid = serieids.get(i);
				int st = 0;
				if (filterOption == FILTER_ENABLED) {
					st = db.getSerieStatus(serieid);
				}
				if (st == 0) {
					TVShowItem tvsi = createTVShowItemScaffold(serieid);
					series.add(tvsi);
				}
			}
			series_adapter.notifyDataSetChanged();
		} catch (Exception e) {
			Log.e(TAG, "Error populating TVShowItems, or not shows added yet.");
			e.printStackTrace();
		}
	}
	
	public static Runnable updateListView = new Runnable() {
		public void run() {
			series_adapter.notifyDataSetChanged();
			if (series != null && series.size() > 0) {
				for (int i = 0; i < series.size(); i++) {
					if (!series.get(i).equals(series_adapter.getItem(i))) {
						series_adapter.add(series.get(i));
					} else {
						TVShowItem tmpTVSI = series_adapter.getItem(i);
						int unwatched = series.get(i).getUnwatched();
						if (unwatched != tmpTVSI.getUnwatched()) {
							if (unwatched == 0) {
								tmpTVSI.setCompletelyWatched(true);
							} else {
								tmpTVSI.setCompletelyWatched(false);
							}
							tmpTVSI.setUnwatched(unwatched);
						}
						tmpTVSI.setUnwatchedAired(series.get(i).getUnwatchedAired());
						tmpTVSI.setNextEpisode(series.get(i).getNextEpisode());
						tmpTVSI.setNextAir(series.get(i).getNextAir());
					}
				}
			}
			Comparator<TVShowItem> comperator = new Comparator<TVShowItem>() {
				public int compare(TVShowItem object1, TVShowItem object2) {
					if (sortOption == SORT_BY_LAST_UNSEEN) {
						// note: nextAir can be null when there are no next episodes
						Date nextAir1 = object1.getNextAir();
						Date nextAir2 = object2.getNextAir();
						if (nextAir1 == null && nextAir2 == null) {
							return object1.getName().compareToIgnoreCase(object2.getName());
						}
						if (nextAir1 == null) {
							return 1;
						}
						if (nextAir2 == null) {
							return -1;
						}
						return nextAir1.compareTo(nextAir2);
					} else {
						return object1.getName().compareToIgnoreCase(object2.getName());
					}
				}
			};
			series_adapter.sort(comperator);
			series_adapter.notifyDataSetChanged();
		}
	};
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = sharedPrefs.edit();
		ed.putInt(SORT_PREF_NAME, sortOption);
		ed.putInt(FILTER_PREF_NAME, filterOption);
		ed.putInt(LAST_SEASON_PREF_NAME, lastSeasonOption);
		ed.commit();
		if (updateShowTh != null) {
			if (updateShowTh.isAlive()) {
				bUpdateShowTh = true;
			}
		}
		/** TODO: make this work
		if (updateAllShowsTh != null) {
			if (updateAllShowsTh.isAlive()) {
				bUpdateAllShowsTh = true;
				// Log.d(TAG, "Updating all TV shows… before pause");
				stopedUASTH = true;
			}
		}**/
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		if (backFromSeasonSerieId != null && backFromSeasonPosition > -1) {
			final TVShowItem newSerie = createTVShowItem(backFromSeasonSerieId);
		for(int i = 0; i < series.size(); i += 1) {
				if (series.get(i).getSerieId().equals(backFromSeasonSerieId)) {
				series.set(i, newSerie);
				break;
			}
		}
			runOnUiThread(updateListView);
			if (sortOption == SORT_BY_LAST_UNSEEN) {
				final int padding = (int) (6 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
				final int lastPosition = backFromSeasonPosition;
				listView.post(new Runnable() {
					public void run() {
						int pos = series_adapter.getPosition(newSerie);
						if (pos != lastPosition) {
							listView.setSelection(pos);
							if (0 < pos && pos < listView.getCount() - 5)
								listView.smoothScrollBy(-padding, 0);
						}
					}
				});
			}
		}
		backFromSeasonSerieId = null;
		backFromSeasonPosition = -1;
	}

	@Override
	public void onDestroy() {
		Runnable exitSeries = new Runnable() { public void run() {
				db.close();
		}};
		Thread thSave = new Thread(null, exitSeries, "MagentoBackground");
		thSave.start();
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		return super.onSearchRequested();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (m_ProgressDialog != null)
			m_ProgressDialog.dismiss();
		super.onSaveInstanceState(outState);
	}

	public class SeriesAdapter extends ArrayAdapter<TVShowItem>
	{
		private List<TVShowItem> items;

		public SeriesAdapter(Context context, int textViewResourceId, List<TVShowItem> series) {
			super(context, textViewResourceId, series);
			this.items = series;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.row, null);
				holder = new ViewHolder();
				holder.sn = (TextView) convertView.findViewById(R.id.seriename);
				holder.si = (TextView) convertView.findViewById(R.id.serieinfo);
				holder.sne = (TextView) convertView.findViewById(R.id.serienextepisode);
				holder.icon = (ImageView) convertView.findViewById(R.id.serieicon);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.icon.setOnClickListener(null);
			}
			TVShowItem serie = items.get(position);
			int nunwatched = serie.getUnwatched();
			int nunwatchedAired = serie.getUnwatchedAired();
			if (holder.sn != null) {
				if (serie.getPassiveStatus()) {
					holder.sn.setText("[" + serie.getName() + "]");
				} else {
					holder.sn.setText(serie.getName());
				}
			}
			if (holder.si != null) {
				String siText = "";
				int sNumber = serie.getSNumber();
				if (sNumber == 1) { // Guillaume: removed "serie.getSNumber() == 0 || "
					siText = sNumber +" "+ getString(R.string.messages_season) +" | ";
				} else {
					siText = sNumber +" "+ getString(R.string.messages_seasons) +" | ";
				}
				String unwatched = "";
				if (nunwatchedAired == 0) { // Guillaume: was nunwatched
					unwatched = getString(R.string.messages_completely_watched);
				} else {
					if (nunwatched == 1) {
						unwatched = nunwatchedAired + " " + getString(R.string.messages_of) + " " + nunwatched + " "
							+ getString(R.string.messages_episode_not_watched); // Guillaume: added "nunwatchedAired + " " + getString(R.string.messages_of) + " " + "
					} else {
						unwatched = nunwatchedAired + " " + getString(R.string.messages_of) + " " + nunwatched + " "
							+ getString(R.string.messages_episodes_not_watched); // Guillaume: added "nunwatchedAired + " " + getString(R.string.messages_of) + " " + "
					}
				}
				String continuing = (serie.getShowStatus().equalsIgnoreCase("Continuing") ? "" : "*");	// Guillaume: gimme * if show's not continuing
				holder.si.setText(siText + unwatched + continuing);
			}
			if (holder.sne != null) {
				if (!serie.getCompletelyWatched()) {
					Date nextAir = serie.getNextAir();
					Date current = new Date();
					if (nextAir != null) {
						if (nextAir.before(current)) {
							holder.sne.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
						} else {
							// Without this all next episodes lines would be bold after rotating screen
							holder.sne.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
						}
					} else {
						holder.sne.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
					}
					holder.sne.setText(getString(R.string.messages_next_episode) +" "+ serie.getNextEpisode());
					holder.sne.setVisibility(View.VISIBLE);
				} else {
					holder.sne.setVisibility(View.GONE);
				}
			}
			if (holder.icon != null) {
				try {
					if (!serie.getIcon().equals("")) {
						holder.icon.setImageDrawable(serie.getDIcon());
					}
				} catch (Exception e) {
					// e.printStackTrace();
				}
				holder.icon.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
							showDetails(items.get(position).getSerieId());
					}
				});
			}
			return convertView;
		}
	}
	static class ViewHolder
	{
		TextView sn;
		TextView si;
		TextView sne;
		ImageView icon;
	}
}