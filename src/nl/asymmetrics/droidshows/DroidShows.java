package nl.asymmetrics.droidshows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.thetvdb.TheTVDB;
import nl.asymmetrics.droidshows.thetvdb.model.Serie;
import nl.asymmetrics.droidshows.thetvdb.model.TVShowItem;
import nl.asymmetrics.droidshows.ui.IconView;
import nl.asymmetrics.droidshows.ui.SerieSeasons;
import nl.asymmetrics.droidshows.ui.ViewEpisode;
import nl.asymmetrics.droidshows.ui.ViewSerie;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import nl.asymmetrics.droidshows.utils.Update;
import nl.asymmetrics.droidshows.utils.Utils;
import nl.asymmetrics.droidshows.utils.SQLiteStore.NextEpisode;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class DroidShows extends ListActivity
{
	/* Menus */
	private static final int TOGGLE_ARCHIVE_MENU_ITEM = Menu.FIRST;
	private static final int TOGGLE_EXCLUDE_SEEN_MENU_ITEM = TOGGLE_ARCHIVE_MENU_ITEM + 1;
	private static final int SORT_MENU_ITEM = TOGGLE_EXCLUDE_SEEN_MENU_ITEM + 1;
	private static final int SEARCH_MENU_ITEM = SORT_MENU_ITEM + 1;
	private static final int UNDO_MENU_ITEM = SEARCH_MENU_ITEM + 1;
	private static final int ADD_SERIE_MENU_ITEM = UNDO_MENU_ITEM + 1;
	private static final int UPDATEALL_MENU_ITEM = ADD_SERIE_MENU_ITEM + 1;
	private static final int OPTIONS_MENU_ITEM = UPDATEALL_MENU_ITEM + 1;
	private static final int EXIT_MENU_ITEM = OPTIONS_MENU_ITEM + 1;
	/* Context Menus */
	private static final int VIEW_SERIEDETAILS_CONTEXT = Menu.FIRST;
	private static final int VIEW_EPISODEDETAILS_CONTEXT = VIEW_SERIEDETAILS_CONTEXT + 1;
	private static final int EXT_RESOURCES_CONTEXT = VIEW_EPISODEDETAILS_CONTEXT + 1;
	private static final int MARK_NEXT_EPISODE_AS_SEEN_CONTEXT = EXT_RESOURCES_CONTEXT + 1;
	private static final int UPDATE_CONTEXT = MARK_NEXT_EPISODE_AS_SEEN_CONTEXT + 1;
	private static final int TOGGLE_ARCHIVED_CONTEXT = UPDATE_CONTEXT + 1;
	private static final int PIN_CONTEXT = TOGGLE_ARCHIVED_CONTEXT + 1;
	private static final int DELETE_CONTEXT = PIN_CONTEXT + 1;
	private static AlertDialog m_AlertDlg;
	private static ProgressDialog m_ProgressDialog = null;
	private static ProgressDialog updateAllSeriesPD = null;
	public static SeriesAdapter seriesAdapter;
	private static ListView listView;
	private static String backFromSeasonSerieId;
	private static TheTVDB theTVDB;
	private Utils utils = new Utils();
	private Update updateDS;
	private static final String PREF_NAME = "DroidShowsPref";
	private SharedPreferences sharedPrefs;
	private static final String AUTO_BACKUP_PREF_NAME = "auto_backup";
	private static boolean autoBackupOption;
	private static final String SORT_PREF_NAME = "sort";
	private static final int SORT_BY_NAME = 0;
	private static final int SORT_BY_LAST_UNSEEN = 1;
	private static int sortOption;
	private static final String EXCLUDE_SEEN_PREF_NAME = "exclude_seen";
	private static boolean excludeSeen;
	private static final String LATEST_SEASON_PREF_NAME = "last_season";
	private static final int UPDATE_ALL_SEASONS = 0;
	private static final int UPDATE_LATEST_SEASON_ONLY = 1;
	private static int latestSeasonOption;
	private static final String INCLUDE_SPECIALS_NAME = "include_specials";
	public static boolean includeSpecialsOption;
	private static final String FULL_LINE_CHECK_NAME = "full_line";
	public static boolean fullLineCheckOption;
	private static final String SWITCH_SWIPE_DIRECTION = "switch_swipe_direction";
	public static boolean switchSwipeDirection;
	private static final String LAST_STATS_UPDATE_NAME = "last_stats_update";
	private static String lastStatsUpdateCurrent;
	private static final String LAST_STATS_UPDATE_ARCHIVE_NAME = "last_stats_update_archive";
	private static String lastStatsUpdateArchive;
	private static final String LANGUAGE_CODE_NAME = "language";
	private static final String SHOW_NEXT_AIRING = "show_next_airing";
	public static boolean showNextAiring;
	private static final String USE_MIRROR = "use_mirror";
	public static boolean useMirror;
	public static String langCode;
	private static final String PINNED_SHOWS_NAME = "pinned_shows";
	private static List<String> pinnedShows = new ArrayList<String>();
	public static Thread deleteTh = null;
	public static Thread updateShowTh = null;
	public static Thread updateAllShowsTh = null;
	private String dialogMsg;
	public static SQLiteStore db;
	public static List<TVShowItem> series;
	private static List<String[]> undo = new ArrayList<String[]>();
	private SwipeDetect swipeDetect = new SwipeDetect();
	private static AsyncInfo asyncInfo;
	private static EditText searchV;
	private InputMethodManager keyboard;
	private int padding;
	public static int showArchive;
	private Vibrator vib = null;
	private TVShowItem lastSerie;
	private static View main;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isTaskRoot()) {	// Prevent multiple instances: http://stackoverflow.com/a/11042163
			final Intent intent = getIntent();
			if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
				finish();
				return;
			}
		}
		setContentView(R.layout.main);
		main = findViewById(R.id.main);
		db = SQLiteStore.getInstance(this);

		updateDS = new Update(db);
		if(updateDS.updateDroidShows())
			db.updateShowStats();

		// Preferences
		sharedPrefs = getSharedPreferences(PREF_NAME, 0);
		autoBackupOption = sharedPrefs.getBoolean(AUTO_BACKUP_PREF_NAME, false);
		sortOption = sharedPrefs.getInt(SORT_PREF_NAME, SORT_BY_NAME);
		excludeSeen = sharedPrefs.getBoolean(EXCLUDE_SEEN_PREF_NAME, false);
		latestSeasonOption = sharedPrefs.getInt(LATEST_SEASON_PREF_NAME, UPDATE_LATEST_SEASON_ONLY);
		includeSpecialsOption = sharedPrefs.getBoolean(INCLUDE_SPECIALS_NAME, false);
		fullLineCheckOption = sharedPrefs.getBoolean(FULL_LINE_CHECK_NAME, false);
		switchSwipeDirection = sharedPrefs.getBoolean(SWITCH_SWIPE_DIRECTION, false);
		lastStatsUpdateCurrent = sharedPrefs.getString(LAST_STATS_UPDATE_NAME, "");
		lastStatsUpdateArchive = sharedPrefs.getString(LAST_STATS_UPDATE_ARCHIVE_NAME, "");
		langCode = sharedPrefs.getString(LANGUAGE_CODE_NAME, getString(R.string.lang_code));
		showNextAiring = sharedPrefs.getBoolean(SHOW_NEXT_AIRING, false);
		useMirror = sharedPrefs.getBoolean(USE_MIRROR, false);
		String pinnedShowsStr = sharedPrefs.getString(PINNED_SHOWS_NAME, "");
		if (!pinnedShowsStr.isEmpty())
			pinnedShows = new ArrayList<String>(Arrays.asList(pinnedShowsStr.replace("[", "").replace("]", "").split(", ")));
		series = new ArrayList<TVShowItem>();
		seriesAdapter = new SeriesAdapter(this, R.layout.row, series);
		setListAdapter(seriesAdapter);
		listView = getListView();
		listView.setDivider(null);
		if (savedInstanceState != null) {
			showArchive = savedInstanceState.getInt("showArchive");
			getSeries((savedInstanceState.getBoolean("searching") ? 2 : showArchive));
		} else {
			getSeries(showArchive);
		}
		registerForContextMenu(listView);
		listView.setOnTouchListener(swipeDetect);
		searchV = (EditText) findViewById(R.id.search_text);
		searchV.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				seriesAdapter.getFilter().filter(s);
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
		});
		keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		padding = (int) (6 * (getApplicationContext().getResources().getDisplayMetrics().densityDpi / 160f));
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}
	
	private void setFastScroll() {
		listView.setVerticalScrollBarEnabled(!excludeSeen);
		listView.setFastScrollEnabled(!excludeSeen);
		listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		if (!excludeSeen) {
			if (seriesAdapter.getCount() > 20) {
				try {	// http://stackoverflow.com/a/26447004
					Drawable thumb = getResources().getDrawable(R.drawable.thumb);
					String fieldName = "mFastScroller";
					if (android.os.Build.VERSION.SDK_INT >= 22)	// 22 = Lollipop
			            fieldName = "mFastScroll";
	
					java.lang.reflect.Field fieldFastScroller = AbsListView.class.getDeclaredField(fieldName);
					fieldFastScroller.setAccessible(true);
					listView.setFastScrollEnabled(true);
					Object thisFastScroller = fieldFastScroller.get(listView);
					java.lang.reflect.Field fieldToChange;
	
					if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
						fieldToChange = fieldFastScroller.getType().getDeclaredField("mThumbImage");
						fieldToChange.setAccessible(true);
						ImageView iv = (ImageView) fieldToChange.get(thisFastScroller);
						fieldToChange.set(thisFastScroller, iv);
						iv.setMinimumWidth(thumb.getIntrinsicWidth());	//IS//THIS//NECESSARY//?//
						iv.setMaxWidth(thumb.getIntrinsicWidth());	//IS//THIS//NECESSARY//?//
						iv.setImageDrawable(thumb);
	
						fieldToChange = fieldFastScroller.getType().getDeclaredField("mTrackImage");
						fieldToChange.setAccessible(true);
						iv = (ImageView) fieldToChange.get(thisFastScroller);
						fieldToChange.set(thisFastScroller, iv);
						iv.setImageDrawable(null);	// getResources().getDrawable(R.drawable.div)
					} else {
						fieldToChange = fieldFastScroller.getType().getDeclaredField("mThumbDrawable");
						fieldToChange.setAccessible(true);
						fieldToChange.set(thisFastScroller, thumb);
	
						fieldToChange = fieldFastScroller.getType().getDeclaredField("mThumbW");
						fieldToChange.setAccessible(true);
						fieldToChange.setInt(thisFastScroller, thumb.getIntrinsicWidth());
	
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
							fieldToChange = fieldFastScroller.getType().getDeclaredField("mTrackDrawable");
							fieldToChange.setAccessible(true);
							fieldToChange.set(thisFastScroller, null);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/* Options Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, TOGGLE_ARCHIVE_MENU_ITEM, 0, "");
		menu.add(0, TOGGLE_EXCLUDE_SEEN_MENU_ITEM, 0, "").setIcon(android.R.drawable.ic_menu_view);
		menu.add(0, SORT_MENU_ITEM, 0, "");
		menu.add(0, SEARCH_MENU_ITEM, 0, getString(R.string.menu_search)).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, UNDO_MENU_ITEM, 0, getString(R.string.menu_undo)).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, UPDATEALL_MENU_ITEM, 0, getString(R.string.menu_update)).setIcon(android.R.drawable.ic_menu_upload);
		menu.add(0, OPTIONS_MENU_ITEM, 0, getString(R.string.menu_about)).setIcon(android.R.drawable.ic_menu_manage);
		menu.add(0, EXIT_MENU_ITEM, 0, getString(R.string.menu_exit)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
			.setEnabled(!searching());
		menu.findItem(TOGGLE_EXCLUDE_SEEN_MENU_ITEM)
			.setEnabled(!searching());

		if (showArchive == 1) {
			menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_today)
				.setTitle(R.string.menu_show_current);
		} else {
			menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_recent_history)
				.setTitle(R.string.menu_show_archive);
		}
		if (excludeSeen) {
			menu.findItem(TOGGLE_EXCLUDE_SEEN_MENU_ITEM)
				.setTitle(R.string.menu_include_seen);
		} else {
			menu.findItem(TOGGLE_EXCLUDE_SEEN_MENU_ITEM)
				.setTitle(R.string.menu_exclude_seen);
		}
		if (sortOption == SORT_BY_LAST_UNSEEN) {
			menu.findItem(SORT_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_sort_alphabetically)
				.setTitle(R.string.menu_sort_az);
		} else {
			menu.findItem(SORT_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_sort_by_size)
				.setTitle(R.string.menu_sort_last_unseen);
		}
		if (undo.size() > 0) {
			menu.findItem(UNDO_MENU_ITEM).setVisible(true);
		} else {
			menu.findItem(UNDO_MENU_ITEM).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case ADD_SERIE_MENU_ITEM :
				super.onSearchRequested();
				break;
			case SEARCH_MENU_ITEM :
				onSearchRequested();
				break;
			case TOGGLE_ARCHIVE_MENU_ITEM :
				toggleArchive();
				break;
			case SORT_MENU_ITEM :
				toggleSort();
				break;
			case TOGGLE_EXCLUDE_SEEN_MENU_ITEM :
				toggleExcludeSeen();
				break;
			case UPDATEALL_MENU_ITEM :
				updateAllSeries();
				break;
			case OPTIONS_MENU_ITEM :
				aboutDialog();
				break;
			case UNDO_MENU_ITEM :
				markLastEpUnseen();
				break;
			case EXIT_MENU_ITEM :
				onPause();	// save options
				backup(true);
				db.close();
				this.finish();
				System.gc();
				System.exit(0);	// kill process
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void toggleArchive() {
		showArchive = (showArchive + 1) % 2;
		getSeries(showArchive);
		listView.setSelection(0);
	}

	private void toggleSort() {
		sortOption ^= 1;
		listView.post(updateListView);
	}
	
	private void toggleExcludeSeen() {
		excludeSeen ^= true;
		listView.post(updateListView);
		setFastScroll();
	}

	private void aboutDialog() {
		if (m_AlertDlg != null) {
			m_AlertDlg.dismiss();
		}
		View about = View.inflate(this, R.layout.alert_about, null);
		TextView changelog = (TextView) about.findViewById(R.id.copyright);
		try {
			changelog.setText(getString(R.string.copyright)
				.replace("{v}", getPackageManager().getPackageInfo(getPackageName(), 0).versionName)
				.replace("{y}", Calendar.getInstance().get(Calendar.YEAR) +""));
			changelog.setTextColor(changelog.getTextColors().getDefaultColor());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		TextView changeLangB = (TextView) about.findViewById(R.id.change_language);
		changeLangB.setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
		CheckBox autoBackupCheckbox = (CheckBox) about.findViewById(R.id.auto_backup);
		autoBackupCheckbox.setChecked(autoBackupOption);
		CheckBox latestSeasonCheckbox = (CheckBox) about.findViewById(R.id.latest_season);
		latestSeasonCheckbox.setChecked(latestSeasonOption == UPDATE_LATEST_SEASON_ONLY);
		CheckBox includeSpecialsCheckbox = (CheckBox) about.findViewById(R.id.include_specials);
		includeSpecialsCheckbox.setChecked(includeSpecialsOption);
		CheckBox fullLineCheckbox = (CheckBox) about.findViewById(R.id.full_line_check);
		fullLineCheckbox.setChecked(fullLineCheckOption);
		CheckBox switchSwipeDirectionBox = (CheckBox) about.findViewById(R.id.switch_swipe_direction);
		switchSwipeDirectionBox.setChecked(switchSwipeDirection);
		CheckBox showNextAiringBox = (CheckBox) about.findViewById(R.id.show_next_airing);
		showNextAiringBox.setChecked(showNextAiring);
		CheckBox useMirrorBox = (CheckBox) about.findViewById(R.id.use_mirror);
		useMirrorBox.setChecked(useMirror);
		m_AlertDlg = new AlertDialog.Builder(this)
			.setView(about)
			.setTitle(R.string.layout_app_name).setIcon(R.drawable.icon)
			.setPositiveButton(getString(R.string.dialog_backup), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					backup(false);
					asyncInfo = new AsyncInfo();
					asyncInfo.execute();
				}
			})
			.setNegativeButton(getString(R.string.dialog_restore), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					new AlertDialog.Builder(DroidShows.this)
					.setTitle(R.string.dialog_restore)
					.setMessage(R.string.dialog_restore_now)
					.setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							restore();
						}
					})
					.setNegativeButton(R.string.dialog_Cancel, null)
					.show();
				}
			})
			.show();
	}
	
	public void dialogOptions(View v) {
		switch(v.getId()) {
			case R.id.auto_backup:
				autoBackupOption ^= true;
				break;
			case R.id.latest_season:
				latestSeasonOption ^= 1;
				break;
			case R.id.include_specials:
				includeSpecialsOption ^= true;
				updateShowStats();
				break;
			case R.id.full_line_check:
				fullLineCheckOption ^= true;
				break;
			case R.id.switch_swipe_direction:
				switchSwipeDirection ^= true;
				break;
			case R.id.show_next_airing:
				showNextAiring ^= true;
				updateShowStats();
				break;
			case R.id.use_mirror:
				useMirror ^= true;
				break;
			case R.id.change_language:
				AlertDialog.Builder changeLang = new AlertDialog.Builder(this);
				changeLang.setItems(R.array.languages, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						langCode = getResources().getStringArray(R.array.langcodes)[item];
						TextView changeLangB = (TextView) m_AlertDlg.findViewById(R.id.change_language);
						changeLangB.setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
					}
				});
				changeLang.show();
			break;
		}
	}
	
	private void updateShowStats() {
		Runnable updateShowStats = new Runnable() {
			public void run() {
				db.updateShowStats();
				listView.post(new Runnable() {
					public void run() {getSeries((searching() ? 2 : showArchive));}
				});
			}
		};
		Thread updateShowStatsTh = new Thread(updateShowStats);
		updateShowStatsTh.start();
	}
	
	private void backup(boolean auto) {
		File source = new File(getApplicationInfo().dataDir +"/databases/DroidShows.db");
		File destination = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db");
		if (auto && (!autoBackupOption || 
				new SimpleDateFormat("yyyy-MM-dd")
					.format(destination.lastModified()).equals(lastStatsUpdateCurrent) ||
				source.lastModified() == destination.lastModified()))
			return;
		if (destination.exists()) {
			File previous0 = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db0");
			if (previous0.exists()) {
				File previous1 = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db1");
				if (previous1.exists())
					previous1.delete();
				previous0.renameTo(previous1);
			}
			destination.renameTo(previous0);
		}
		File folder = new File(Environment.getExternalStorageDirectory() +"/DroidShows");
		if (!folder.isDirectory())
			folder.mkdir();
		int toastTxt = R.string.dialog_backup_done;
		try {
			copy(source, destination);
		} catch (IOException e) {
			toastTxt = R.string.dialog_backup_failed;
			e.printStackTrace();
		}
		if (!auto || toastTxt == R.string.dialog_backup_failed)
			Toast.makeText(getApplicationContext(), toastTxt, Toast.LENGTH_LONG).show();
	}
	
	private void restore() {
		int toastTxt = R.string.dialog_restore_done;
		File source = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db");
		if (!source.exists()) source = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db0");
		if (!source.exists()) source = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db1");
		if (!source.exists()) source = new File(Environment.getExternalStorageDirectory(), "droidseries.db");
		if (source.exists()) {
			File destination = new File(getApplicationInfo().dataDir +"/databases", "DroidShows.db");
			try {
				copy(source, destination);
				updateDS.updateDroidShows();
				File thumbs[] = new File(getApplicationContext().getFilesDir().getAbsolutePath() +"/thumbs/banners/posters").listFiles();
				if (thumbs != null)
					for (File thumb : thumbs)
						thumb.delete();
				for (File file : new File(getApplicationInfo().dataDir +"/databases").listFiles())
				    if (!file.getName().equalsIgnoreCase("DroidShows.db")) file.delete();
				if (showArchive == 1)
					setTitle(getString(R.string.layout_app_name));
				getSeries(2);	// Get archived and current shows
				updateAllSeries();
				undo.clear();
			} catch (IOException e) {
				toastTxt = R.string.dialog_restore_failed;
				e.printStackTrace();
			}
		} else {
			toastTxt = R.string.dialog_restore_notfound;
		}
		Toast.makeText(getApplicationContext(), toastTxt, Toast.LENGTH_LONG).show();
	}
		
	private void copy(File source, File destination) throws IOException {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			asyncInfo.cancel(true);
			db.close();
			FileChannel sourceCh = null, destinationCh = null;
			try {
				sourceCh = new FileInputStream(source).getChannel();
				if (destination.exists()) destination.delete();
				destination.createNewFile();
				destinationCh = new FileOutputStream(destination).getChannel();
				destinationCh.transferFrom(sourceCh, 0, sourceCh.size());
				destination.setLastModified(source.lastModified());
			} finally {
				if (sourceCh != null) {
					sourceCh.close();
				}
				if (destinationCh != null) {
					destinationCh.close();
				}
			}
			db.openDataBase();
		}
	}

	/* context menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, VIEW_SERIEDETAILS_CONTEXT, 0, getString(R.string.menu_context_view_serie_details));
		menu.add(0, VIEW_EPISODEDETAILS_CONTEXT, 0, getString(R.string.messsages_view_ep_details));
		menu.add(0, EXT_RESOURCES_CONTEXT, 0, getString(R.string.menu_context_ext_resources));
		menu.add(0, MARK_NEXT_EPISODE_AS_SEEN_CONTEXT, 0, getString(R.string.menu_context_mark_next_episode_as_seen));
		menu.add(0, UPDATE_CONTEXT, 0, getString(R.string.menu_context_update));
		menu.add(0, TOGGLE_ARCHIVED_CONTEXT, 0, getString(R.string.menu_archive));
		menu.add(0, PIN_CONTEXT, 0, getString(R.string.menu_context_pin));
		menu.add(0, DELETE_CONTEXT, 0, getString(R.string.menu_context_delete));
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		if (seriesAdapter.getItem(info.position).getUnwatched() == 0) {
			menu.findItem(VIEW_EPISODEDETAILS_CONTEXT).setVisible(false);
			menu.findItem(MARK_NEXT_EPISODE_AS_SEEN_CONTEXT).setVisible(false);
		} else if (seriesAdapter.getItem(info.position).getUnwatchedAired() == 0)
	    	menu.findItem(MARK_NEXT_EPISODE_AS_SEEN_CONTEXT).setVisible(false);
	    if (seriesAdapter.getItem(info.position).getPassiveStatus())
	    	menu.findItem(TOGGLE_ARCHIVED_CONTEXT).setTitle(R.string.menu_unarchive);
	    if (pinnedShows.contains(seriesAdapter.getItem(info.position).getSerieId()))
	    	menu.findItem(PIN_CONTEXT).setTitle(R.string.menu_context_unpin);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
			case MARK_NEXT_EPISODE_AS_SEEN_CONTEXT :
				markNextEpSeen(info.position);
				return true;
			case VIEW_SERIEDETAILS_CONTEXT :
				showDetails(seriesAdapter.getItem(info.position).getSerieId());
				return true;
			case VIEW_EPISODEDETAILS_CONTEXT :
				episodeDetails(info.position);
				return true;
			case EXT_RESOURCES_CONTEXT :
				extResources(seriesAdapter.getItem(info.position).getExtResources(), info.position);
				return true;
			case UPDATE_CONTEXT :
				updateSerie(seriesAdapter.getItem(info.position).getSerieId(), info.position);
				return true;
			case TOGGLE_ARCHIVED_CONTEXT :
				asyncInfo.cancel(true);
				TVShowItem serie = seriesAdapter.getItem(info.position);
				boolean passiveStatus = serie.getPassiveStatus();
				db.updateSerieStatus(serie.getSerieId(), (passiveStatus ? 0 : 1));
				String message = serie.getName() +" "+
					(passiveStatus ? getString(R.string.messages_context_unarchived) : getString(R.string.messages_context_archived));
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
				if (!seriesAdapter.isFiltered)
					series.remove(serie);
				else
					serie.setPassiveStatus(!passiveStatus);
				listView.post(updateListView);
				asyncInfo = new AsyncInfo();
				asyncInfo.execute();
				return true;
			case PIN_CONTEXT :
				String serieId = seriesAdapter.getItem(info.position).getSerieId();
				if (pinnedShows.contains(serieId))
					pinnedShows.remove(serieId);
				else
					pinnedShows.add(serieId);
				listView.post(updateListView);
				return true;
			case DELETE_CONTEXT :
				asyncInfo.cancel(true);
				final int position = info.position;
				final Runnable deleteserie = new Runnable() {
					public void run() {
						TVShowItem serie = seriesAdapter.getItem(position);
						String sname = serie.getName();
						db.deleteSerie(serie.getSerieId());
						series.remove(series.indexOf(serie));
						listView.post(updateListView);
						Looper.prepare();	// Threads don't have a message loop
							Toast.makeText(getApplicationContext(), sname +" "+ getString(R.string.messages_deleted), Toast.LENGTH_LONG).show();
							asyncInfo = new AsyncInfo();
							asyncInfo.execute();
						Looper.loop();
					}
				};
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.dialog_title_delete)
					.setMessage(String.format(getString(R.string.dialog_delete), seriesAdapter.getItem(info.position).getName()))
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setCancelable(false)
					.setPositiveButton(getString(R.string.dialog_OK), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							deleteTh = new Thread(deleteserie);
							deleteTh.start();
							return;
						}
					})
					.setNegativeButton(getString(R.string.dialog_Cancel), new DialogInterface.OnClickListener() {
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
		keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
		if (swipeDetect.value == 1 && (seriesAdapter.getItem(position).getUnwatchedAired() > 0 ||
				 seriesAdapter.getItem(position).getNextAir() != null &&
				!seriesAdapter.getItem(position).getNextAir().after(Calendar.getInstance().getTime()))) {
			vib.vibrate(150);
			markNextEpSeen(position);
		} else if (swipeDetect.value == 0) {
			String serieId = seriesAdapter.getItem(position).getSerieId();
			backFromSeasonSerieId = serieId;
			Intent serieSeasons = new Intent(DroidShows.this, SerieSeasons.class);
			serieSeasons.putExtra("serieId", serieId);
			serieSeasons.putExtra("nextEpisode", seriesAdapter.getItem(position).getUnwatched() > 0);
			startActivity(serieSeasons);
		}
	}
	
	private void markNextEpSeen(int position) {
		TVShowItem serie = seriesAdapter.getItem(position);
		String serieId = serie.getSerieId();
		String nextEpisode = db.getNextEpisodeId(serieId, true);
		if (!nextEpisode.equals("-1")) {
			String episodeMarked = db.updateUnwatchedEpisode(serieId, nextEpisode);
			Toast.makeText(getApplicationContext(), serie.getName() +" "+ episodeMarked +" "+ getString(R.string.messages_marked_seen), Toast.LENGTH_SHORT).show();
			undo.add(new String[] {serieId, nextEpisode, serie.getName()});
			updateShowView(serie);
		}
	}
	
	private void markLastEpUnseen() {
		String[] episodeInfo = undo.get(undo.size()-1);
		String serieId = episodeInfo[0];
		String episodeId = episodeInfo[1];
		String serieName = episodeInfo[2];
		String episodeMarked = db.updateUnwatchedEpisode(serieId, episodeId);
		undo.remove(undo.size()-1);
		Toast.makeText(getApplicationContext(), serieName +" "+ episodeMarked +" "+ getString(R.string.messages_marked_unseen), Toast.LENGTH_SHORT).show();
		listView.post(updateShowView(serieId));
	}
	
	private Runnable updateShowView(final String serieId) {
		Runnable updateView = new Runnable(){
			public void run() {
				for (TVShowItem serie : series) {
					if (serie.getSerieId().equals(serieId)) {
						updateShowView(serie);
						break;
					}
				}
			}
		};
		return updateView;
	}
	
	private void updateShowView(final TVShowItem serie) {
		final int position = seriesAdapter.getPosition(serie);
		final TVShowItem newSerie = db.createTVShowItem(serie.getSerieId());
		lastSerie = newSerie;
		series.set(series.indexOf(serie), newSerie);
		listView.post(updateListView);
		listView.post(new Runnable() {
			public void run() {
				int newPosition = seriesAdapter.getPosition(newSerie);
				if (newPosition != position)
					listView.setSelection(newPosition);
				if (listView.getLastVisiblePosition() > newPosition)
					listView.smoothScrollBy(-padding, 400);
			}
		});
	}
	
	private void showDetails(String serieId) {
		Intent viewSerie = new Intent(DroidShows.this, ViewSerie.class);
		viewSerie.putExtra("serieId", serieId);
		startActivity(viewSerie);
	}
	
	private void episodeDetails(int position) {
		String serieId = seriesAdapter.getItem(position).getSerieId();
		String episodeId = db.getNextEpisodeId(serieId);
		if (!episodeId.equals("-1")) {
			backFromSeasonSerieId = serieId;
			Intent viewEpisode = new Intent(DroidShows.this, ViewEpisode.class);
			viewEpisode.putExtra("serieName", seriesAdapter.getItem(position).getName());
			viewEpisode.putExtra("serieId", serieId);
			viewEpisode.putExtra("episodeId", episodeId);
			startActivity(viewEpisode);
		}
	}
	
	private void WikiDetails(String serieName) {
		serieName = serieName.replaceAll(" \\(....\\)", "");
		Intent wiki;
		String wikiApp = null;
	    if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("org.wikipedia") != null)
	    	wikiApp = "org.wikipedia";
	    else if (getApplicationContext().getPackageManager().getLaunchIntentForPackage("org.wikipedia.beta") != null)
	    	wikiApp = "org.wikipedia.beta";
	    if (wikiApp == null) {
	    	String uri = "http://"+ langCode +".m.wikipedia.org/wiki/index.php?search="+ serieName;
	    	wiki = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
	    } else {
	    	wiki = new Intent(Intent.ACTION_SEND)
	    		.putExtra(Intent.EXTRA_TEXT, serieName)
	    		.setType("text/plain")
	    		.setPackage(wikiApp);
	    }
	    startActivity(wiki);
	}
	
	private void IMDbDetails(String serieId, String serieName, boolean viewNextEpisode) {
		String nextEpisode = (viewNextEpisode ? db.getNextEpisodeId(serieId) : "-1");
		String query;
		if (!nextEpisode.equals("-1"))
			query = "SELECT imdbId, episodeName FROM episodes WHERE id = '"+ nextEpisode +"' AND serieId='"+ serieId +"'";
		else
			query = "SELECT imdbId, serieName FROM series WHERE id = '" + serieId + "'";
		Cursor c = db.Query(query);
		c.moveToFirst();
		if (c != null && c.isFirst()) {
			String imdbId = c.getString(0);
		    if (!nextEpisode.equals("-1") && imdbId.equals(db.getSerieIMDbId(serieId)))	// Sometimes the given episode's IMDb id is that of the show's
		    	imdbId = "-1";	// So we want to search for the episode instead of go to the show's page 
		    String name = c.getString(1);
			c.close();
			String uri = "imdb:///";
			Intent testForApp = new Intent(Intent.ACTION_VIEW, Uri.parse("imdb:///find"));
			if (getApplicationContext().getPackageManager().resolveActivity(testForApp, 0) == null)
				uri = "http://m.imdb.com/";
			if (imdbId.startsWith("tt"))
				uri += "title/"+ imdbId;
			else
				uri += "find?q="+ (!nextEpisode.equals("-1") ? serieName.replaceAll(" \\(....\\)", "") +" " : "") + name;
			Intent imdb = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			startActivity(imdb);
		}
	}
	
	private void extResources(String extResourcesString, final int position) {
		if (extResourcesString.length() > 0) {
			String[] tmpResources = extResourcesString.trim().split("\\n");
			extResourcesString = "";
			for (int i = 0; i < tmpResources.length; i++) {
				String url = tmpResources[i].trim();
				if (url.length() > 3)
					extResourcesString += url +"\n";
			}
		}
		final String[] extResources = (getString(R.string.menu_context_view_imdb) +"\n"
				+ getString(R.string.menu_context_view_ep_imdb) +"\n"
				+ getString(R.string.menu_context_view_wiki) +"\n"
				+ extResourcesString
				+"\u2026").split("\\n");
		final EditText input = new EditText(this);
		final String extResourcesInput = extResourcesString;
		input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_VARIATION_URI);
		new AlertDialog.Builder(this)
		.setTitle(R.string.menu_context_ext_resources)
		.setItems(extResources, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				final TVShowItem serie = seriesAdapter.getItem(position);
				switch(item) {
					case 0 :
						IMDbDetails(serie.getSerieId(), serie.getName(), false);
						break;
					case 1 :
						IMDbDetails(serie.getSerieId(), serie.getName(), true);
						break;
					case 2 :
						WikiDetails(serie.getName());
						break;
					default :
						if (item == extResources.length-1) {
							input.setText(extResourcesInput);
							new AlertDialog.Builder(DroidShows.this)
								.setTitle(R.string.menu_context_ext_resources)
								.setView(input)
								.setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
										String resources = input.getText().toString().trim();
										serie.setExtResources(resources);
										db.updateExtResources(serie.getSerieId(), resources);
										return;
									}
								})
								.setNegativeButton(R.string.dialog_Cancel, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
										return;
									}
								})
							.show();
							if (extResourcesInput.length() == 0) {
								input.setText("tvshow.wikia.com\n*onlongpress-poster.openstarred.url\ntvshow.blogspot.com");
								input.selectAll();
							}
							input.requestFocus();
							keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
						} else {
							browseExtResource(extResources[item]);
						}
				}
			}
		})
		.show();
	}
	
	private void browseExtResource(String url) {
		url = url.trim();
		if (url.startsWith("*"))
			url = url.substring(1).trim();
		if (!url.startsWith("http"))
			url = "http://"+ url;
		Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browse);
	}
	
	private void updateSerie(final String serieId, int position) {
		if (!utils.isNetworkAvailable(DroidShows.this)) {
			Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
		} else {
			final String serieName = seriesAdapter.getItem(position).getName();
			Runnable updateserierun = new Runnable() {
				public void run() {
					if (theTVDB == null)
						theTVDB = new TheTVDB("8AC675886350B3C3", useMirror);
					Serie sToUpdate = theTVDB.getSerie(serieId, langCode);
					if (sToUpdate == null) {
						errorNotify(serieName);
						m_ProgressDialog.dismiss();
					} else {
						dialogMsg = getString(R.string.messages_title_updating_db) + " - " + sToUpdate.getSerieName();
						runOnUiThread(changeMessage);
						db.updateSerie(sToUpdate, latestSeasonOption == UPDATE_LATEST_SEASON_ONLY);
						updatePosterThumb(serieId, sToUpdate);
						m_ProgressDialog.dismiss();
						Looper.prepare();
							Toast.makeText(getApplicationContext(),
								serieName +" "+ getString(R.string.menu_context_updated),
								Toast.LENGTH_SHORT).show();
							listView.post(updateShowView(serieId));
						Looper.loop();
					}
				}
			};
			m_ProgressDialog = ProgressDialog.show(DroidShows.this, serieName, getString(R.string.messages_update_serie), true, false);
			updateShowTh = new Thread(updateserierun);
			updateShowTh.start();
		}
	}
	
	public void updatePosterThumb(String serieId, Serie sToUpdate) {
		Cursor c = DroidShows.db.Query("SELECT posterInCache, poster, posterThumb FROM series WHERE id='"+ serieId +"'");
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
					Log.e(SQLiteStore.TAG, "Show "+ serieId +" doesn't have poster URL");
					e.printStackTrace();
					return;
				}
				File posterThumbFile = new File(posterThumbPath);
				try {
					FileUtils.copyURLToFile(posterURL, posterThumbFile);
				} catch (IOException e) {
					Log.e(SQLiteStore.TAG, "Could not download poster: "+ posterURL);
					e.printStackTrace();
					return;
				}
				Bitmap posterThumb = BitmapFactory.decodeFile(posterThumbPath);
				if (posterThumb == null) {
					Log.e(SQLiteStore.TAG, "Corrupt or unknown poster file type:"+ posterThumbPath);
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
					Log.d(SQLiteStore.TAG, "Updated poster thumb for "+ sToUpdate.getSerieName());
				} catch (FileNotFoundException e) {
					Log.e(SQLiteStore.TAG, "File not found:"+ posterThumbFile);
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
			m_ProgressDialog.setMessage(dialogMsg);
		}
	};
	
	public void clearFilter(View v) {
		main.setVisibility(View.INVISIBLE);
		keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
		searchV.setText("");
		findViewById(R.id.search).setVisibility(View.GONE);
		getSeries(showArchive);
	}

	private void updateAllSeries() {
		if (!utils.isNetworkAvailable(DroidShows.this)) {
			Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
		} else {
			final Runnable updateMessage = new Runnable() {
				public void run() {
					updateAllSeriesPD.setMessage(dialogMsg);
					updateAllSeriesPD.show();
				}
			};
			final Runnable updateallseries = new Runnable() {
				public void run() {
					if (theTVDB == null)
						theTVDB = new TheTVDB("8AC675886350B3C3", useMirror);
					String updatesFailed = "";
					for (int i = 0; i < series.size(); i++) {
						Log.d(SQLiteStore.TAG, "Getting updated info from TheTVDB "+ (useMirror ? "MIRROR " : "")
							+"for TV show " + series.get(i).getName() +" ["+ (i+1) +"/"+ (series.size()) +"]");
						dialogMsg = series.get(i).getName() + "\u2026";
						updateAllSeriesPD.incrementProgressBy(1);
						runOnUiThread(updateMessage);
						Serie sToUpdate = theTVDB.getSerie(series.get(i).getSerieId(), langCode);
						if (sToUpdate == null) {
							updatesFailed += dialogMsg +" ";
						} else {
							try {
								db.updateSerie(sToUpdate, latestSeasonOption == UPDATE_LATEST_SEASON_ONLY);
								updatePosterThumb(series.get(i).getSerieId(), sToUpdate);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					if (updatesFailed.length() > 0) {
						final String updatesFailedResult = updatesFailed;
						runOnUiThread(new Runnable() {
							public void run() {errorNotify(updatesFailedResult);}
						});
					}
					listView.post(new Runnable() {
						public void run() {getSeries((searching() ? 2 : showArchive));}
					});
					updateAllSeriesPD.dismiss();
					theTVDB = null;
				}
			};
			updateAllSeriesPD = new ProgressDialog(this);
			String updateMessageAD = getString(R.string.dialog_update_series) + (latestSeasonOption == UPDATE_ALL_SEASONS ? getString(R.string.dialog_update_speedup) : "");
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.messages_title_update_series)
				.setMessage(updateMessageAD)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.dialog_OK), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						updateAllSeriesPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						updateAllSeriesPD.setTitle(R.string.messages_title_updating_series);
						updateAllSeriesPD.setMessage(getString(R.string.messages_update_series));
						updateAllSeriesPD.setCancelable(false);
						updateAllSeriesPD.setMax(series.size());
						updateAllSeriesPD.setProgress(0);
						updateAllSeriesPD.show();
						updateAllShowsTh = new Thread(updateallseries);
						updateAllShowsTh.start();
						return;
					}
				})
				.setNegativeButton(getString(R.string.dialog_Cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				});
			alertDialog.show();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void errorNotify(String error) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent appIntent = PendingIntent.getActivity(DroidShows.this, 0, new Intent(), 0);
		Notification notification = new Notification(R.drawable.noposter,
			getString(R.string.messages_thetvdb_con_error), System.currentTimeMillis());
		notification.setLatestEventInfo(getApplicationContext(), getString(R.string.messages_thetvdb_con_error),
			error, appIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(0, notification);
	}

	private void getSeries(int show) {
		main.setVisibility(View.INVISIBLE);
		if (asyncInfo != null)
			asyncInfo.cancel(true);
		try {
			List<String> serieIds = db.getSeries(show);
			series.clear();
			for (int i = 0; i < serieIds.size(); i++) {
				series.add(db.createTVShowItem(serieIds.get(i)));
				seriesAdapter.notifyDataSetChanged();
			}
			setTitle(getString(R.string.layout_app_name)
					+(show == 1 ? " - "+ getString(R.string.archive) : ""));
			listView.post(updateListView);
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, "Error populating TVShowItems or no shows added yet");
			e.printStackTrace();
		}
		setFastScroll();
		asyncInfo = new AsyncInfo();
		asyncInfo.execute();
	}
	
	public static Runnable updateListView = new Runnable() {
		public void run() {
			main.setVisibility(View.INVISIBLE);
			seriesAdapter.notifyDataSetChanged();
			if (series != null && series.size() > 0) {
				if (seriesAdapter.isFiltered) {
					for (int i = 0; i < seriesAdapter.getCount(); i++) {
						String adapterSerie = seriesAdapter.getItem(i).getSerieId();
						for (TVShowItem serie : series)
							if (serie.getSerieId().equals(adapterSerie))
								seriesAdapter.setItem(i, serie);
					}
				} else {
					for (int i = 0; i < series.size(); i++) {
						if (series.get(i).equals(seriesAdapter.getItem(i)))
							seriesAdapter.setItem(i, series.get(i));
						else
							seriesAdapter.add(series.get(i));
					}
				}
			}
			
			Comparator<TVShowItem> comperator = new Comparator<TVShowItem>() {
				public int compare(TVShowItem object1, TVShowItem object2) {
					if (pinnedShows.contains(object1.getSerieId()) && !pinnedShows.contains(object2.getSerieId()))
						return -1;
					else if (pinnedShows.contains(object2.getSerieId()) && !pinnedShows.contains(object1.getSerieId()))
						return 1;

					if (sortOption == SORT_BY_LAST_UNSEEN) {
						int unwatchedAired1 = object1.getUnwatchedAired();
						int unwatchedAired2 = object2.getUnwatchedAired();
						if (unwatchedAired1 == unwatchedAired2) {
							Date nextAir1 = object1.getNextAir();
							Date nextAir2 = object2.getNextAir();
							if (nextAir1 == null && nextAir2 == null)
								return object1.getName().compareToIgnoreCase(object2.getName());
							if (nextAir1 == null)
								return 1;
							if (nextAir2 == null)
								return -1;
							return nextAir1.compareTo(nextAir2);
						}
						if (unwatchedAired1 == 0)
							return 1;
						if (unwatchedAired2 == 0)
							return -1;
						return ((Integer) unwatchedAired2).compareTo(unwatchedAired1);
					} else {
						return object1.getName().compareToIgnoreCase(object2.getName());
					}
				}
			};
			
			seriesAdapter.sort(comperator);
			if (seriesAdapter.isFiltered)
				seriesAdapter.getFilter().filter(searchV.getText());
			seriesAdapter.notifyDataSetChanged();
			main.setVisibility(View.VISIBLE);
		}
	};
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = sharedPrefs.edit();
		ed.putBoolean(AUTO_BACKUP_PREF_NAME, autoBackupOption);
		ed.putInt(SORT_PREF_NAME, sortOption);
		ed.putBoolean(EXCLUDE_SEEN_PREF_NAME, excludeSeen);
		ed.putInt(LATEST_SEASON_PREF_NAME, latestSeasonOption);
		ed.putBoolean(INCLUDE_SPECIALS_NAME, includeSpecialsOption);
		ed.putBoolean(FULL_LINE_CHECK_NAME, fullLineCheckOption);
		ed.putBoolean(SWITCH_SWIPE_DIRECTION, switchSwipeDirection);
		ed.putString(LAST_STATS_UPDATE_NAME, lastStatsUpdateCurrent);
		ed.putString(LAST_STATS_UPDATE_ARCHIVE_NAME, lastStatsUpdateArchive);
		ed.putString(LANGUAGE_CODE_NAME, langCode);
		ed.putBoolean(SHOW_NEXT_AIRING, showNextAiring);
		ed.putBoolean(USE_MIRROR, useMirror);
		ed.putString(PINNED_SHOWS_NAME, pinnedShows.toString());
		ed.commit();
	}
	
	@Override
	protected void onDestroy() {
		if (autoBackupOption)
			backup(true);
		super.onDestroy();
	}

	@Override
	public void onRestart() {
		super.onRestart();
		listView.post(updateShowView(backFromSeasonSerieId));
		backFromSeasonSerieId = null;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (searchV.getText().length() > 0) {
			findViewById(R.id.search).setVisibility(View.VISIBLE);
			listView.requestFocus();
		}
		if (asyncInfo == null || asyncInfo.getStatus() != AsyncTask.Status.RUNNING) {
			asyncInfo = new AsyncInfo();
			asyncInfo.execute();
		}
	}
	
	private class AsyncInfo extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
//			Log.d(SQLiteStore.TAG, "AsyncInfo Initializing");
			try {
				int showArchiveTmp = showArchive;
				String newToday = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());	// thread needs own SimpleDateFormat to prevent collisions in formatting of other dates
				String lastStatsUpdate = (showArchiveTmp == 0 ? lastStatsUpdateCurrent: lastStatsUpdateArchive);
				if (!lastStatsUpdate.equals(newToday)) {
					db.updateToday(newToday);
//					Log.d(SQLiteStore.TAG, "AsyncInfo RUNNING | Today = "+ newToday);
					for (int i = 0; i < series.size(); i++) {
						TVShowItem serie = series.get(i);
						if (isCancelled()) return null;
						String serieId = serie.getSerieId();
						int unwatched = db.getEPUnwatched(serieId);
						int unwatchedAired = db.getEPUnwatchedAired(serieId);
						if (unwatched != serie.getUnwatched() || unwatchedAired != serie.getUnwatchedAired()) {
							if (isCancelled()) return null;
							serie.setUnwatched(unwatched);
							serie.setUnwatchedAired(unwatchedAired);
							runOnUiThread(new Runnable() {
								public void run() {seriesAdapter.notifyDataSetChanged();}
							});
							if (isCancelled()) return null;
							if (showNextAiring && 0 < unwatchedAired) {
								NextEpisode nextEpisode = db.getNextEpisode(serieId);
								String nextEpisodeString = db.getNextEpisodeString(nextEpisode, true);
								db.execQuery("UPDATE series SET unwatched="+ unwatched +", unwatchedAired="+ unwatchedAired +", nextEpisode='"+ nextEpisodeString +"' WHERE id="+ serieId);
								serie.setNextEpisode(nextEpisodeString);
								runOnUiThread(new Runnable() {
									public void run() {seriesAdapter.notifyDataSetChanged();}
								});
							} else
								db.execQuery("UPDATE series SET unwatched="+ unwatched +", unwatchedAired="+ unwatchedAired +" WHERE id="+ serieId);
						}
					}
					listView.post(updateListView);
					if (showArchiveTmp == 0 || showArchiveTmp == 2)
						lastStatsUpdateCurrent = newToday;
					if (showArchiveTmp > 0)
						lastStatsUpdateArchive = newToday;
//				Log.d(SQLiteStore.TAG, "Updated show stats for "+ (showArchiveTmp == 0 ? "current" : "archive") +" on "+ newToday);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (findViewById(R.id.search).getVisibility() != View.VISIBLE) {
			findViewById(R.id.search).setVisibility(View.VISIBLE);
			getSeries(2);	// Get archived and current shows
		}
		searchV.requestFocus();
		searchV.selectAll();
		keyboard.showSoftInput(searchV, 0);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (searching())
			clearFilter(null);
		else if (showArchive == 1)
			toggleArchive();
		else
			super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("searching", searching());
		outState.putInt("showArchive", showArchive);
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

	private boolean searching() {
		return (seriesAdapter.isFiltered || findViewById(R.id.search).getVisibility() == View.VISIBLE);
	}
	
	public class SeriesAdapter extends ArrayAdapter<TVShowItem> {
		private List<TVShowItem> items;
		private ShowsFilter filter;
		private boolean isFiltered;
		private LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		private int iconListPosition;
		private ColorStateList textViewColors = new TextView(getContext()).getTextColors();

		private final String strEpAired = getString(R.string.messages_ep_aired);
		private final String strNewEp = getString(R.string.messages_new_episode);
		private final String strNewEps = getString(R.string.messages_new_episodes);
		private final String strNextAiring = getString(R.string.messages_next_airing);
		private final String strNextEp = getString(R.string.messages_next_episode);
		private final String strNoNewEps = getString(R.string.messages_no_new_eps);
		private final String strOf = getString(R.string.messages_of);
		private final String strOn = getString(R.string.messages_on);
		private final String strSeason = getString(R.string.messages_season);
		private final String strSeasons = getString(R.string.messages_seasons);
		private final String strToBeAired = getString(R.string.messages_to_be_aired);
		
		public SeriesAdapter(Context context, int textViewResourceId, List<TVShowItem> series) {
			super(context, textViewResourceId, series);
			items = series;
			isFiltered = false;
		}
		
		@Override
		public int getCount() {
			return items.size();
		}
		
		@Override
		public Filter getFilter() {
			if (filter == null)
				filter = new ShowsFilter();
			return filter;
		}
		
		@Override
		public TVShowItem getItem(int position) {
			return items.get(position);
		}
		
		public void setItem(int location, TVShowItem serie) {
			items.set(location, serie);
			notifyDataSetChanged();
		}
		
		private class ShowsFilter extends Filter {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				if (constraint == null || constraint.length() == 0) {
					results.count = series.size();
					results.values = series;
					isFiltered = false;
				} else {
					constraint = constraint.toString().toLowerCase();
					ArrayList<TVShowItem> filteredSeries = new ArrayList<TVShowItem>();
					for (TVShowItem serie : series) {
						if (serie.getName().toLowerCase().contains(constraint))
							filteredSeries.add(serie);
					}
					results.count = filteredSeries.size();
					results.values = filteredSeries;
					isFiltered = true;
				}
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				items = (List<TVShowItem>) results.values;
				notifyDataSetChanged();
			}
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			TVShowItem serie = items.get(position);
			ViewHolder holder;
			if (excludeSeen && !isFiltered && serie != lastSerie && serie.getUnwatchedAired() == 0 && (serie.getNextAir() == null || serie.getNextAir().after(Calendar.getInstance().getTime()))) {
				if (convertView == null || convertView.isEnabled()) {
					convertView = vi.inflate(R.layout.row_excluded, parent, false);
					convertView.setEnabled(false);
				}
				return convertView;
			} else if (convertView == null || !convertView.isEnabled()) {
				convertView = vi.inflate(R.layout.row, parent, false);
				holder = new ViewHolder();
				holder.sn = (TextView) convertView.findViewById(R.id.seriename);
				holder.si = (TextView) convertView.findViewById(R.id.serieinfo);
				holder.sne = (TextView) convertView.findViewById(R.id.serienextepisode);
				holder.icon = (IconView) convertView.findViewById(R.id.serieicon);
				convertView.setEnabled(true);
				convertView.setTag(holder);
				holder.icon.setOnTouchListener(iconTouchListener);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.icon.setOnClickListener(null);
			}
			int nunwatched = serie.getUnwatched();
			int nunwatchedAired = serie.getUnwatchedAired();
			String ended = (serie.getShowStatus().equalsIgnoreCase("Ended") ? " \u2020" : "");
			holder.sn.setText(serie.getName() + ended);
			if (pinnedShows.contains(serie.getSerieId()))
				holder.sn.setTextColor(getResources().getColor(android.R.color.white));
			else
				holder.sn.setTextColor(textViewColors);
			if (holder.si != null) {
				String siText = "";
				int sNumber = serie.getSNumber();
				if (sNumber == 1) {
					siText = sNumber +" "+ strSeason;
				} else {
					siText = sNumber +" "+ strSeasons;
				}
				String unwatched = "";
				if (nunwatched == 0) {
					unwatched = strNoNewEps;
					if (!serie.getShowStatus().equalsIgnoreCase("null"))
						unwatched += " ("+ translateStatus(serie.getShowStatus()) +")";
					holder.si.setEnabled(false);
				} else {
					unwatched = nunwatched +" "+ (nunwatched > 1 ? strNewEps : strNewEp) +" ";
					if (nunwatchedAired > 0) {
						unwatched = (nunwatchedAired == nunwatched ? "" : nunwatchedAired +" "+ strOf +" ") + unwatched + strEpAired + (nunwatchedAired == nunwatched && ended.isEmpty() ? " \u00b7" : "");
						holder.si.setEnabled(true);
					} else {
						unwatched += strToBeAired;
						holder.si.setEnabled(false);
					}
				}
				holder.si.setText(siText +" | "+ unwatched);
			}
			if (holder.sne != null) {
				if (nunwatched > 0 && !serie.getNextEpisode().isEmpty()) {
					holder.sne.setText(serie.getNextEpisode() == null ? "" : serie.getNextEpisode()
							.replace("[ne]", strNextEp)
							.replace("[na]", strNextAiring)
							.replace("[on]", strOn));
					holder.sne.setEnabled(serie.getNextAir() != null && serie.getNextAir().compareTo(Calendar.getInstance().getTime()) <= 0);
				} else {
					holder.sne.setText("");
				}
			}
			if (holder.icon != null) {
				Drawable icon = serie.getDIcon();
				if (icon == null)
					if (!serie.getIcon().equals(""))
						icon = Drawable.createFromPath(serie.getIcon());
				if (icon == null) {
					holder.icon.setImageResource(R.drawable.noposter);
				} else {
					holder.icon.setImageDrawable(icon);
					serie.setDIcon(icon);
				}
			}
			return convertView;
		}
		
		private OnTouchListener iconTouchListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				iconListPosition = listView.getPositionForView(v);
				keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
				iconGestureDetector.onTouchEvent(event);
				return true;
			}
		};
		
		private final GestureDetector.SimpleOnGestureListener iconGestureListener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				episodeDetails(iconListPosition);
				return true;
			}
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				String[] extResources = seriesAdapter.getItem(iconListPosition).getExtResources().trim().split("\\n");
				boolean foundResources = false;
				for (int i = 0; i < extResources.length; i++) {
					if (extResources[i].startsWith("*")) {
						browseExtResource(extResources[i]);
						foundResources = true;
					}
				}
				if (!foundResources)
					extResources(seriesAdapter.getItem(iconListPosition).getExtResources(), iconListPosition);
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e) {
				showDetails(seriesAdapter.getItem(iconListPosition).getSerieId());
			}
		};

		private GestureDetector iconGestureDetector = new GestureDetector(getApplicationContext(), iconGestureListener);
	}

	static class ViewHolder
	{
		TextView sn;
		TextView si;
		TextView sne;
		IconView icon;
	}
}