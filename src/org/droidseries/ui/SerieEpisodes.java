package org.droidseries.ui;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

import org.droidseries.droidseries;
import org.droidseries.R;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

public class SerieEpisodes extends ListActivity {

	private final String TAG = "DroidSeries";
	private EpisodesAdapter episodesAdapter;
	private String serieName;
	private String serieId;
	private int seasonNumber;
	private List<String> episodes = null;
	private ListView listView;

	/* Context Menus */
	private static final int VIEWEP_CONTEXT = Menu.FIRST;
	private static final int DELEP_CONTEXT = Menu.FIRST + 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serie_episodes);
		serieId = getIntent().getStringExtra("serieId");
		serieName = droidseries.db.getSerieName(serieId);
		seasonNumber = getIntent().getIntExtra("seasonNumber", 0);
		setTitle(serieName +" - "+ (seasonNumber == 0 ? getString(R.string.messages_specials) : getString(R.string.messages_season) +" "+ seasonNumber));
		episodes = droidseries.db.getEpisodes(serieId, seasonNumber);
		episodesAdapter = new EpisodesAdapter(this, R.layout.row_serie_episodes, episodes);
		setListAdapter(episodesAdapter);
		listView = getListView();
		listView.setOnTouchListener(new SwipeDetect());
		registerForContextMenu(getListView());
	}

	/* context menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, VIEWEP_CONTEXT, 0, getString(R.string.messsages_view_ep_details));
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
			query = "DELETE FROM episodes WHERE serieId='" + serieId + "' "
				+ "AND id = '" + episodes.get(info.position) + "'";
			Log.d(TAG, query);
			droidseries.db.execQuery(query);
			episodes.remove(info.position);
			episodesAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (droidseries.fullLineCheckOption) {
			try {
				droidseries.db.updateUnwatchedEpisode(serieId, episodes.get(position));
				CheckBox c = (CheckBox) v.findViewById(R.id.seen);
				c.setChecked(!c.isChecked());
			} catch (Exception e) {
				Log.e(TAG, "Could not set episode seen state: "+ e.getMessage());
			}
		} else {
			try {
				startViewEpisode(episodes.get(position));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
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
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.row_serie_episodes, parent, false);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.aired = (TextView) convertView.findViewById(R.id.aired);
				holder.seen = (CheckBox) convertView.findViewById(R.id.seen);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String episodeId = items.get(position);
			String query = "SELECT episodeName, episodeNumber, seen, firstAired FROM episodes WHERE id='"
					+ episodeId + "' AND serieId='" + serieId + "'";
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
					tmpName = (getString(R.string.messages_ep).isEmpty() ? "" : getString(R.string.messages_ep) +" ") 
						+ c.getInt(enumberCol) +". "+ c.getString(enameCol);
					if (!aired.equals(""))
						tmpAired = getString(R.string.messages_aired) + " "+ aired;
					holder.name.setText(tmpName);
					holder.aired.setText(tmpAired);
				}

				holder.seen.setChecked(c.getInt(seenCol) == 1);
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

	public void check(View v) {
		int position = getListView().getPositionForView(v);
		try {
			droidseries.db.updateUnwatchedEpisode(serieId, episodes.get(position));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void startViewEpisode(String episode) {
		Intent viewEpisode = new Intent(SerieEpisodes.this, ViewEpisode.class);
		viewEpisode.putExtra("serieId", serieId);
		viewEpisode.putExtra("serieName", serieName);
		viewEpisode.putExtra("seasonNumber", seasonNumber);
		viewEpisode.putExtra("episodeId", episode);
		startActivity(viewEpisode);
	}
}