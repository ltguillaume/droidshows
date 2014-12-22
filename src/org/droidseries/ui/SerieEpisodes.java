package org.droidseries.ui;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.droidseries.droidseries;
import org.droidseries.R;

//import org.droidseries.utils.JsonStore;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import java.text.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class SerieEpisodes extends ListActivity {

	private final String TAG = "DroidSeries";

	private EpisodesAdapter episodes_adapter;

	@SuppressWarnings("unused")
	private Runnable viewEpisodes;

	private String serieid;
	private int nseason;
	private List<String> episodes = null;

	private ListView listView;

	/* Context Menus */
	private static final int VIEWEP_CONTEXT = Menu.FIRST;
	private static final int DELEP_CONTEXT = Menu.FIRST + 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.serie_episodes);
			serieid = getIntent().getStringExtra("serieid");
			nseason = getIntent().getIntExtra("nseason", 0);
			setTitle(droidseries.db.getSerieName(serieid) + " - " + getString(R.string.messages_season) + " " + nseason + " - " + getString(R.string.messages_episodes));
			episodes = droidseries.db.getEpisodes(serieid, nseason);
			listView = getListView();
			episodes_adapter = new EpisodesAdapter(this, R.layout.row_serie_episodes, episodes);
			listView.setOnTouchListener(new SwipeDetect());
			listView.setAdapter(episodes_adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
							startViewEpisode(episodes.get(position));
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
				}
			});		
			registerForContextMenu(getListView());
	}

	/* context menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.add(0, VIEWEP_CONTEXT, 0,getString(R.string.messsages_view_ep_details));
			menu.add(0, DELEP_CONTEXT, 0, getString(R.string.menu_context_delete));
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
				case VIEWEP_CONTEXT:
					startViewEpisode(episodes.get(info.position));
					return true;
				case DELEP_CONTEXT:
						String query;
						query = "DELETE FROM episodes WHERE serieId='" + serieid + "' " +
								"AND id = '" + episodes.get(info.position) + "'";
						Log.d(TAG, query);
						droidseries.db.execQuery(query);
						episodes_adapter.remove(episodes_adapter.getItem(info.position));
						return true;
				default:
						return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onStop() {
		runOnUiThread(SerieSeasons.getSeasonInfo);
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
				convertView = vi.inflate(R.layout.row_serie_episodes, null);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.aired = (TextView) convertView.findViewById(R.id.aired);
				holder.seen = (CheckBox) convertView.findViewById(R.id.seen);
				
				convertView.setTag(holder);
			}
			else {
				holder = (ViewHolder) convertView.getTag();
				holder.seen.setOnClickListener(null);
			}

			String episodeId = items.get(position);
			String query = "SELECT episodeName, episodeNumber, seen, firstAired FROM episodes WHERE id='" + episodeId + "' AND serieId='" + serieid + "'";	// Guillaume: added firstAired
			Cursor c = droidseries.db.Query(query);
			c.moveToFirst();

			if (c != null) {
				int enameCol = c.getColumnIndex("episodeName");
				int enumberCol = c.getColumnIndex("episodeNumber");
				int seenCol = c.getColumnIndex("seen");
				int airedCol = c.getColumnIndex("firstAired");
				
				String aired = c.getString(airedCol);
				if (!aired.equals("") && !aired.equals("null")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					try {
						Format formatter = SimpleDateFormat.getDateInstance();
						aired = formatter.format(sdf.parse(aired));
					} catch (ParseException e) {
						Log.e(TAG, e.getMessage());
					}
				} else {
					aired = "";
				}

				if (holder.name != null) {
					String tmpName = "", tmpAired = "";
					tmpName = (getString(R.string.messages_ep).isEmpty() ? "" : getString(R.string.messages_ep) +" ") + c.getInt(enumberCol) +". "+ c.getString(enameCol) + "\n";
					if (!aired.equals(""))
						tmpAired = getString(R.string.messages_aired) + " " + aired;
					holder.name.setText(tmpName);
					holder.aired.setText(tmpAired);
				}
				
				holder.seen.setChecked(c.getInt(seenCol) == 1);
				holder.seen.setOnClickListener(new OnClickListener() {
					public void onClick(View view) {
						try {
							droidseries.db.updateUnwatchedEpisode(serieid, episodes.get(position));
						} catch (Exception e) {
							Toast.makeText(getApplicationContext(), "Could not set episode seen state", Toast.LENGTH_LONG).show();
							Log.e(TAG, e.getMessage());
						}
					}
				});
			}
			c.close();
			return convertView;
		}
	}
	static class ViewHolder {
		TextView name;
		TextView aired;
		CheckBox seen;
	}
	
	private void startViewEpisode(String episode) {
		Intent viewEpisode = new Intent(SerieEpisodes.this, ViewEpisode.class);
		String query = "SELECT episodeName, overview, rating, firstAired " +
				"FROM episodes WHERE serieId='" + serieid + "' AND id = '" + episode + "'";
		 Cursor c = droidseries.db.Query(query);
		 c.moveToFirst();
		 if (c != null && c.isFirst()) {
				 int enameCol = c.getColumnIndex("episodeName");
				 int overviewCol = c.getColumnIndex("overview");
				 int ratingCol = c.getColumnIndex("rating");
				 int airedCol = c.getColumnIndex("firstAired");
				 
				 String aired = c.getString(airedCol);
				 if (!aired.equals("") && !aired.equals("null")) {
					try {
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
										Date epDate = sdf.parse(aired);
										Format formatter = SimpleDateFormat.getDateInstance();
										aired = formatter.format(epDate);
									} catch (ParseException e) {
										Log.e(TAG, e.getMessage());
									}
				 }
				 else {
					aired = "";
				}

				 viewEpisode.putExtra("episodename", c.getString(enameCol));
				 viewEpisode.putExtra("episodeoverview", c.getString(overviewCol));
				 viewEpisode.putExtra("episoderating", c.getString(ratingCol));
				 viewEpisode.putExtra("episodefirstaired", aired);
				 c.close();

				 List<String> guestStars = new ArrayList<String>();
				 Cursor cgs = droidseries.db.Query("SELECT guestStar FROM guestStars WHERE serieId='" + serieid + "' AND episodeId='" + episode + "'");
				 cgs.moveToFirst();
				 if(cgs != null && cgs.isFirst()) {
					 do {
						 guestStars.add(cgs.getString(0));
					 } while ( cgs.moveToNext() );
				 }
				 cgs.close();
				 viewEpisode.putExtra("episodegueststars", guestStars.toString().replace("]", "").replace("[", ""));

				 List<String> writers = new ArrayList<String>();
				 Cursor cwriters = droidseries.db.Query("SELECT writer FROM writers WHERE serieId='" + serieid + "' AND episodeId='" + episode + "'");
				 cwriters.moveToFirst();
				 if(cwriters != null && cwriters.isFirst()) {
					 do {
						 writers.add(cwriters.getString(0));
					 } while ( cwriters.moveToNext() );
				 }
				 cwriters.close();
				 viewEpisode.putExtra("episodewriter", writers.toString().replace("]", "").replace("[", ""));

				 List<String> directors = new ArrayList<String>();
				 Cursor cdirectors = droidseries.db.Query("SELECT director FROM directors WHERE serieId='" + serieid + "' AND episodeId='" + episode + "'");
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
	}
}