package org.droidseries.ui;

import java.util.ArrayList;
import java.util.List;

import org.droidseries.droidseries;

import org.droidseries.R;

//import org.droidseries.utils.JsonStore;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class SerieEpisodes extends ListActivity {

        private final String MY_DEBUG_TAG = "DroidSeries";

        private EpisodesAdapter episodes_adapter;

        @SuppressWarnings("unused")
        private Runnable viewEpisodes;

        private String serieid;
        private int nseason;
        private List<String> episodes = null;

        private ListView lView;

        /* Context Menus */
        private static final int VIEWEP_CONTEXT = Menu.FIRST;

        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                //setContentView(R.layout.main_serie_episodes);
                setContentView(R.layout.main_serie_episodes_listview);

                serieid = getIntent().getStringExtra("serieid");
                nseason = getIntent().getIntExtra("nseason", 0);

                setTitle(droidseries.db.getSerieName(serieid) + " - " + getString(R.string.messages_season) + " " + nseason + " - " + getString(R.string.messages_episodes));

                episodes = droidseries.db.getEpisodes(serieid, nseason);
                lView = getListView();

                episodes_adapter = new EpisodesAdapter(this, R.layout.row_checkedtextview, episodes);
                lView.setAdapter(episodes_adapter);

                lView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                lView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                        try {
                            droidseries.db.updateUnwatchedEpisode(serieid, episodes.get(position));
                        } catch (Exception e) {
                            Log.e(MY_DEBUG_TAG, e.getMessage());
                        }
                    }
                });

                //context menu para as seasons
                registerForContextMenu(getListView());
        }

        /* context menu */
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                super.onCreateContextMenu(menu, v, menuInfo);
                menu.add(0, VIEWEP_CONTEXT, 0,  getString(R.string.messsages_view_ep_details));
        }

        public boolean onContextItemSelected(MenuItem item) {
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                switch (item.getItemId()) {
                        case  VIEWEP_CONTEXT:
                                //TODO: check this
                                Intent viewEpisode = new Intent(SerieEpisodes.this, ViewEpisode.class);
                                String query = "SELECT episodeName, overview, rating, firstAired " +
                                                           "FROM episodes WHERE serieId='" + serieid + "' AND id = '" + episodes.get(info.position) + "'";

                                 Cursor c = droidseries.db.Query(query);
                                 c.moveToFirst();
                                 if (c != null && c.isFirst()) {
                                         int enameCol = c.getColumnIndex("episodeName");
                                         int overviewCol = c.getColumnIndex("overview");
                                         int ratingCol = c.getColumnIndex("rating");
                                         int airedCol = c.getColumnIndex("firstAired");
                                         viewEpisode.putExtra("episodename", c.getString(enameCol));
                                         viewEpisode.putExtra("episodeoverview", c.getString(overviewCol));
                                         viewEpisode.putExtra("episoderating", "Rating: " + c.getString(ratingCol));
                                         viewEpisode.putExtra("episodefirstaired", "Aired: " + c.getString(airedCol));
                                         c.close();

                                         List<String> guestStars = new ArrayList<String>();
                                         Cursor cgs = droidseries.db.Query("SELECT guestStar FROM guestStars WHERE serieId='" + serieid + "' AND episodeId='" + episodes.get(info.position) + "'");
                                         cgs.moveToFirst();
                                         if(cgs != null && cgs.isFirst()) {
                                             do {
                                                 guestStars.add(cgs.getString(0));
                                             } while ( cgs.moveToNext() );
                                         }
                                         cgs.close();
                                         viewEpisode.putExtra("episodegueststars", guestStars.toString().replace("]", "").replace("[", ""));

                                         List<String> writers = new ArrayList<String>();
                                         Cursor cwriters = droidseries.db.Query("SELECT writer FROM writers WHERE serieId='" + serieid + "' AND episodeId='" + episodes.get(info.position) + "'");
                                         cwriters.moveToFirst();
                                         if(cwriters != null && cwriters.isFirst()) {
                                             do {
                                                 writers.add(cwriters.getString(0));
                                             } while ( cwriters.moveToNext() );
                                         }
                                         cwriters.close();
                                         viewEpisode.putExtra("episodewriter", writers.toString().replace("]", "").replace("[", ""));

                                         List<String> directors = new ArrayList<String>();
                                         Cursor cdirectors = droidseries.db.Query("SELECT director FROM directors WHERE serieId='" + serieid + "' AND episodeId='" + episodes.get(info.position) + "'");
                                         cdirectors.moveToFirst();
                                         if(cdirectors != null && cdirectors.isFirst()) {
                                             do {
                                                 directors.add(cdirectors.getString(0));
                                             } while ( cdirectors.moveToNext() );
                                         }
                                         cdirectors.close();
                                         viewEpisode.putExtra("episodedirector", directors.toString().replace("]", "").replace("[", ""));

                                         startActivity(viewEpisode);
                                 }
                                return true;
                        default:
                                return super.onContextItemSelected(item);
                }
        }

        @Override
        public void onStop() {
                runOnUiThread(SerieSeasons.updateListView);

                Runnable changeEpisodeSeen = new Runnable(){
                    public void run() {
                        runOnUiThread(droidseries.updateListView);
                    }
                };

                Thread thCES =  new Thread(null, changeEpisodeSeen, "MagentoBackground");
                thCES.start();

                super.onStop();
        }

        public class EpisodesAdapter extends ArrayAdapter<String> {

                private List<String> items;

                public EpisodesAdapter(Context context, int textViewResourceId, List<String> episodes) {
                    super(context, textViewResourceId, episodes);
                    this.items = episodes;
                }

                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    final ViewHolder holder;

                    if (convertView == null) {
                        LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        convertView = vi.inflate(R.layout.row_checkedtextview, null);

                        holder = new ViewHolder();
                        holder.cb = (CheckedTextView) convertView.findViewById(R.id.tsname);
                        holder.cb.setTextSize(14);

                        holder.cb.setFocusable(false);
                        holder.cb.setFocusableInTouchMode(false);

                        convertView.setTag(holder);
                    }
                    else {
                        holder = (ViewHolder) convertView.getTag();
                    }

                    String episodeId = items.get(position);
                    String query = "SELECT episodeName, episodeNumber, seen FROM episodes WHERE id='" + episodeId + "' AND serieId='" + serieid + "'";
                    Cursor c = droidseries.db.Query(query);
                    c.moveToFirst();

                    if (c != null) {
                        int enameCol = c.getColumnIndex("episodeName");
                        int enumberCol = c.getColumnIndex("episodeNumber");
                        int seenCol = c.getColumnIndex("seen");

                        if (holder.cb != null) {
                            String tmpEN = "";
                            tmpEN = "Ep. " + c.getInt(enumberCol) + ": ";
                            tmpEN += c.getString(enameCol);
                            holder.cb.setText( tmpEN );

                            if (c.getInt(seenCol) == 1) {
                                lView.setItemChecked(position, true);
                            }
                        }
                    }
                    c.close();
                    return convertView;
                }
        }

        static class ViewHolder {
                CheckedTextView cb;
        }
}
