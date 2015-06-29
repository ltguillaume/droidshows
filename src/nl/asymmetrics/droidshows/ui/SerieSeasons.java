package nl.asymmetrics.droidshows.ui;

import java.util.ArrayList;
import java.util.List;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.thetvdb.model.Season;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class SerieSeasons extends ListActivity
{
	private String serieId;
	private List<Integer> seasonNumbers = new ArrayList<Integer>();
	private List<Season> seasons = new ArrayList<Season>();
	// Context Menus
	private static final int ALLEPSEEN_CONTEXT = Menu.FIRST;
	private static final int ALLUPTOTHIS_CONTEXT = ALLEPSEEN_CONTEXT + 1;
	private static final int ALLEPUNSEEN_CONTEXT = ALLUPTOTHIS_CONTEXT + 1;
	private static ListView listView;
	public static SeriesSeasonsAdapter seasonsAdapter;
	private static final Boolean pool = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB);
	private SwipeDetect swipeDetect = new SwipeDetect();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serie_seasons);
		serieId = getIntent().getStringExtra("serieId");
		setTitle(DroidShows.db.getSerieName(serieId));
		getSeasons();
		seasonsAdapter = new SeriesSeasonsAdapter(this, R.layout.row_serie_seasons, seasons);
		setListAdapter(seasonsAdapter);
		listView = getListView();
		listView.getViewTreeObserver().addOnGlobalLayoutListener(listDone);
		registerForContextMenu(listView);
		listView.setOnTouchListener(swipeDetect);
	}
	
	/* context menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, ALLEPSEEN_CONTEXT, 0, getString(R.string.messages_context_mark_seasonseen));
		menu.add(0, ALLUPTOTHIS_CONTEXT, 0, getString(R.string.messages_context_mark_asseenuptothis));
		menu.add(0, ALLEPUNSEEN_CONTEXT, 0, getString(R.string.messages_context_mark_seasonunseen));
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case ALLEPSEEN_CONTEXT :
				DroidShows.db.updateUnwatchedSeason(serieId, seasonNumbers.get(info.position));
				getInfo();
				return true;
			case ALLEPUNSEEN_CONTEXT :
				DroidShows.db.updateWatchedSeason(serieId, seasonNumbers.get(info.position));
				getInfo();
				return true;
			case ALLUPTOTHIS_CONTEXT :
				for (int i = 1; i <= seasonNumbers.get(info.position); i++) {
					DroidShows.db.updateUnwatchedSeason(serieId, i);
				}
				getInfo();
				return true;
			default :
				return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (swipeDetect.value == 0) {
			try {
				Intent serieEpisode = new Intent(SerieSeasons.this, SerieEpisodes.class);
				serieEpisode.putExtra("serieId", serieId);
				serieEpisode.putExtra("seasonNumber", seasonNumbers.get(position));
				startActivity(serieEpisode);
			} catch (Exception e) {
				Log.e(DroidShows.TAG, e.getMessage());
			}
		}
	}

	private void getSeasons() {
		try {
			Cursor cseasons = DroidShows.db.Query("SELECT season FROM serie_seasons WHERE serieId = '"+ serieId +"'");
			cseasons.moveToFirst();
			if (cseasons.getCount() != 0) {
				do {
					seasonNumbers.add(cseasons.getInt(0));
				} while (cseasons.moveToNext());
			}
			cseasons.close();
		} catch (Exception e) {
			Log.e(DroidShows.TAG, "Error getting seasons: "+ e.getMessage());
		}
		for (int i = 0; i < seasonNumbers.size(); i++) {
			String season = "";
			if (seasonNumbers.get(i) == 0) {
				season = getString(R.string.messages_specials);
			} else {
				season = getString(R.string.messages_season) + " " + seasonNumbers.get(i);
			}
			Season newSeason = new Season(serieId, seasonNumbers.get(i), season, -1, -1, "");
			seasons.add(newSeason);
		}
	}	
	
	@TargetApi(android.os.Build.VERSION_CODES.HONEYCOMB)
	private void getInfo() {
		for (int i = 0; i < seasons.size(); i++) {
			if (pool) {	// Otherwise executions won't be parallel >= HoneyComb
				new AsyncInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, i);
			} else {
				new AsyncInfo().execute(i);
			}
		}
	}
	
	private final OnGlobalLayoutListener listDone = new OnGlobalLayoutListener() {
		public void onGlobalLayout() {
			listView.getViewTreeObserver().removeGlobalOnLayoutListener(listDone);
			getInfo();
		}
	};
	
	private class AsyncInfo extends AsyncTask<Integer, Void, Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			int i = params[0];
			String serieId = seasons.get(i).getSerieId();
			int seasonNumber = seasons.get(i).getSNumber();
			int unwatchedAired = DroidShows.db.getSeasonEPUnwatchedAired(serieId, seasonNumber);
			int unwatched = DroidShows.db.getSeasonEPUnwatched(serieId, seasonNumber);
			seasons.get(i).setUnwatchedAired(unwatchedAired);
			seasons.get(i).setUnwatched(unwatched);
			if (unwatched > 0) {
				seasons.get(i).setNextEpisode(DroidShows.db.getNextEpisode(serieId, seasonNumber));
			}
			listView.post(new Runnable() { public void run() { seasonsAdapter.notifyDataSetChanged(); }});
			return null;
		}
	}

	@Override
	public void onRestart() {
		super.onRestart();
		getInfo();
	}
	
	private class SeriesSeasonsAdapter extends ArrayAdapter<Season>
	{
		private List<Season> items;

		public SeriesSeasonsAdapter(Context context, int textViewResourceId, List<Season> seasons) {
			super(context, textViewResourceId, seasons);
			this.items = seasons;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.row_serie_seasons, parent, false);
				holder = new ViewHolder();
				holder.season = (TextView) convertView.findViewById(R.id.serieseason);
				holder.unwatched = (TextView) convertView.findViewById(R.id.unwatched);
				holder.nextEpisode = (TextView) convertView.findViewById(R.id.nextepisode); 
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.unwatched.setText("");
				holder.nextEpisode.setText("");
				holder.nextEpisode.setTypeface(null, Typeface.NORMAL);
			}
			Season s = items.get(position);
			int nunwatched = s.getUnwatched();
			int nunwatchedAired = s.getUnwatchedAired();
			if (holder.season != null) {
				holder.season.setText(s.getSeason());
			}
			if (holder.unwatched != null) {
				String unwatchedText = DroidShows.db.getSeasonEpisodeCount(serieId, s.getSNumber())
						+" "+ getString(R.string.messages_episodes);
				if (nunwatched > 0) {
					String unwatched = "";
					unwatched = nunwatched +" "+ (nunwatched > 1 ? getString(R.string.messages_new_episodes)
							: getString(R.string.messages_new_episode)) +" ";
					if (nunwatchedAired > 0) unwatched = (nunwatchedAired == nunwatched ? "" : nunwatchedAired 
							+" "+ getString(R.string.messages_of) +" ") + unwatched + getString(R.string.messages_ep_aired);
					else unwatched += getString(R.string.messages_to_be_aired);
					unwatchedText += " | "+ unwatched;
				}
				holder.unwatched.setText(unwatchedText);
			}
			if (holder.nextEpisode != null) {
				String nextEpisodeText = "";
				if (nunwatched == 0) {
					nextEpisodeText = getString(R.string.messages_season_completely_watched);
				} else if (nunwatched > 0) {
					nextEpisodeText = getString(R.string.messages_next_episode) + " "
						+ s.getNextEpisode().replace("[on]", getString(R.string.messages_on));
					if (nunwatchedAired > 0) holder.nextEpisode.setTypeface(null, Typeface.BOLD);
				}
				holder.nextEpisode.setText(nextEpisodeText);				
			}
			return convertView;
		}
	}
	static class ViewHolder
	{
		TextView season;
		TextView unwatched;
		TextView nextEpisode;
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
	}
}