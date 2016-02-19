package nl.asymmetrics.droidshows.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Date;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
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
	private EpisodesAdapter episodesAdapter;
	private String serieName;
	private String serieId;
	private int seasonNumber;
	private List<String> episodes = null;
	private ListView listView;
	private SwipeDetect swipeDetect = new SwipeDetect();
	private SimpleDateFormat sdfseen = new SimpleDateFormat("yyyyMMdd");
	private SQLiteStore db;

	/* Context Menus */
	private static final int VIEWEP_CONTEXT = Menu.FIRST;
	private static final int DELEP_CONTEXT = Menu.FIRST + 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serie_episodes);
		db = SQLiteStore.getInstance(this);
		serieId = getIntent().getStringExtra("serieId");
		serieName = db.getSerieName(serieId);
		seasonNumber = getIntent().getIntExtra("seasonNumber", 0);
		setTitle(serieName +" - "+ (seasonNumber == 0 ? getString(R.string.messages_specials) : getString(R.string.messages_season) +" "+ seasonNumber));
		episodes = db.getEpisodes(serieId, seasonNumber);
		episodesAdapter = new EpisodesAdapter(this, R.layout.row_serie_episodes, episodes);
		setListAdapter(episodesAdapter);
		listView = getListView();
		listView.setOnTouchListener(swipeDetect);
		registerForContextMenu(getListView());
		if (getIntent().getBooleanExtra("nextEpisode", false))
			listView.setSelection(db.getNextEpisode(serieId, seasonNumber).episode -3);
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
			db.deleteEpisode(serieId, episodes.get(info.position));
			episodes.remove(info.position);
			episodesAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (swipeDetect.value == 0) {
			if (DroidShows.fullLineCheckOption) {
				try {
					check(v);
					CheckBox c = (CheckBox) v.findViewById(R.id.seen);
					c.setChecked(!c.isChecked());
				} catch (Exception e) {
					Log.e(SQLiteStore.TAG, "Could not set episode seen state: "+ e.getMessage());
				}
			} else {
				try {
					startViewEpisode(episodes.get(position));
				} catch (Exception e) {
					Log.e(SQLiteStore.TAG, e.getMessage());
				}
			}
		}
	}

	public class EpisodesAdapter extends ArrayAdapter<String> {

		private List<String> items;
		private LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		public EpisodesAdapter(Context context, int textViewResourceId, List<String> episodes) {
			super(context, textViewResourceId, episodes);
			this.items = episodes;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			if (convertView == null) {
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
			Cursor c = db.Query(query);
			c.moveToFirst();

			if (c != null) {
				int enameCol = c.getColumnIndex("episodeName");
				int enumberCol = c.getColumnIndex("episodeNumber");
				int seenCol = c.getColumnIndex("seen");
				int airedCol = c.getColumnIndex("firstAired");

				String aired = c.getString(airedCol);
				if (!aired.equals("") && !aired.equals("null")) {
					try {
						aired = SimpleDateFormat.getDateInstance().format(SQLiteStore.dateFormat.parse(aired));
					} catch (ParseException e) {
						Log.e(SQLiteStore.TAG, e.getMessage());
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
				
				int seen = c.getInt(seenCol);
				holder.seen.setChecked(seen > 0);
				if (seen > 1) {
					try {
						holder.seen.setTextColor(holder.aired.getTextColors().getDefaultColor());
						holder.seen.setText(SimpleDateFormat.getDateInstance().format(sdfseen.parse(seen +"")));
					} catch (ParseException e) {
						Log.e(SQLiteStore.TAG, e.getMessage());
					}
				} else {
					holder.seen.setText("");
				}
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
			db.updateUnwatchedEpisode(serieId, episodes.get(position));
			CheckBox c = (CheckBox) v.findViewById(R.id.seen);
			if (c.isChecked()) {
				c.setTextColor(getResources().getColor(android.R.color.white));
				c.setText(SimpleDateFormat.getDateInstance().format(Calendar.getInstance().getTime()));
			} else {
				c.setText("");
			}
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, e.getMessage());
		}
	}

	private void startViewEpisode(String episode) {
		Intent viewEpisode = new Intent(SerieEpisodes.this, ViewEpisode.class);
		viewEpisode.putExtra("serieId", serieId);
		viewEpisode.putExtra("serieName", serieName);
		viewEpisode.putExtra("episodeId", episode);
		startActivity(viewEpisode);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
	}
}