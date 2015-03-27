package org.droidseries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.droidseries.thetvdb.TheTVDB;
import org.droidseries.thetvdb.model.Serie;
import org.droidseries.thetvdb.model.TVShowItem;
import org.droidseries.ui.IconView;
import org.droidseries.ui.SerieSeasons;
import org.droidseries.ui.SwipeDetect;
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class droidseries extends ListActivity
{
	public static String VERSION = "0.1.5-7G";
	/* Menus */
	private static final int UNDO_MENU_ITEM = Menu.FIRST; 
	private static final int ADD_SERIE_MENU_ITEM = UNDO_MENU_ITEM + 1;
	private static final int TOGGLE_FILTER_MENU_ITEM = ADD_SERIE_MENU_ITEM + 1;
	private static final int PREFERENCES_MENU_ITEM = TOGGLE_FILTER_MENU_ITEM + 1;
	private static final int SORT_MENU_ITEM = PREFERENCES_MENU_ITEM + 1;
	private static final int UPDATEALL_MENU_ITEM = SORT_MENU_ITEM + 1;
	private static final int ABOUT_MENU_ITEM = UPDATEALL_MENU_ITEM + 1;
	private static final int EXIT_MENU_ITEM = ABOUT_MENU_ITEM + 1;
	/* Context Menus */
	private static final int MARK_NEXT_EPISODE_AS_SEEN_CONTEXT = Menu.FIRST;
	private static final int TOGGLE_SERIE_STATUS_CONTEXT = MARK_NEXT_EPISODE_AS_SEEN_CONTEXT + 1;
	private static final int VIEW_SERIEDETAILS_CONTEXT = TOGGLE_SERIE_STATUS_CONTEXT + 1;
	private static final int VIEW_IMDB_CONTEXT = VIEW_SERIEDETAILS_CONTEXT + 1;
	private static final int UPDATE_CONTEXT = VIEW_IMDB_CONTEXT + 1;
	private static final int DELETE_CONTEXT = UPDATE_CONTEXT + 1;
	public static String on;
	private static AlertDialog m_AlertDlg;
	private final static String TAG = "DroidSeries";
	private static ProgressDialog m_ProgressDialog = null;
	private static ProgressDialog updateAllSeriesPD = null;
	public static SeriesAdapter seriesAdapter;
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
	private static int lastSeasonOption;
	private static final String INCLUDE_SPECIALS_NAME = "include_specials";
	public static boolean includeSpecialsOption;
	private static final String FULL_LINE_CHECK_NAME = "full_line";
	public static boolean fullLineCheckOption;
	public static Thread deleteTh = null;
	public static Thread updateShowTh = null;
	private static boolean bUpdateShowTh = false;
	public static Thread updateAllShowsTh = null;
	private static boolean bUpdateAllShowsTh = false;
	private String toastMessage;
	public static SQLiteStore db;
	public static List<TVShowItem> series = null;
	private static List<String[]> undo = new ArrayList<String[]>();
	private static SwipeDetect swipeDetect;
	private static AsyncInfo asyncInfo;
	
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

		if(updateDS.updateDroidSeries())
			db.updateShowStats();

		// Preferences
		sharedPrefs = getSharedPreferences(PREF_NAME, 0);
		sortOption = sharedPrefs.getInt(SORT_PREF_NAME, SORT_BY_NAME);
		filterOption = sharedPrefs.getInt(FILTER_PREF_NAME, FILTER_DISABLED);
		lastSeasonOption = sharedPrefs.getInt(LAST_SEASON_PREF_NAME, UPDATE_ALL_SEASONS);
		includeSpecialsOption = sharedPrefs.getBoolean(INCLUDE_SPECIALS_NAME, false);
		fullLineCheckOption = sharedPrefs.getBoolean(FULL_LINE_CHECK_NAME, false);

		series = new ArrayList<TVShowItem>();
		seriesAdapter = new SeriesAdapter(this, R.layout.row, series);
		setListAdapter(seriesAdapter);
		on = getString(R.string.messages_on);
		listView = getListView();
		listView.getViewTreeObserver().addOnGlobalLayoutListener(listDone);
		getSeries();
		registerForContextMenu(listView);
		swipeDetect = new SwipeDetect();
		listView.setOnTouchListener(swipeDetect);
	}
	
	private final OnGlobalLayoutListener listDone = new OnGlobalLayoutListener() {
		public void onGlobalLayout() {
			listView.getViewTreeObserver().removeGlobalOnLayoutListener(listDone);
			asyncInfo = new AsyncInfo();
			asyncInfo.execute();
		}
	};

	/* Options Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, UNDO_MENU_ITEM, 0, getString(R.string.menu_undo)).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, TOGGLE_FILTER_MENU_ITEM, 0, getString(R.string.menu_show_toggled)).setIcon(android.R.drawable.ic_menu_view);
		menu.add(0, SORT_MENU_ITEM, 0, getString(R.string.menu_sort_last_unseen)).setIcon(SORT_BY_LAST_UNSEEN_ICON);
		menu.add(0, UPDATEALL_MENU_ITEM, 0, getString(R.string.menu_update_all)).setIcon(android.R.drawable.ic_menu_upload);
		menu.add(0, ABOUT_MENU_ITEM, 0, getString(R.string.menu_about)).setIcon(android.R.drawable.ic_menu_manage);
		menu.add(0, EXIT_MENU_ITEM, 0, getString(R.string.menu_exit)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (undo.size() > 0) {
			menu.findItem(UNDO_MENU_ITEM).setVisible(true);
		} else {
			menu.findItem(UNDO_MENU_ITEM).setVisible(false);
		}
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
				break;
			case SORT_MENU_ITEM :
				toggleSort();
				break;
			case UPDATEALL_MENU_ITEM :
				if (!db.unsetCleanUp()) updateAllSeries();
				break;
			case ABOUT_MENU_ITEM :
				if (m_AlertDlg != null) {
					m_AlertDlg.cancel();
				}
				View about = View.inflate(this, R.layout.alert_about, null);
				TextView changelog = (TextView) about.findViewById(R.id.changelog);
				try {
					changelog.setText(getString(R.string.changelog).replace("{v}", getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
					changelog.setTextColor(changelog.getTextColors().getDefaultColor());
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				CheckBox lastSeasonCheckbox = (CheckBox) about.findViewById(R.id.last_season);
				lastSeasonCheckbox.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						lastSeasonOption ^= 1;
					}
				});
				lastSeasonCheckbox.setChecked(lastSeasonOption == UPDATE_LAST_SEASON_ONLY);
				CheckBox includeSpecialsCheckbox = (CheckBox) about.findViewById(R.id.include_specials);
				includeSpecialsCheckbox.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						includeSpecialsOption = !includeSpecialsOption;
						db.updateShowStats();
						getSeries();
					}
				});
				includeSpecialsCheckbox.setChecked(includeSpecialsOption);
				CheckBox fullLineCheckbox = (CheckBox) about.findViewById(R.id.full_line_check);
				fullLineCheckbox.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						fullLineCheckOption = !fullLineCheckOption;
					}
				});
				fullLineCheckbox.setChecked(fullLineCheckOption);
				m_AlertDlg = new AlertDialog.Builder(this)
					.setView(about)
					.setTitle(getString(R.string.layout_app_name)).setIcon(R.drawable.icon)
					.setNeutralButton(getString(R.string.dialog_clean_db), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							cleanUp();
						}
					})
					.show();
				m_AlertDlg.setCanceledOnTouchOutside(true);
				break;
			case UNDO_MENU_ITEM :
				markLastEpUnseen();
				break;
			case EXIT_MENU_ITEM :
				onPause();	// save options
				asyncInfo.cancel(true);
				this.finish();
				System.gc();
				System.exit(0);	// kill process
		}
		return super.onOptionsItemSelected(item);
	}

	public void toggleFilter() {
		filterOption ^= 1;
		getSeries();
		listView.post(updateListView);
	}

	public void toggleSort() {
		sortOption ^= 1;
		listView.post(updateListView);
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
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		TVShowItem serie;
		String serieId;
		switch (item.getItemId()) {
			case MARK_NEXT_EPISODE_AS_SEEN_CONTEXT :
				markNextEpSeen(info.position);
				return true;
			case TOGGLE_SERIE_STATUS_CONTEXT :
				serie = series.get(info.position);
				serieId = serie.getSerieId();
				Integer st = db.getSerieStatus(serieId);
				st ^= 1;
				db.updateSerieStatus(serieId, st);
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
				updateSerie(series.get(info.position).getSerieId(), info.position);
				return true;
			case DELETE_CONTEXT :
				// TODO (1): add a verification to be sure that the TV show was well eliminated
				final int spos = info.position;
				final Runnable deleteserie = new Runnable() {
					public void run() {
						String sname = db.getSerieName(series.get(spos).getSerieId());
						db.deleteSerie(series.get(spos).getSerieId());
						series.remove(series.get(spos));
						listView.post(updateListView);
						Looper.prepare();	// Threads don't have a message loop
						Toast.makeText(getApplicationContext(), sname +" "+ getString(R.string.messages_deleted), Toast.LENGTH_LONG).show();
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
			default :
				return super.onContextItemSelected(item);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (swipeDetect.detected()) {
			if (markNextEpSeen(position)) {
				Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vib.vibrate(150);
			}
		} else {
			try {
				String serieId = series.get(position).getSerieId();
				backFromSeasonSerieId = serieId;
				backFromSeasonPosition = position;
				Intent serieSeasons = new Intent(droidseries.this, SerieSeasons.class);
				serieSeasons.putExtra("serieId", serieId);
				startActivity(serieSeasons);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
	
	private boolean markNextEpSeen(final int oldListPosition) {
		TVShowItem serie = series.get(oldListPosition);
		String serieId = serie.getSerieId();
		String nextEpisode = db.getNextEpisodeId(serieId, -1);
		if (!nextEpisode.equals("-1")) {
			String episodeMarked = db.updateUnwatchedEpisode(serieId, nextEpisode);
			final TVShowItem newSerie = createTVShowItem(serieId);
			series.set(oldListPosition, newSerie);
			listView.post(updateListView);
			if (sortOption == SORT_BY_LAST_UNSEEN) {
				final int padding = (int) (6 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
				listView.post(new Runnable() {
					public void run() {
						int pos = seriesAdapter.getPosition(newSerie);
						if (pos != oldListPosition) {
							listView.setSelection(pos);
							if (0 < pos && pos < listView.getCount() - 5)
								listView.smoothScrollBy(-padding, 500);
						}
					}
				});
			}
			Toast.makeText(getApplicationContext(), serie.getName() +" "+ episodeMarked +" "+ getString(R.string.messages_marked_seen), Toast.LENGTH_SHORT).show();
			undo.add(new String[] {serieId, nextEpisode, serie.getName()});
			return true;
		}
		return false;
	}
	
	private void markLastEpUnseen() {
		String[] episodeInfo = undo.get(undo.size()-1);
		String serieId = episodeInfo[0];
		String episodeId = episodeInfo[1];
		String serieName = episodeInfo[2];
		String episodeMarked = db.updateUnwatchedEpisode(serieId, episodeId);
		int oldListPosition = -1;
		for (int i = 0; i < series.size(); i++) {
			if (series.get(i).getSerieId().equals(serieId)) {
				oldListPosition = i;
				break;
			}
		}
		final TVShowItem newSerie = createTVShowItem(serieId);
		series.set(oldListPosition, newSerie);
		listView.post(updateListView);
		undo.remove(undo.size()-1);
		Toast.makeText(getApplicationContext(), serieName +" "+ episodeMarked +" "+ getString(R.string.messages_marked_unseen), Toast.LENGTH_SHORT).show();
	}
	
	private void showDetails(String serieId) {
		Intent viewSerie = new Intent(droidseries.this, ViewSerie.class);
		viewSerie.putExtra("serieId", serieId);
		startActivity(viewSerie);
	}

	private void IMDbDetails(String serieId) {
		String query = "SELECT imdbId, serieName FROM series WHERE id = '" + serieId + "'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			String imdbId = c.getString(0);
			String serieName = c.getString(1);
			c.close();
			Intent imdb;
			if (imdbId.equalsIgnoreCase("null") || imdbId.equals("")) {
				imdb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.imdb.com/find?q=" + serieName));
			} else {
				imdb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.imdb.com/title/"+ imdbId));
			}
			startActivity(imdb);
		}
	}
	
	private void updateSerie(String serieId, int pos) {
		final String id = serieId;
		final int oldListPosition = pos;
		if (utils.isNetworkAvailable(droidseries.this)) {
			Runnable updateserierun = new Runnable() {
				public void run() {
					theTVDB = new TheTVDB("8AC675886350B3C3");
					if (theTVDB.getMirror() != null) {
						// Log.d(TAG, "Running getSerie - " + id);
						Serie sToUpdate = theTVDB.getSerie(id, getString(R.string.lang_code));
						// Log.d(TAG, "Running db.updateserie");
						toastMessage = getString(R.string.messages_title_updating_db) + " - " + sToUpdate.getSerieName();
						runOnUiThread(changeMessage);
						db.updateSerie(sToUpdate, lastSeasonOption == UPDATE_LAST_SEASON_ONLY);
						updatePosterThumb(id, sToUpdate);
					} else {
						Looper.prepare();
						Toast.makeText(getApplicationContext(), "Could not connect to TheTVDb", Toast.LENGTH_LONG).show();
						Looper.loop();
					}
					if (!bUpdateShowTh) {
						m_ProgressDialog.dismiss();
						bUpdateShowTh = false;
					}
					theTVDB = null;
					final TVShowItem newSerie = createTVShowItem(id);
					series.set(oldListPosition, newSerie);
					listView.post(updateListView);
					if (sortOption == SORT_BY_LAST_UNSEEN) {
						final int padding = (int) (6 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
						listView.post(new Runnable() {
							public void run() {
								int pos = seriesAdapter.getPosition(newSerie);
								if (pos != oldListPosition) {
									listView.setSelection(pos);
									if (0 < pos && pos < listView.getCount() - 5)
										listView.smoothScrollBy(-padding, 500);
								}
							}
						});
					}
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
	
	public void updatePosterThumb(String serieId, Serie sToUpdate) {
		Cursor c = droidseries.db.Query("SELECT posterInCache, poster, posterThumb FROM series WHERE id='"+ serieId +"'");
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			String posterInCache = c.getString(0);
			String poster = c.getString(1);
			String posterThumbPath = c.getString(2);
			URL posterURL = null;
			if (!posterInCache.equals("true") || !(new File(posterThumbPath).exists())) {
				poster = sToUpdate.getPoster();
				try {
					posterURL = new URL(poster);
					new File(posterThumbPath).delete();
					posterThumbPath = getApplicationContext().getFilesDir().getAbsolutePath() +"/thumbs"+ posterURL.getFile().toString();
				} catch (MalformedURLException e) {
					Log.e(TAG, "Show "+ serieId +" doesn't have poster URL");
					e.printStackTrace();
					return;
				}
				File posterThumbFile = new File(posterThumbPath);
				try {
					FileUtils.copyURLToFile(posterURL, posterThumbFile);
				} catch (IOException e) {
					Log.e(TAG, "Could not download poster: "+ posterURL);
					e.printStackTrace();
					return;
				}
				Bitmap posterThumb = BitmapFactory.decodeFile(posterThumbPath);
				if (posterThumb == null) {
					Log.e(TAG, "Corrupt or unknown poster file type:"+ posterThumbPath);
					return;
				}
				int width = getWindowManager().getDefaultDisplay().getWidth();
				int height = getWindowManager().getDefaultDisplay().getHeight();
				int newHeight = (int) ((height > width ? height : width) * 0.265);
				int newWidth = (int) (1.0 * posterThumb.getWidth() / posterThumb.getHeight() * newHeight);
				Bitmap resizedBitmap = Bitmap.createScaledBitmap(posterThumb, newWidth, newHeight, true);
				OutputStream fOut = null;
				try {
					fOut = new FileOutputStream(posterThumbFile, false);
					resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
					fOut.flush();
					fOut.close();
					db.execQuery("UPDATE series SET posterInCache='true', poster='"+ poster
						+"', posterThumb='"+ posterThumbPath +"' WHERE id='"+ serieId +"'");
					Log.d(TAG, "Updated poster thumb for "+ sToUpdate.getSerieName());
				} catch (FileNotFoundException e) {
					Log.e(TAG, "File not found:"+ posterThumbFile);
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				posterThumb.recycle();
				resizedBitmap.recycle();
				System.gc();
				posterThumb = null;
				resizedBitmap = null;
			}
		}
		c.close();
	}

	private Runnable changeMessage = new Runnable() {
		public void run() {
			m_ProgressDialog.setMessage(toastMessage);
		}
	};
	
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
							db.updateSerie(sToUpdate, lastSeasonOption == UPDATE_LAST_SEASON_ONLY);
							updatePosterThumb(series.get(i).getSerieId(), sToUpdate);
						} catch (Exception e) {
						}
						if (!bUpdateAllShowsTh) {
							updateAllSeriesPD.incrementProgressBy(1);
						}
					}
					if (!bUpdateAllShowsTh) {
						getSeries();
						updateAllSeriesPD.dismiss();
						bUpdateAllShowsTh = false;
					}
					theTVDB = null;
					listView.post(updateListView);
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
	
	private static TVShowItem createTVShowItem(String serieId) {
		String name = "", tmpPoster = "", showStatus = "", tmpNextEpisode = "", nextEpisode = "", tmpNextAir = "";
		int tmpStatus = 0, seasonCount = 0, unwatched = 0, unwatchedAired = 0;
		Date nextAir = null;
		String query = "SELECT serieName, posterThumb, status, passiveStatus, seasonCount, unwatchedAired, unwatched, nextEpisode, nextAir FROM series WHERE id = '" + serieId + "'";
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
		boolean status = (tmpStatus == 1);
		TVShowItem tvsi = new TVShowItem(serieId, tmpPoster, null, name, seasonCount, nextEpisode, nextAir, unwatchedAired, unwatched, status, showStatus);
		return tvsi;
	}

	private void getSeries() {
		if (series != null) series.clear();
		try {
			List<String> serieIds;
			if (sortOption == SORT_BY_LAST_UNSEEN) {
				serieIds = db.getSeriesByNextEpisode();
			} else {
				serieIds = db.getSeriesByName();
			}
			for (int i = 0; i < serieIds.size(); i++) {
				String serieId = serieIds.get(i);
				int status = 0;
				if (filterOption == FILTER_ENABLED) {
					status = db.getSerieStatus(serieId);
				}
				if (status == 0) {
					TVShowItem tvsi = createTVShowItem(serieId);
					series.add(tvsi);
				}
			}
			listView.post(updateListView);
		} catch (Exception e) {
			Log.e(TAG, "Error populating TVShowItems or no shows added yet");
			e.printStackTrace();
		}
	}
	
	public static Runnable updateListView = new Runnable() {
		public void run() {
			seriesAdapter.notifyDataSetChanged();
			if (series != null && series.size() > 0) {
				for (int i = 0; i < series.size(); i++) {
					if (!series.get(i).equals(seriesAdapter.getItem(i))) {
						seriesAdapter.add(series.get(i));
					} else {
						TVShowItem tmpTVSI = seriesAdapter.getItem(i);
						tmpTVSI.setUnwatched(series.get(i).getUnwatched());
						tmpTVSI.setUnwatchedAired(series.get(i).getUnwatchedAired());
						tmpTVSI.setNextEpisode(series.get(i).getNextEpisode());
						tmpTVSI.setNextAir(series.get(i).getNextAir());
					}
				}
			}
			Comparator<TVShowItem> comperator = new Comparator<TVShowItem>() {
				public int compare(TVShowItem object1, TVShowItem object2) {
					if (sortOption == SORT_BY_LAST_UNSEEN) {
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
			seriesAdapter.sort(comperator);
			seriesAdapter.notifyDataSetChanged();
		}
	};
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = sharedPrefs.edit();
		ed.putInt(SORT_PREF_NAME, sortOption);
		ed.putInt(FILTER_PREF_NAME, filterOption);
		ed.putInt(LAST_SEASON_PREF_NAME, lastSeasonOption);
		ed.putBoolean(INCLUDE_SPECIALS_NAME, includeSpecialsOption);
		ed.putBoolean(FULL_LINE_CHECK_NAME, fullLineCheckOption);
		ed.commit();
		if (updateShowTh != null) {
			if (updateShowTh.isAlive()) {
				bUpdateShowTh = true;
			}
		}
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
		listView.post(updateListView);
		if (sortOption == SORT_BY_LAST_UNSEEN) {
			final int padding = (int) (6 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
			final int lastPosition = backFromSeasonPosition;
			listView.post(new Runnable() {
				public void run() {
					int pos = seriesAdapter.getPosition(newSerie);
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
		asyncInfo = new AsyncInfo();
		asyncInfo.execute();
	}
	
	private class AsyncInfo extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			for (TVShowItem serie : series) {
				String serieId = serie.getSerieId();
				int unwatched = db.getEPUnwatched(serieId);
				int unwatchedAired = db.getEPUnwatchedAired(serieId);
				if (unwatched != serie.getUnwatched() || unwatchedAired != serie.getUnwatchedAired()) {
					serie.setUnwatched(unwatched);
					serie.setUnwatchedAired(unwatchedAired);
					listView.post(updateListView);
					db.execQuery("UPDATE series SET unwatched="+ unwatched +", unwatchedAired="+ unwatchedAired +" WHERE id="+ serieId);
				}
			}
			return null;
		}
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
		
	public String translateStatus(String statusValue) {
		if (statusValue.equalsIgnoreCase("Continuing")) {
			return getString(R.string.showstatus_continuing);
		} else if (statusValue.equalsIgnoreCase("Ended")) {
			return getString(R.string.showstatus_ended);
		} else {
			return statusValue;
		}
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
				convertView = vi.inflate(R.layout.row, parent, false);
				holder = new ViewHolder();
				holder.sn = (TextView) convertView.findViewById(R.id.seriename);
				holder.si = (TextView) convertView.findViewById(R.id.serieinfo);
				holder.sne = (TextView) convertView.findViewById(R.id.serienextepisode);
				holder.icon = (IconView) convertView.findViewById(R.id.serieicon);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.icon.setOnClickListener(null);
			}
			TVShowItem serie = items.get(position);
			int nunwatched = serie.getUnwatched();
			int nunwatchedAired = serie.getUnwatchedAired();
			String ended = (serie.getShowStatus().equalsIgnoreCase("Ended") ? " †" : "");
			if (holder.sn != null) {
				if (serie.getPassiveStatus()) {
					holder.sn.setText("[" + serie.getName() + "]"+ ended);
				} else {
					holder.sn.setText(serie.getName() + ended);
				}
			}
			if (holder.si != null) {
				String siText = "";
				int sNumber = serie.getSNumber();
				if (sNumber == 1) {
					siText = sNumber +" "+ getString(R.string.messages_season);
				} else {
					siText = sNumber +" "+ getString(R.string.messages_seasons);
				}
				String unwatched = "";
				if (nunwatched == 0) {
					unwatched = getString(R.string.messages_no_new_eps);
					if (!serie.getShowStatus().equalsIgnoreCase("null"))
						unwatched += " ("+ translateStatus(serie.getShowStatus()) +")";
					holder.si.setEnabled(false);
				} else {
					unwatched = nunwatched +" "+ (nunwatched > 1 ? getString(R.string.messages_new_episodes) : getString(R.string.messages_new_episode)) +" ";
					if (nunwatchedAired > 0) {
						unwatched = (nunwatchedAired == nunwatched ? "" : nunwatchedAired +" "+ getString(R.string.messages_of) +" ") + unwatched + getString(R.string.messages_ep_aired) + (nunwatchedAired == nunwatched && ended.isEmpty() ? " \u00b7" : "");
						holder.si.setEnabled(true);
					} else {
						unwatched += getString(R.string.messages_to_be_aired);
						holder.si.setEnabled(false);
					}
				}
				holder.si.setText(siText +" | "+ unwatched);
			}
			if (holder.sne != null) {
				if (nunwatched > 0) {
					holder.sne.setText(getString(R.string.messages_next_episode) +" "+ serie.getNextEpisode());
					holder.sne.setVisibility(View.VISIBLE);
					if (nunwatchedAired > 0) {
						holder.sne.setEnabled(true);
					} else {
						holder.sne.setEnabled(false);
					}
				} else {
					holder.sne.setText("");
				}
			}
			if (holder.icon != null) {
				Drawable icon = serie.getDIcon();
				if (icon == null)
					icon = Drawable.createFromPath(serie.getIcon());
				if (icon == null) {
					holder.icon.setImageResource(R.drawable.noposter);
				} else {
					holder.icon.setImageDrawable(icon);
					serie.setDIcon(icon);
				}
				holder.icon.setOnClickListener(detailsListener);
				holder.icon.setOnLongClickListener(IMDbListener);
			}
			return convertView;
		}
	}
	static class ViewHolder
	{
		TextView sn;
		TextView si;
		TextView sne;
		IconView icon;
	}
	private OnClickListener detailsListener = new OnClickListener() {
		public void onClick(View v) {
	        final int position = getListView().getPositionForView(v);
	        if (position != ListView.INVALID_POSITION) {
				showDetails(series.get(position).getSerieId());
			}
		}
	};
	private OnLongClickListener IMDbListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
	        final int position = getListView().getPositionForView(v);
	        if (position != ListView.INVALID_POSITION) {
	        	IMDbDetails(series.get(position).getSerieId());
	        }
			return true;
		}
	};
}