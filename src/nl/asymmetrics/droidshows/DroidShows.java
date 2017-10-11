package nl.asymmetrics.droidshows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
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
import nl.asymmetrics.droidshows.ui.BounceListView;
import nl.asymmetrics.droidshows.ui.IconView;
import nl.asymmetrics.droidshows.ui.SerieSeasons;
import nl.asymmetrics.droidshows.ui.ViewEpisode;
import nl.asymmetrics.droidshows.ui.ViewSerie;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import nl.asymmetrics.droidshows.utils.Update;
import nl.asymmetrics.droidshows.utils.Utils;
import nl.asymmetrics.droidshows.utils.SQLiteStore.NextEpisode;
import android.annotation.SuppressLint;
import android.app.ActionBar;
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
import android.os.Build;
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
import android.view.GestureDetector.SimpleOnGestureListener;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ToggleButton;

public class DroidShows extends ListActivity
{
	/* Menus */
	private static final int UNDO_MENU_ITEM = Menu.FIRST;
	private static final int SEARCH_MENU_ITEM = UNDO_MENU_ITEM + 1;
	private static final int FILTER_MENU_ITEM = SEARCH_MENU_ITEM + 1;
	private static final int SORT_MENU_ITEM = FILTER_MENU_ITEM + 1;
	private static final int TOGGLE_ARCHIVE_MENU_ITEM = SORT_MENU_ITEM + 1;
	private static final int LOG_MODE_ITEM = TOGGLE_ARCHIVE_MENU_ITEM + 1;
	private static final int ADD_SERIE_MENU_ITEM = LOG_MODE_ITEM + 1;
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
	private static BounceListView listView;
	private static String backFromSeasonSerieId;
	private static TheTVDB theTVDB;
	private Utils utils = new Utils();
	private Update updateDS;
	private static final String PREF_NAME = "DroidShowsPref";
	private SharedPreferences sharedPrefs;
	private static final String AUTO_BACKUP_PREF_NAME = "auto_backup";
	private static boolean autoBackupOption;
	private static final String BACKUP_FOLDER_PREF_NAME = "backup_folder";
	private static String defaultBackupFolder;
	private static final String SORT_PREF_NAME = "sort";
	private static final int SORT_BY_NAME = 0;
	private static final int SORT_BY_UNSEEN = 1;
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
	private static final String FILTER_NETWORKS_NAME = "filter_networks";
	private static boolean filterNetworks;
	private static final String NETWORKS_NAME = "networks";
	private static List<String> networks = new ArrayList<String>();
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
	public static boolean logMode = false;
	public static String removeEpisodeFromLog = "";
	private File[] dirList;
	private String[] dirNamesList;
	private Spinner spinner = null;
	
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
		defaultBackupFolder = sharedPrefs.getString(BACKUP_FOLDER_PREF_NAME, Environment.getExternalStorageDirectory() +"/DroidShows");
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
		filterNetworks = sharedPrefs.getBoolean(FILTER_NETWORKS_NAME, false); 
		String networksStr = sharedPrefs.getString(NETWORKS_NAME, "");
		if (!networksStr.isEmpty())
			networks = new ArrayList<String>(Arrays.asList(networksStr.replace("[", "").replace("]", "").split(", ")));
		series = new ArrayList<TVShowItem>();
		seriesAdapter = new SeriesAdapter(this, R.layout.row, series);
		setListAdapter(seriesAdapter);
		listView = (BounceListView) getListView();
		listView.setDivider(null);
		listView.setOverscrollHeader(getResources().getDrawable(R.drawable.shape_gradient_ring));
		if (savedInstanceState != null) {
			showArchive = savedInstanceState.getInt("showArchive");
			getSeries((savedInstanceState.getBoolean("searching") ? 2 : showArchive));
		} else {
			getSeries();
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
		if (!excludeSeen || logMode) {
			if (seriesAdapter.getCount() > 20) {
				try {	// http://stackoverflow.com/a/26447004
					Drawable thumb = getResources().getDrawable(R.drawable.thumb);
					String fieldName = "mFastScroller";
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)	// 22 = Lollipop
			            fieldName = "mFastScroll";
	
					java.lang.reflect.Field fieldFastScroller = AbsListView.class.getDeclaredField(fieldName);
					fieldFastScroller.setAccessible(true);
					listView.setFastScrollEnabled(true);
					Object thisFastScroller = fieldFastScroller.get(listView);
					java.lang.reflect.Field fieldToChange;
	
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
	
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
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
		menu.add(0, UNDO_MENU_ITEM, 0, getString(R.string.menu_undo)).setIcon(android.R.drawable.ic_menu_revert);
		menu.add(0, SEARCH_MENU_ITEM, 0, getString(R.string.menu_search)).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, FILTER_MENU_ITEM, 0, getString(R.string.menu_filter)).setIcon(android.R.drawable.ic_menu_view);
		menu.add(0, SORT_MENU_ITEM, 0, "");
		menu.add(0, TOGGLE_ARCHIVE_MENU_ITEM, 0, "");
		menu.add(0, LOG_MODE_ITEM, 0, getString(R.string.menu_log)).setIcon(android.R.drawable.ic_menu_agenda);
		menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, UPDATEALL_MENU_ITEM, 0, getString(R.string.menu_update)).setIcon(android.R.drawable.ic_menu_upload);
		menu.add(0, OPTIONS_MENU_ITEM, 0, getString(R.string.menu_about)).setIcon(android.R.drawable.ic_menu_manage);
		menu.add(0, EXIT_MENU_ITEM, 0, getString(R.string.menu_exit)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			arrangeActionBar(menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@SuppressLint("NewApi")
	private void arrangeActionBar(Menu menu) {
		menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM).setVisible(false);
		menu.findItem(LOG_MODE_ITEM).setVisible(false);
		menu.findItem(SEARCH_MENU_ITEM).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		spinner = new Spinner(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			spinner.setPopupBackgroundResource(R.drawable.menu_dropdown_panel);
		spinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,
			new String[] {
				getString(R.string.layout_app_name),
				getString(R.string.archive),
				getString(R.string.menu_log),
			}));
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				logMode = position == 2;
				showArchive = (position == 2 ? showArchive : position);
				if (logMode)
					clearFilter(null);
				getSeries();
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
		actionBar.setCustomView(spinner);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			actionBar.setIcon(R.drawable.actionbar);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(UNDO_MENU_ITEM)
			.setVisible(undo.size() > 0);
		menu.findItem(FILTER_MENU_ITEM)
			.setEnabled(!logMode && !searching());
		menu.findItem(SORT_MENU_ITEM)
			.setEnabled(!logMode);
		menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
			.setEnabled(!logMode && !searching());
		menu.findItem(LOG_MODE_ITEM)
			.setEnabled(!searching())
			.setTitle((logMode ? R.string.menu_close_log: R.string.menu_log));
		menu.findItem(UPDATEALL_MENU_ITEM)
			.setEnabled(!logMode);

		if (showArchive == 1) {
			menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_today)
				.setTitle(R.string.menu_show_current);
		} else {
			menu.findItem(TOGGLE_ARCHIVE_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_recent_history)
				.setTitle(R.string.menu_show_archive);
		}
		if (sortOption == SORT_BY_UNSEEN) {
			menu.findItem(SORT_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_sort_alphabetically)
				.setTitle(R.string.menu_sort_by_name);
		} else {
			menu.findItem(SORT_MENU_ITEM)
				.setIcon(android.R.drawable.ic_menu_sort_by_size)
				.setTitle(R.string.menu_sort_by_unseen);
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
			case FILTER_MENU_ITEM :
				filterDialog();
				break;
			case UPDATEALL_MENU_ITEM :
				updateAllSeriesDialog();
				break;
			case OPTIONS_MENU_ITEM :
				aboutDialog();
				break;
			case UNDO_MENU_ITEM :
				markLastEpUnseen();
				break;
			case LOG_MODE_ITEM :
				toggleLogMode();
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
		getSeries();
		listView.setSelection(0);
	}

	private void toggleSort() {
		sortOption ^= 1;
		listView.post(updateListView);
	}
	
	private void toggleLogMode() {
		logMode ^= true;
		getSeries();
		removeEpisodeFromLog = "";
		listView.setSelection(0);
	}
	
	private void filterDialog() {
		if (m_AlertDlg != null) {
			m_AlertDlg.dismiss();
		}
		final View filterV = View.inflate(this, R.layout.alert_filter, null);
		((CheckBox) filterV.findViewById(R.id.exclude_seen)).setChecked(excludeSeen);
		List<String> allNetworks = db.getNetworks();
		final LinearLayout networksFilterV = (LinearLayout) filterV.findViewById(R.id.networks_filter);
		for (String network : allNetworks) {
			CheckBox networkCheckBox = new CheckBox(this);
			networkCheckBox.setText(network);
			if (!networks.isEmpty())
				networkCheckBox.setChecked(networks.contains(network));
			networksFilterV.addView(networkCheckBox);
		}
		ToggleButton networksFilter = (ToggleButton) filterV.findViewById(R.id.toggle_networks_filter);
		networksFilter.setChecked(filterNetworks);
		toggleNetworksFilter(networksFilter);
		m_AlertDlg = new AlertDialog.Builder(this)
		.setView(filterV)
		.setTitle(R.string.menu_filter)
		.setIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? R.drawable.icon : 0)
		.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				applyFilters((ScrollView) filterV, networksFilterV);
			}
		})
		.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				m_AlertDlg.dismiss();
			}
		})
		.show();
	}
	
	public void toggleNetworksFilter(View v) {
		boolean enabled = (((ToggleButton) v).isChecked());
		LinearLayout networksFilterV = (LinearLayout) ((View) v.getParent().getParent()).findViewById(R.id.networks_filter);
		for (int i = 0; i < networksFilterV.getChildCount(); i++) {
			networksFilterV.getChildAt(i).setEnabled(enabled);
		}
	}
	
	private void applyFilters(ScrollView filterV, LinearLayout networksFilterV) {
		excludeSeen = (((CheckBox) filterV.findViewById(R.id.exclude_seen)).isChecked() ? true : false);
		filterNetworks = (((ToggleButton) filterV.findViewById(R.id.toggle_networks_filter)).isChecked() == true);
		for (int i = 0; i < networksFilterV.getChildCount(); i++) {
			CheckBox networkCheckBox = (CheckBox) networksFilterV.getChildAt(i);
			String network = (String) networkCheckBox.getText();
			if (networkCheckBox.isChecked()) {
				if (!networks.contains(network))
					networks.add(network);
			} else {
				if (networks.contains(network))
					networks.remove(network);
			}
		}
		getSeries();
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
		((TextView) about.findViewById(R.id.change_language)).setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
		((CheckBox) about.findViewById(R.id.auto_backup)).setChecked(autoBackupOption);
		((CheckBox) about.findViewById(R.id.latest_season)).setChecked(latestSeasonOption == UPDATE_LATEST_SEASON_ONLY);
		((CheckBox) about.findViewById(R.id.include_specials)).setChecked(includeSpecialsOption);
		((CheckBox) about.findViewById(R.id.full_line_check)).setChecked(fullLineCheckOption);
		((CheckBox) about.findViewById(R.id.switch_swipe_direction)).setChecked(switchSwipeDirection);
		((CheckBox) about.findViewById(R.id.show_next_airing)).setChecked(showNextAiring);
		((CheckBox) about.findViewById(R.id.use_mirror)).setChecked(useMirror);
		m_AlertDlg = new AlertDialog.Builder(this)
			.setView(about)
			.setTitle(R.string.menu_about)
			.setIcon(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? R.drawable.icon : 0)
			.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					m_AlertDlg.dismiss();
				}
			})
			.show();
	}
	
	public void dialogOptions(View v) {
		switch(v.getId()) {
			case R.id.backup:
				m_AlertDlg.dismiss();
				backup(false);
				break;
			case R.id.restore:
				m_AlertDlg.dismiss();
				restore();
				break;
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
				changeLang.setTitle(R.string.dialog_change_language)
					.setItems(R.array.languages, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							langCode = getResources().getStringArray(R.array.langcodes)[item];
							TextView changeLangB = (TextView) m_AlertDlg.findViewById(R.id.change_language);
							changeLangB.setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
						}
					})
					.show();
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
		if (auto) {
			backup(auto, defaultBackupFolder);
		} else {
			File folder = new File(defaultBackupFolder);
			if (!folder.isDirectory())
				folder.mkdir();
			filePicker(defaultBackupFolder, getString(R.string.dialog_backup), false);
		}
	}

	private void restore() {
		filePicker(defaultBackupFolder, getString(R.string.dialog_restore), true);
	}
	
	private void filePicker(final String folderString, final String title, final boolean restoring) {
		File folder = new File(folderString);
		File[] tempDirList = dirContents(folder, restoring);
		if (folderString.equals("/")) {
			dirList = new File[tempDirList.length];
			dirNamesList = new String[tempDirList.length];
			for(int i = 0; i < tempDirList.length; i++) {
				dirList[i] = tempDirList[i];
				dirNamesList[i] = tempDirList[i].getName();
				if (restoring && dirList[i].isFile())
					dirNamesList[i] += " ("+ SimpleDateFormat.getDateTimeInstance().format(tempDirList[i].lastModified()) +")";
			}
		} else {
			dirList = new File[tempDirList.length + 1];
			dirNamesList = new String[tempDirList.length + 1];
			dirList[0] = folder.getParentFile();
			dirNamesList[0] = "..";
			for(int i = 0; i < tempDirList.length; i++) {
				dirList[i+1] = tempDirList[i];
				dirNamesList[i+1] = tempDirList[i].getName();
				if (restoring && dirList[i+1].isFile())
					dirNamesList[i+1] += " ("+ SimpleDateFormat.getDateTimeInstance().format(tempDirList[i].lastModified()) +")";
			}
		}
		AlertDialog.Builder filePicker = new AlertDialog.Builder(this)
			.setTitle(folder.toString())
			.setItems(dirNamesList, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					File chosenFile = dirList[which];
					if (chosenFile.isDirectory()) {
						filePicker(chosenFile.toString(), title, restoring);
					} else if (restoring) {
						confirmRestore(chosenFile.toString());
					}
				}
			})
			.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		
		if (!restoring)
			filePicker.setPositiveButton(R.string.dialog_backup, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					backup(false, folderString);
				}
			});
		filePicker.show();
	}

	private File[] dirContents(File folder, final boolean showFiles)  {
		if (!folder.exists())
			folder = new File(Environment.getExternalStorageDirectory(), "");
		if (folder.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File file = new File(dir.getAbsolutePath() + File.separator + filename);
					if (showFiles)
						return file.isDirectory()
							|| file.isFile() && file.getName().toLowerCase().indexOf("droidshows.") == 0;
					else
						return file.isDirectory();
				}
			};
			File[] list = folder.listFiles(filter);
			if (list != null)
				Arrays.sort(list, filesComperator);
			return list == null ? new File[0] : list;
		} else {
			return new File[0];
		}
	}
	
	private static Comparator<File> filesComperator = new Comparator<File>() {
		public int compare(File f1, File f2) {
			if (f1.isDirectory() && !f2.isDirectory())
				return 1;
			if (f2.isDirectory() && !f1.isDirectory())
				return -1;
			return f1.getName().compareToIgnoreCase(f2.getName());
		}
	};

	private void backup(boolean auto, String backupFolder) {
		File source = new File(getApplicationInfo().dataDir +"/databases/DroidShows.db");
		File destination = new File(backupFolder, "DroidShows.db");
		if (auto && (!autoBackupOption || 
				new SimpleDateFormat("yyyy-MM-dd")
					.format(destination.lastModified()).equals(lastStatsUpdateCurrent) ||
				source.lastModified() == destination.lastModified()))
			return;
		if (destination.exists()) {
			File previous0 = new File(backupFolder, "DroidShows.db0");
			if (previous0.exists()) {
				File previous1 = new File(backupFolder, "DroidShows.db1");
				if (previous1.exists())
					previous1.delete();
				previous0.renameTo(previous1);
			}
			destination.renameTo(previous0);
		}
		File folder = new File(backupFolder);
		if (!folder.isDirectory())
			folder.mkdir();
		int toastTxt = R.string.dialog_backup_done;
		try {
			copy(source, destination);
		} catch (IOException e) {
			toastTxt = R.string.dialog_backup_failed;
			e.printStackTrace();
		}
		if (!auto) {
			asyncInfo = new AsyncInfo();
			asyncInfo.execute();
		}
		if (!auto || toastTxt == R.string.dialog_backup_failed)
			Toast.makeText(getApplicationContext(), getString(toastTxt) + " ("+ backupFolder +")", Toast.LENGTH_LONG).show();
	}

	private void confirmRestore(final String backupFile) {
		new AlertDialog.Builder(DroidShows.this)
		.setTitle(R.string.dialog_restore)
		.setMessage(R.string.dialog_restore_now)
		.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				restore(backupFile);
				}
			})
		.setNegativeButton(R.string.dialog_cancel, null)
		.show();
	}

	private void restore(String backupFile) {
		String toastTxt = getString(R.string.dialog_restore_done);
		File source = new File(backupFile);
		if (!source.exists()) source = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db0");
		if (!source.exists()) source = new File(Environment.getExternalStorageDirectory() +"/DroidShows", "DroidShows.db1");
		if (!source.exists()) source = new File(Environment.getExternalStorageDirectory(), "DroidShows.db");
		if (!source.exists()) source = new File(Environment.getExternalStorageDirectory(), "droidseries.db");
		if (!source.exists()) source = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "DroidShows.db");
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
				getSeries(2, false);	// 2 = archive and current shows, false = don't filter networks
				updateAllSeries();
				undo.clear();
				toastTxt += " ("+ source.getPath() +")";
			} catch (IOException e) {
				toastTxt = getString(R.string.dialog_restore_failed);
				e.printStackTrace();
			}
		} else {
			toastTxt = getString(R.string.dialog_restore_notfound);
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
		if (logMode || seriesAdapter.getItem(info.position).getUnwatched() == 0) {
			menu.findItem(VIEW_EPISODEDETAILS_CONTEXT).setVisible(false);
			menu.findItem(MARK_NEXT_EPISODE_AS_SEEN_CONTEXT).setVisible(false);
		} else if (seriesAdapter.getItem(info.position).getUnwatchedAired() == 0)
	    	menu.findItem(MARK_NEXT_EPISODE_AS_SEEN_CONTEXT).setVisible(false);
		if (!logMode) {
		    if (seriesAdapter.getItem(info.position).getPassiveStatus())
		    	menu.findItem(TOGGLE_ARCHIVED_CONTEXT).setTitle(R.string.menu_unarchive);
		    if (pinnedShows.contains(seriesAdapter.getItem(info.position).getSerieId()))
		    	menu.findItem(PIN_CONTEXT).setTitle(R.string.menu_context_unpin);
		} else {
			menu.findItem(DELETE_CONTEXT).setVisible(false);
			menu.findItem(TOGGLE_ARCHIVED_CONTEXT).setVisible(false);
			menu.findItem(PIN_CONTEXT).setVisible(false);
			menu.findItem(DELETE_CONTEXT).setVisible(false);
		}
		menu.setHeaderTitle(seriesAdapter.getItem(info.position).getName());
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
					.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							deleteTh = new Thread(deleteserie);
							deleteTh.start();
							return;
						}
					})
					.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
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
	
	@SuppressLint("NewApi")
	public void openContext(View v) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			listView.showContextMenuForChild(v, v.getX(), v.getY());
		else
			openContextMenu(v);
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
			if (!logMode) {
				serieSeasons(position);
			} else {
				episodeDetails(position);
			}
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
	
	private void serieSeasons(int position) {
		backFromSeasonSerieId = seriesAdapter.getItem(position).getSerieId();
		Intent serieSeasons = new Intent(DroidShows.this, SerieSeasons.class);
		serieSeasons.putExtra("serieId", backFromSeasonSerieId);
		serieSeasons.putExtra("nextEpisode", seriesAdapter.getItem(position).getUnwatched() > 0);
		startActivity(serieSeasons);
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
				if (newPosition != position) {
					listView.setSelection(newPosition);
					if (listView.getLastVisiblePosition() > newPosition)
						listView.smoothScrollBy(-padding, 400);
				}
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
		String episodeId = "-1";
		if (!logMode)
			episodeId = db.getNextEpisodeId(serieId);
		else
			episodeId = seriesAdapter.getItem(position).getEpisodeId();
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
								.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
										String resources = input.getText().toString().trim();
										serie.setExtResources(resources);
										db.updateExtResources(serie.getSerieId(), resources);
										return;
									}
								})
								.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
										return;
									}
								})
							.show();
							if (extResourcesInput.length() == 0) {
								input.setText("tvshow.wikia.com\n*double-tap-poster.openstarred.url\ntvshow.blogspot.com");
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
	
	@SuppressWarnings("deprecation")
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
					Log.e(SQLiteStore.TAG, sToUpdate.getSerieName() +" doesn't have a poster URL");
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
					Log.e(SQLiteStore.TAG, "Corrupt or unknown poster file type: "+ posterThumbPath);
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
		getSeries();
	}

	public void updateAllSeriesDialog() {
		String updateMessageAD = getString(R.string.dialog_update_series) + (latestSeasonOption == UPDATE_ALL_SEASONS ? getString(R.string.dialog_update_speedup) : "");
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.messages_title_update_series)
			.setMessage(updateMessageAD)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setCancelable(false)
			.setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					updateAllSeries();
					return;
				}
			})
			.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
		alertDialog.show();
	}

	public void updateAllSeries() {
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
						public void run() {
							getSeries((searching() ? 2 : showArchive));
							listView.updating = false;
						}
					});
					updateAllSeriesPD.dismiss();
					theTVDB = null;
				}
			};
			updateAllSeriesPD = new ProgressDialog(this);
			updateAllSeriesPD.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			updateAllSeriesPD.setTitle(R.string.messages_title_updating_series);
			updateAllSeriesPD.setMessage(getString(R.string.messages_update_series));
			updateAllSeriesPD.setCancelable(false);
			updateAllSeriesPD.setMax(series.size());
			updateAllSeriesPD.setProgress(0);
			updateAllSeriesPD.show();
			updateAllShowsTh = new Thread(updateallseries);
			updateAllShowsTh.start();
		}
	}
	
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void errorNotify(String error) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent appIntent = PendingIntent.getActivity(DroidShows.this, 0, new Intent(), 0);
		
		Notification notification = null;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			notification = new Notification(R.drawable.noposter,
					getString(R.string.messages_thetvdb_con_error), System.currentTimeMillis());
			try {
				Method deprecatedMethod = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
				deprecatedMethod.invoke(notification, getApplicationContext(), getString(R.string.messages_thetvdb_con_error), error, appIntent);
			} catch (Exception e) {
				Log.e(SQLiteStore.TAG, "Method setLatestEventInfo not found", e);
			}
		} else {
			Notification.Builder builder = new Notification.Builder(getApplicationContext())
				.setContentIntent(appIntent)
				.setSmallIcon(R.drawable.noposter)
				.setContentTitle(getString(R.string.messages_thetvdb_con_error))
				.setContentText(error);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
			    notification = builder.getNotification();
			else
			    notification = builder.build();
		}
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(0, notification);
	}

	private void getSeries() {
		getSeries(showArchive, filterNetworks);
	}
	
	private void getSeries(int showArchive) {
		getSeries(showArchive, filterNetworks);
	}

	private void getSeries(int showArchive, boolean filterNetworks) {
		main.setVisibility(View.INVISIBLE);
		if (asyncInfo != null)
			asyncInfo.cancel(true);
		try {
			if (!logMode) {
				List<String> ids = db.getSeries(showArchive, filterNetworks, networks);
				series.clear();
				for (int i = 0; i < ids.size(); i++) {
					series.add(db.createTVShowItem(ids.get(i)));
					seriesAdapter.notifyDataSetChanged();
				}
			} else {
				List<TVShowItem> episodes = db.getLog();
				series.clear();
				for (int i = 0; i < episodes.size(); i++) {
					series.add(episodes.get(i));
					seriesAdapter.notifyDataSetChanged();
				}
			}
			setTitle(getString(R.string.layout_app_name)
					+ (logMode ? " - "+ getString(R.string.menu_log) :
						(showArchive == 1 ? " - "+ getString(R.string.archive) : "")));
			listView.post(updateListView);
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, "Error populating TVShowItems or no shows added yet");
			e.printStackTrace();
		}
		setFastScroll();
		asyncInfo = new AsyncInfo();
		asyncInfo.execute();
	}
	
	public void getNextLogged() {
		List<TVShowItem> episodes = db.getLog(series.size());
		for (int i = 0; i < episodes.size(); i++) {
			series.add(episodes.get(i));
			seriesAdapter.notifyDataSetChanged();
		}
		listView.gettingNextLogged = false;
	}

	public static Runnable updateListView = new Runnable() {
		public void run() {
			main.setVisibility(View.INVISIBLE);
			seriesAdapter.notifyDataSetChanged();
/*			if (series != null && series.size() > 0) {
				if (seriesAdapter.isFiltered) {
					for (int i = 0; i < seriesAdapter.getCount(); i++) {
						String adapterSerie = seriesAdapter.getItem(i).getSerieId();
						for (TVShowItem serie : series)
							if (serie.getSerieId().equals(adapterSerie))
								seriesAdapter.setItem(i, serie);
					}
				} else {
					for (int i = 0; i < series.size(); i++) {
						if (seriesAdapter.getCount() > i && series.get(i).equals(seriesAdapter.getItem(i)))
							seriesAdapter.setItem(i, series.get(i));
						else
							seriesAdapter.add(series.get(i));
/*	The following is legacy code that now causes java.lang.outOfMemoryError due to seriesAdapter.add(series.get(i));
 *	ArrayList.ensureCapacityInternal -> ensureExplicitCapacity -> grow -> copyOf -> Failed to allocate a 124606360 byte
					}
				}
			}
*/			
			if (!logMode) seriesAdapter.sort(showsComperator);
			if (seriesAdapter.isFiltered)
				seriesAdapter.getFilter().filter(searchV.getText());
			seriesAdapter.notifyDataSetChanged();
			main.setVisibility(View.VISIBLE);
		}
	};
	
	private static Comparator<TVShowItem> showsComperator = new Comparator<TVShowItem>() {
		public int compare(TVShowItem object1, TVShowItem object2) {
			if (pinnedShows.contains(object1.getSerieId()) && !pinnedShows.contains(object2.getSerieId()))
				return -1;
			else if (pinnedShows.contains(object2.getSerieId()) && !pinnedShows.contains(object1.getSerieId()))
				return 1;

			if (sortOption == SORT_BY_UNSEEN) {
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
	
	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = sharedPrefs.edit();
		ed.putBoolean(AUTO_BACKUP_PREF_NAME, autoBackupOption);
		ed.putString(BACKUP_FOLDER_PREF_NAME, defaultBackupFolder);
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
		ed.putBoolean(FILTER_NETWORKS_NAME, filterNetworks);
		ed.putString(NETWORKS_NAME, networks.toString());
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
		if (!logMode) {
			listView.post(updateShowView(backFromSeasonSerieId));
			backFromSeasonSerieId = null;
		} else {
			if (!removeEpisodeFromLog.isEmpty()) {
				for (int i = 0; i < series.size(); i++)
					if (series.get(i).getEpisodeId().equals(removeEpisodeFromLog)) {
						series.remove(i);
						listView.post(updateListView);
					}
				removeEpisodeFromLog = "";
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (searchV.getText().length() > 0) {
			findViewById(R.id.search).setVisibility(View.VISIBLE);
			listView.requestFocus();
		}
		if (!logMode && (asyncInfo == null || asyncInfo.getStatus() != AsyncTask.Status.RUNNING)) {
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
					if (isCancelled()) return null;
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
		if (logMode)
			return false;
		if (findViewById(R.id.search).getVisibility() != View.VISIBLE) {
			findViewById(R.id.search).setVisibility(View.VISIBLE);
			getSeries(2, false);	// 2 = archive and current shows, false = don't filter networks
		}
		searchV.requestFocus();
		searchV.selectAll();
		keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (searching())
			clearFilter(null);
		else {
			if (logMode)
				toggleLogMode();
			else if (showArchive == 1)
				toggleArchive();
			else
				super.onBackPressed();
			if (spinner != null)
				spinner.setSelection(showArchive);
		}
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
		
		public void updateData(List<TVShowItem> series) {
			items = series;
			notifyDataSetChanged();
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
			@SuppressLint("DefaultLocale")
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

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				items = (List<TVShowItem>) results.values;
				notifyDataSetChanged();
			}
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			TVShowItem serie = items.get(position);
			ViewHolder holder;
			if (!logMode &&  excludeSeen && !isFiltered && serie != lastSerie && serie.getUnwatchedAired() == 0 && (serie.getNextAir() == null || serie.getNextAir().after(Calendar.getInstance().getTime()))) {
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
				holder.context = (ImageView) convertView.findViewById(R.id.seriecontext);
				convertView.setEnabled(true);
				convertView.setTag(holder);
				holder.icon.setOnTouchListener(iconTouchListener);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.icon.setOnClickListener(null);
			}
			if (!logMode) {
				int nunwatched = serie.getUnwatched();
				int nunwatchedAired = serie.getUnwatchedAired();
				String ended = (serie.getShowStatus().equalsIgnoreCase("Ended") ? " \u2020" : "");
				if (holder.sn != null) {
					holder.sn.setText(serie.getName() + ended);
					if (pinnedShows.contains(serie.getSerieId()))
						holder.sn.setTextColor(getResources().getColor(android.R.color.white));
					else
						holder.sn.setTextColor(textViewColors);
				}
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
					if (icon == null && !serie.getIcon().equals(""))
						icon = Drawable.createFromPath(serie.getIcon());
					if (icon == null) {
						holder.icon.setImageResource(R.drawable.noposter);
					} else {
						holder.icon.setImageDrawable(icon);
						serie.setDIcon(icon);
					}
				}
				if (holder.context != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
						holder.context.setImageResource(R.drawable.context_material);
					holder.context.setVisibility(View.VISIBLE);
				}
			} else {
				if (holder.sn != null) {
					holder.sn.setText(serie.getName());
					holder.sn.setTextColor(textViewColors);
				}					
				if (holder.si != null) {
					holder.si.setEnabled(true);
					holder.si.setText(serie.getEpisodeName());
				}
				if (holder.sne != null) {
					holder.sne.setEnabled(true);
					holder.sne.setText(serie.getEpisodeSeen());
				}
				if (holder.icon != null) {
					Drawable icon = serie.getDIcon();
					if (icon == null && !serie.getIcon().equals(""))
						icon = Drawable.createFromPath(serie.getIcon());
					if (icon == null) {
						holder.icon.setImageResource(R.drawable.noposter);
					} else {
						holder.icon.setImageDrawable(icon);
						serie.setDIcon(icon);
					}
				}
				if (holder.context != null)
					holder.context.setVisibility(View.GONE);
			}
			return convertView;
		}
		
		private OnTouchListener iconTouchListener = new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				iconListPosition = listView.getPositionForView(v);
				iconGestureDetector.onTouchEvent(event);
				return true;
			}
		};
		
		private final SimpleOnGestureListener iconGestureListener = new SimpleOnGestureListener() {
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
				if (!logMode)
					episodeDetails(iconListPosition);
				else
					serieSeasons(iconListPosition);
				return true;
			}
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
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
				keyboard.hideSoftInputFromWindow(searchV.getWindowToken(), 0);
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
		ImageView context;
	}
}