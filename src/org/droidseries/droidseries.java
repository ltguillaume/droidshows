package org.droidseries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

import org.droidseries.thetvdb.TheTVDB;
import org.droidseries.thetvdb.model.Serie;
import org.droidseries.thetvdb.model.TVShowItem;
import org.droidseries.ui.SerieSeasons;
import org.droidseries.ui.SerieViewPoster;
import org.droidseries.ui.ViewSerie;
import org.droidseries.utils.SQLiteStore;
import org.droidseries.utils.Update;
import org.droidseries.utils.Utils;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
//import android.os.PowerManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class droidseries extends ListActivity {
    public static String VERSION = "0.1.5-7";
    public static String CONTRIBUTORS = "Jeremy Wickersheimer, Russell Schmidt, Walla, Mikael Berthe";

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
    private static final int TOGGLE_SERIE_STATUS_CONTEXT = MARK_NEXT_EPISODE_AS_SEEN_CONTEXT+1;
    private static final int UPDATE_CONTEXT = TOGGLE_SERIE_STATUS_CONTEXT+1;
    private static final int DELETE_CONTEXT = UPDATE_CONTEXT + 1;
    private static final int VIEW_POSTER_CONTEXT = DELETE_CONTEXT + 1;
    private static final int VIEW_SERIEDETAILS_CONTEXT = VIEW_POSTER_CONTEXT + 1;

    private static AlertDialog m_AlertDlg;

    //private final String MY_DEBUG_TAG = getString(R.string.debug_tag);
    private final static String TAG = "DroidSeries";

    private ProgressDialog m_ProgressDialog = null;
    private static ProgressDialog updateAllSeriesPD = null;
    private Runnable viewSeries;

    public static SeriesAdapter series_adapter;

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
    private static int sortOption = SORT_BY_NAME;

    private static final String FILTER_PREF_NAME = "filter_passive";
    private static final int FILTER_DISABLED = 0;
    private static final int FILTER_ENABLED  = 1;
    private static int filterOption = FILTER_DISABLED;

    public static Thread deleteTh = null;
    private static boolean bDeleteTh = false;
    public static Thread updateShowTh = null;
    private static boolean bUpdateShowTh = false;
    public static Thread updateAllShowsTh = null;
    private static boolean bUpdateAllShowsTh = false;
    private boolean stopedUASTH = false;


    /*
     * public stuff that is needed somewhere else, not the best way to go.. but it's working
     */
    public static SQLiteStore db;
    public static List<TVShowItem> series = null;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //this removes the window title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

        db = new SQLiteStore(this);
        try {
                db.openDataBase();
        } catch(SQLException sqle){
                try {
                    db.createDataBase();
                    db.close();
                    try {
                        db.openDataBase();
                    } catch(SQLException sqle2){
                        Log.e(TAG, sqle2.getMessage());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to create database");
                }
        }

        // restore the preferences (for now just sort)
        sharedPrefs = getSharedPreferences(PREF_NAME, 0);
        sortOption = sharedPrefs.getInt(SORT_PREF_NAME, SORT_BY_NAME);
        filterOption = sharedPrefs.getInt(FILTER_PREF_NAME, FILTER_DISABLED);

        series = new ArrayList<TVShowItem>();

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        updateDS.updateDroidSeries(getApplicationContext(), display);

        viewSeries = new Runnable(){
                public void run() {
                        getUserSeries();
                }
        };

        m_ProgressDialog = ProgressDialog.show(droidseries.this,
                getString(R.string.messages_title_loading_series) , getString(R.string.messages_loading_series), true);
        Thread thread =  new Thread(null, viewSeries, "MagentoBackground");
        thread.start();

        ListView lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                try {
                        Intent serieSeasons = new Intent(droidseries.this, SerieSeasons.class);
                        serieSeasons.putExtra("serieid", droidseries.series.get(position).getSerieId());
                        startActivity(serieSeasons);
                } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                }
            }
        });

        //register context menu
        registerForContextMenu(getListView());

        series_adapter = new SeriesAdapter(this, R.layout.row, series);
        setListAdapter(series_adapter);
    }

        /* Options Menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, TOGGLE_FILTER_MENU_ITEM, 0, "Toggle filter");
        menu.add(0, SORT_MENU_ITEM, 0, getString(R.string.menu_sort)).setIcon(SORT_BY_LAST_UNSEEN_ICON);
        menu.add(0, UPDATEALL_MENU_ITEM, 0, getString(R.string.menu_update_all)).setIcon(android.R.drawable.ic_menu_upload);
        //menu.add(0, PREFERENCES_MENU_ITEM, 0, "Preferences").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(0, ABOUT_MENU_ITEM, 0, getString(R.string.menu_about)).setIcon(android.R.drawable.ic_menu_info_details);
        menu.add(0, EXIT_MENU_ITEM, 0, getString(R.string.menu_exit)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(sortOption == SORT_BY_LAST_UNSEEN) {
             menu.findItem(SORT_MENU_ITEM).setIcon(SORT_BY_NAME_ICON);
        } else {
             menu.findItem(SORT_MENU_ITEM).setIcon(SORT_BY_LAST_UNSEEN_ICON);
        }

        return super.onPrepareOptionsMenu(menu);
   }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Intent intent = null;

        switch (item.getItemId()) {
                case ADD_SERIE_MENU_ITEM:
                        onSearchRequested();
                        break;
                case TOGGLE_FILTER_MENU_ITEM:
                        toggleFilter();
                        break;
                case SORT_MENU_ITEM:
                        toggleSort();
                        break;
                case UPDATEALL_MENU_ITEM:
                        updateAllSeries();
                        break;
                case PREFERENCES_MENU_ITEM:
                        //TODO: create preferences interface
                        break;
                case ABOUT_MENU_ITEM:
                        if (m_AlertDlg != null)
                        {
                                m_AlertDlg.cancel();
                        }
                        m_AlertDlg = new AlertDialog.Builder(this)
                            .setMessage(getString(R.string.menu_about_content).replace("\\n","\n").replace("${VERSION}", VERSION).replace("${CONTRIBUTORS}", CONTRIBUTORS))
                            .setTitle(getString(R.string.menu_about))
                            .setIcon(R.drawable.icon)
                            .setCancelable(true)
                            .show();
                        break;
                case EXIT_MENU_ITEM:
                        //exitMenu();
                        this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void toggleFilter() {
        filterOption ^= 1;
        getUserSeries();
    }

    public void toggleSort() {
        sortOption--;
        if (sortOption < 0) {
            sortOption = SORT_BY_LAST_UNSEEN;
        }
        getUserSeries();
    }

        /* context menu */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MARK_NEXT_EPISODE_AS_SEEN_CONTEXT, 0, getString(R.string.menu_context_mark_next_episode_as_seen));
        menu.add(0, TOGGLE_SERIE_STATUS_CONTEXT, 0, getString(R.string.menu_context_toggle_status));
        menu.add(0, UPDATE_CONTEXT, 0, getString(R.string.menu_context_update));
        menu.add(0, DELETE_CONTEXT, 0,  getString(R.string.menu_context_delete));
        menu.add(0, VIEW_POSTER_CONTEXT, 0,  getString(R.string.menu_context_viewposter));
        menu.add(0, VIEW_SERIEDETAILS_CONTEXT, 0, getString(R.string.menu_context_view_serie_details));
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        TVShowItem serie;
        String serieid;

        switch (item.getItemId()) {
            case MARK_NEXT_EPISODE_AS_SEEN_CONTEXT:
                serie = series.get(info.position);
                serieid = serie.getSerieId();
                String nextEpisode = db.getNextEpisodeId(serieid, -1);

                db.updateUnwatchedEpisode(serieid, nextEpisode );
                runOnUiThread(updateListView);

                return true;
            case TOGGLE_SERIE_STATUS_CONTEXT:
                serie = series.get(info.position);
                serieid = serie.getSerieId();
                Integer st = db.getSerieStatus(serieid);
                Log.d(TAG, "Toggle serie [" + serieid + "] status case");
                st ^= 1;
                db.updateSerieStatus(serieid, st);

                // Remove the show from the list if it is passive and
                // if the filter is enabled
                if (filterOption == FILTER_ENABLED && st == 1) {
                    series.remove(series.get(info.position));
                } else {
                    // Update data to change the show's title on refresh
                    series.get(info.position).setPassiveStatus(st == 1);
                }
                runOnUiThread(updateListView);
                return true;
            case UPDATE_CONTEXT:
                updateSerie(series.get(info.position).getSerieId());
                return true;
            case DELETE_CONTEXT:
                //TODO (1): add a verification to be sure that the TV show was well eliminated
                //TODO (2): add the queue struct here, it may need to restart with pending actions to do (only half deleted)
                final int spos = info.position;
                final Runnable deleteserie = new Runnable(){
                    public void run() {
                        String sname = db.getSerieName(series.get(spos).getSerieId());

                        db.deleteSerie(series.get(spos).getSerieId());
                        series.remove(series.get(spos));
                        runOnUiThread(updateListView);

                        if (!bDeleteTh) {
                            m_ProgressDialog.dismiss();
                            bDeleteTh = false;
                        }

                        Context context = getApplicationContext();
                        CharSequence text =  String.format(getString(R.string.messages_delete_sucessful),sname);
                        int duration = Toast.LENGTH_LONG;

                        Looper.prepare();
                        Toast toast = Toast.makeText(context, text, duration);
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
                        m_ProgressDialog = ProgressDialog.show(droidseries.this,
                            getString(R.string.messages_title_delete_serie), getString(R.string.messages_delete_serie), true);
                        return;
                    } });
                alertDialog.setNegativeButton(getString(R.string.dialog_Cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    } });
                alertDialog.show();
                return true;
            case VIEW_POSTER_CONTEXT:
                String posterincache = db.getSeriePoster(series.get(info.position).getSerieId());
                if(!posterincache.equals("")) {
                    Intent viewPoster = new Intent(droidseries.this, SerieViewPoster.class);
                    viewPoster.putExtra("seriename", db.getSerieName(series.get(info.position).getSerieId()));
                    viewPoster.putExtra("poster", posterincache);
                    startActivity(viewPoster);
                }
                return true;
            case VIEW_SERIEDETAILS_CONTEXT:
                //TODO: check if the data is being all passed
                Intent viewSerie = new Intent(droidseries.this, ViewSerie.class);

                String query = "SELECT serieName, overview, firstAired, airsDayOfWeek, airsTime, runtime, network, rating " +
                        "FROM series WHERE id = '" + series.get(info.position).getSerieId() + "'";

                Cursor c = db.Query(query);
                c.moveToFirst();
                if (c != null && c.isFirst()) {

                    int snameCol = c.getColumnIndex("serieName");
                    int overviewCol = c.getColumnIndex("overview");
                    int firstAiredCol = c.getColumnIndex("firstAired");
                    int airsdayofweekCol = c.getColumnIndex("airsDayOfWeek");
                    int airstimeCol = c.getColumnIndex("airsTime");
                    int runtimeCol = c.getColumnIndex("runtime");
                    int networkCol = c.getColumnIndex("network");
                    int ratingCol = c.getColumnIndex("rating");


                    viewSerie.putExtra("seriename", c.getString(snameCol));
                    viewSerie.putExtra("serieoverview", c.getString(overviewCol));
                    viewSerie.putExtra("firstaired", c.getString(firstAiredCol));
                    viewSerie.putExtra("airday", c.getString(airsdayofweekCol));
                    viewSerie.putExtra("airtime", c.getString(airstimeCol));
                    viewSerie.putExtra("runtime", c.getString(runtimeCol));
                    viewSerie.putExtra("network", c.getString(networkCol));
                    viewSerie.putExtra("rating", c.getString(ratingCol));
                    c.close();

                    List<String> genres = new ArrayList<String>();
                    Cursor cgenres = db.Query("SELECT genre FROM genres WHERE serieId='" + series.get(info.position).getSerieId() + "'");
                    cgenres.moveToFirst();
                    if(cgenres != null && cgenres.isFirst()) {
                        do {
                            genres.add(cgenres.getString(0));
                        } while ( cgenres.moveToNext() );
                    }
                    cgenres.close();
                    viewSerie.putExtra("genre", genres.toString().replace("]", "").replace("[", "") );

                    List<String> actors = new ArrayList<String>();
                    Cursor cactors = db.Query("SELECT actor FROM actors WHERE serieId='" + series.get(info.position).getSerieId() + "'");
                    cactors.moveToFirst();
                    if(cactors != null && cactors.isFirst()) {
                        do {
                            actors.add(cactors.getString(0));
                        } while ( cactors.moveToNext() );
                    }
                    cactors.close();
                    viewSerie.putExtra("serieactors", actors.toString().replace("]", "").replace("[", ""));

                    startActivity(viewSerie);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    //TODO : change and create a struct for handling aditions and updates. The idea is to have a
    //queue of ids or somethings like similar to that, to process. Easy to stop and resume.
    private void updateSerie(String serieId) {
        final String id = serieId;

        if (utils.isNetworkAvailable(droidseries.this)) {
                Runnable updateserierun = new Runnable(){
                    public void run() {
                        //PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        //PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
                        //wl.acquire();
                        theTVDB = new TheTVDB("8AC675886350B3C3");
                        if (theTVDB.getMirror() != null) {
                            Serie sToUpdate = theTVDB.getSerie(id, "en");
                            db.updateSerie(sToUpdate, false);

                            runOnUiThread(droidseries.updateListView);
                        }
                        else {
                            //TODO: add a message here
                        }

                        //wl.release();
                        if(!bUpdateShowTh) {
                            m_ProgressDialog.dismiss();
                            bUpdateShowTh = false;
                        }
                        theTVDB = null;
                    }
                };

                updateShowTh = new Thread(null, updateserierun, "MagentoBackground");
                updateShowTh.start();
                m_ProgressDialog = ProgressDialog.show(droidseries.this,
                        getString(R.string.messages_title_updating_serie), getString(R.string.messages_update_serie), true);
        }
        else {
                CharSequence text = getString(R.string.messages_no_internet);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
        }
    }

    //updateAllShowsTh
    public synchronized void startUpdateAllShowsTh(){
        if(updateAllShowsTh == null){
            updateAllShowsTh = new Thread();
            updateAllShowsTh.start();
        }
    }

    public synchronized void stopUpdateAllShowsTh(){
        if(updateAllShowsTh != null){
            Thread moribund = updateAllShowsTh;
            updateAllShowsTh = null;
            moribund.interrupt();
        }
    }

    //TODO (1): change the progress dialog type to show the current tv show update progress
    //TODO (2): change and create a struct for handling aditions and updates. The idea is to have a
    //queue of ids or somethings like similar to that, to process. Easy to stop and resume.
    private void updateAllSeries() {
        if (!utils.isNetworkAvailable(droidseries.this)) {
                CharSequence text = getString(R.string.messages_no_internet);
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
        }
        else {
            final Runnable updateallseries = new Runnable(){
                public void run() {
                    theTVDB = new TheTVDB("8AC675886350B3C3");
                    for(int i=0; i < series.size(); i++) {
                        if(bUpdateAllShowsTh) {
                            stopUpdateAllShowsTh();
                            bUpdateAllShowsTh = false;
                            return;
                        }
                        Log.d(TAG, "Getting updated info from TheTVDB for TV show " + series.get(i).getName());
                        Serie sToUpdate = theTVDB.getSerie(series.get(i).getSerieId(), "en");
                        Log.d(TAG, "Updating the database...");
                        try {
                            db.updateSerie(sToUpdate, true);
                        }
                        catch (Exception e) {
                            //does nothing
                        }

                        if(!bUpdateAllShowsTh) {
                            updateAllSeriesPD.incrementProgressBy(1);
                        }
                    }

                    runOnUiThread(droidseries.updateListView);
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
            alertDialog.setMessage(getString(R.string.dialog_update_all_series));
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
                    updateAllShowsTh =  new Thread(null, updateallseries, "MagentoBackground");
                    updateAllShowsTh.start();
                    return;
                } });
            alertDialog.setNegativeButton(getString(R.string.dialog_Cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    return;
                } });
            alertDialog.show();
        }
    }

    private static TVShowItem createTVShowItem(String serieid) {
        String tmpName = "";
        String tmpPoster = "";

        int unwatched = db.getEPUnwatched(serieid);
        boolean completelyWatched = false;
        if(unwatched == 0) {
            completelyWatched = true;
        }

        String nextEpisode = db.getNextEpisode(serieid, -1);
        Date nextAir = db.getNextAir(serieid, -1);

        String query = "SELECT serieName, posterThumb FROM series WHERE id = '" + serieid + "'";
        Cursor c = db.Query(query);
        c.moveToFirst();

        int serieNameCol = c.getColumnIndex("serieName");
        int posterThumbCol = c.getColumnIndex("posterThumb");

        if (c != null && c.isFirst()) {
            tmpName =  c.getString(serieNameCol);
            tmpPoster = c.getString(posterThumbCol);
        }
        c.close();

        int tmpSeasons = db.getSeasonCount(serieid);
        Drawable d = Drawable.createFromPath(tmpPoster);
        boolean st = (db.getSerieStatus(serieid) == 1);

        TVShowItem tvsi = new TVShowItem(serieid, tmpPoster, d, tmpName,
                tmpSeasons, nextEpisode, nextAir, unwatched, st,
                completelyWatched);
        return tvsi;
    }

    private void getUserSeries(){
        Runnable updateList = new Runnable() {
            public void run() {
                series_adapter.notifyDataSetChanged();
            }
        };

        try {
                List<String> serieids;
                if (SORT_BY_NAME == sortOption) {
                    serieids = db.getSeriesByName();
                } else if (SORT_BY_LAST_UNSEEN == sortOption) {
                    serieids = db.getSeriesByNextEpisode();
                } else {
                    serieids = db.getSeries();
                }
                series.clear();
                for(int i=0; i < serieids.size(); i++) {
                        String serieid = serieids.get(i);
                        Integer st = db.getSerieStatus(serieid);
                        if (filterOption == FILTER_DISABLED || st == 0) {
                                TVShowItem tvsi = createTVShowItem(serieid);
                                series.add(tvsi);
                        }
                }
        } catch (Exception e) {
                Log.e(TAG, "Error populating the TVShowItems");
                e.printStackTrace();
        }
        runOnUiThread(updateList);
        m_ProgressDialog.dismiss();
    }

    public static Runnable updateListView = new Runnable() {
        public void run() {
            //series_adapter.clear();
            series_adapter.notifyDataSetChanged();
            if(series != null && series.size() > 0){
                series_adapter.notifyDataSetChanged();
                for(int i=0; i < series.size() ;i++) {
                    if(!series.get(i).equals(series_adapter.getItem(i))) {
                        series_adapter.add(series.get(i));
                    }
                    else {
                        TVShowItem tmpTVSI = series_adapter.getItem(i);
                        int unwatched = db.getEPUnwatched(tmpTVSI.getSerieId());
                        if(unwatched != tmpTVSI.getEpNotSeen()) {
                            if(unwatched == 0) {
                                tmpTVSI.setCompletelyWatched(true);
                            } else {
                                tmpTVSI.setCompletelyWatched(false);
                            }
                            tmpTVSI.setEpNotSeen(unwatched);
                        }
                        String nextEpisode = db.getNextEpisode(tmpTVSI.getSerieId(), -1);
                        Date nextAir = db.getNextAir(tmpTVSI.getSerieId(), -1);
                        tmpTVSI.setNextEpisode(nextEpisode);
                        tmpTVSI.setNextAir(nextAir);
                    }
                }
            }

            Comparator<TVShowItem> comperator = new Comparator<TVShowItem>() {
                public int compare(TVShowItem object1, TVShowItem object2) {
                    if (SORT_BY_LAST_UNSEEN == sortOption) {
                        // note: nextAir can be null when there are no next episode
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
        ed.commit();

        if(deleteTh != null) {
            if(deleteTh.isAlive()) {
                bDeleteTh = true;
            }
        }

        if(updateShowTh != null) {
            if(updateShowTh.isAlive()) {
                bUpdateShowTh = true;
            }
        }

        //TODO: make this work
        if(updateAllShowsTh != null) {
            if(updateAllShowsTh.isAlive()) {
                bUpdateAllShowsTh = true;
                Log.d(TAG, "Updating all TV shows... before pause");
                stopedUASTH = true;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "ON RESUME!!! : " + stopedUASTH);
    }

    @Override
    public void onDestroy() {
        Runnable exitSeries = new Runnable(){
            public void run() {
                db.close();
            }
        };

        Thread thSave =  new Thread(null, exitSeries, "MagentoBackground");
        thSave.start();

        super.onDestroy();
    }

    @Override
    public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        m_ProgressDialog.dismiss();
        super.onSaveInstanceState(outState);
    }

    //TODO: create update tv show async task
    private static class UpdateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    //TODO: create update all tv shows async task
    private static class UpdateAllTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    //TODO: create delete tv shows async task
    private static class DeleteTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public class SeriesAdapter extends ArrayAdapter<TVShowItem> {

        private List<TVShowItem> items;

        public SeriesAdapter(Context context, int textViewResourceId, List<TVShowItem> series) {
            super(context, textViewResourceId, series);
            this.items = series;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row, null);

                holder = new ViewHolder();
                holder.sn = (TextView) convertView.findViewById(R.id.seriename);
                holder.si = (TextView) convertView.findViewById(R.id.serieinfo);
                holder.sne = (TextView) convertView.findViewById(R.id.serienextepisode);
                holder.icon = (ImageView) convertView.findViewById(R.id.serieicon);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            TVShowItem serie = items.get(position);

            int nunwatched = serie.getEpNotSeen();

            if (holder.sn != null) {
                if (serie.getPassiveStatus()) {
                    holder.sn.setText("[" + serie.getName() + "]");
                } else {
                    holder.sn.setText(serie.getName());
                }
            }

            if (holder.si != null) {
                String unwatched = "";
                if (nunwatched == 0) {
                    unwatched = getString(R.string.messages_completely_watched);
                }
                else {
                    if(nunwatched == 1) {
                        unwatched = nunwatched + " " + getString(R.string.messages_episode_not_watched);
                    }
                    else {
                        unwatched = nunwatched + " " + getString(R.string.messages_episodes_not_watched);
                    }
                }

                if (serie.getSNumber() == 0 || serie.getSNumber() == 1) {
                    holder.si.setText( serie.getSNumber() + " " + getString(R.string.messages_season) + " | " + unwatched );
                }
                else {
                    holder.si.setText( serie.getSNumber() + " " + getString(R.string.messages_seasons) + " | " + unwatched );
                }
            }

            if(holder.sne != null){
                if(!serie.getCompletelyWatched()) {
                    String serieid = serie.getSerieId();
                    Date nextAir = db.getNextAir(serieid, -1);
                    Date current = new Date();

                    if (nextAir != null) {
                        if (nextAir.before(current)){
                            holder.sne.setTypeface( Typeface.DEFAULT, Typeface.BOLD );
                        }
                        else {
                            // Without this all next episodes lines would be bold after
                            // rotating screen
                            holder.sne.setTypeface( Typeface.DEFAULT, Typeface.NORMAL );
                        }
                    }
                    else {
                        holder.sne.setTypeface( Typeface.DEFAULT, Typeface.NORMAL );
                    }

                    holder.sne.setText(getString(R.string.messages_next_episode) + " " + serie.getNextEpisode());
                    holder.sne.setVisibility(View.VISIBLE);
                }
                else {
                    holder.sne.setVisibility(View.GONE);
                }
            }

            if(holder.icon != null){
                try {
                    if (!serie.getIcon().equals("")) {
                        holder.icon.setImageDrawable(serie.getDIcon());
                    }
                    else {
                        holder.icon.setImageDrawable(getResources().getDrawable(R.drawable.icon ));
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView sn;
        TextView si;
        TextView sne;
        ImageView icon;
    }
}
