package org.droidseries.ui;

import java.util.ArrayList;
import java.util.List;

import org.droidseries.droidseries;
import org.droidseries.thetvdb.model.Season;

import org.droidseries.R;


import android.app.ListActivity;
import android.app.ProgressDialog;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class SerieSeasons extends ListActivity {
	
	private final String TAG = "DroidSeries";
	
	private static List<Integer> iseasons;
	private static List<Season> seasons;
	
	private String serieid;
	
	// Context Menus
	private static final int ALLEPSEEN_CONTEXT = Menu.FIRST;
	private static final int ALLEPUNSEEN_CONTEXT = ALLEPSEEN_CONTEXT + 1;
	private static final int ALLUPTOTHIS_CONTEXT = ALLEPUNSEEN_CONTEXT + 1;
	
	private ProgressDialog m_ProgressDialog;
	
	private static ListView lv;
	
	public static SeriesSeasonsAdapter seriesseasons_adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serie_seasons);
		
        serieid = getIntent().getStringExtra("serieid");
        
        setTitle(droidseries.db.getSerieName(serieid) + " - " + getString(R.string.messages_seasons));
        
        seasons = new ArrayList<Season>();
        
        //sets the progress dialog that shows when seasons are loading
        m_ProgressDialog = new ProgressDialog(this);
        m_ProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        m_ProgressDialog.setMessage("Loading seasons...");
        m_ProgressDialog.setIndeterminate(true);        
        m_ProgressDialog.show();
        
        Thread thread =  new Thread(null, returnRes, "MagentoBackground");
        thread.start();
        
        
        seriesseasons_adapter = new SeriesSeasonsAdapter(this, R.layout.row_serie_seasons, seasons);       
        setListAdapter(seriesseasons_adapter);
        
        //context menu
        registerForContextMenu(getListView());
        
        lv = getListView();
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	try {
            		Intent serieEpisode = new Intent(SerieSeasons.this, SerieEpisodes.class);
            		serieEpisode.putExtra("serieid", serieid);
            		serieEpisode.putExtra("nseason", iseasons.get(position));
            		startActivity(serieEpisode);
            	} catch (Exception e) {
            		Log.e(TAG, e.getMessage());
            	}
            }
        });
	}
	
	/* context menu */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, ALLEPSEEN_CONTEXT, 0,  getString(R.string.messages_context_mark_seasonseen));
		
		menu.add(0, ALLEPUNSEEN_CONTEXT, 0, getString(R.string.messages_context_mark_seasonunseen));
		
		menu.add(0, ALLUPTOTHIS_CONTEXT, 0, getString(R.string.messages_context_mark_asseenuptothis));
	}
		
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int nseason = 0;
		switch (item.getItemId()) {
			case  ALLEPSEEN_CONTEXT:
				nseason = iseasons.get(info.position);
				
				droidseries.db.updateUnwatchedSeason(serieid, nseason);
				
				runOnUiThread(SerieSeasons.updateListView);
				
				Runnable changeEpisodeSeen = new Runnable(){
			        @Override
			        public void run() {
			        	runOnUiThread(droidseries.updateListView);
			        }
				};
				
				Thread thCES =  new Thread(null, changeEpisodeSeen, "MagentoBackground");
		    	thCES.start();
		    	
				return true;
			case ALLEPUNSEEN_CONTEXT:
				nseason = iseasons.get(info.position);
				
				droidseries.db.updateWatchedSeason(serieid, nseason);
				
				runOnUiThread(SerieSeasons.updateListView);
				
				Runnable changeEpisodeUnSeen = new Runnable(){
			        @Override
			        public void run() {
			        	runOnUiThread(droidseries.updateListView);
			        }
				};
				
				Thread thCEU =  new Thread(null, changeEpisodeUnSeen, "MagentoBackground");
		    	thCEU.start();
		    	
				return true;
			case ALLUPTOTHIS_CONTEXT:
				nseason = iseasons.get(info.position);
				
				for(int i=1; i <= nseason; i++) {
					droidseries.db.updateUnwatchedSeason(serieid, i);
				}
				runOnUiThread(SerieSeasons.updateListView);
				
				Runnable changeSeasonsSeen = new Runnable(){
			        @Override
			        public void run() {
			        	runOnUiThread(droidseries.updateListView);
			        }
				};
				
				Thread thCSS =  new Thread(null, changeSeasonsSeen, "MagentoBackground");
		    	thCSS.start();
		    	
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private void getSeasons(){
		Runnable updateList = new Runnable() {
            @Override
            public void run() {
            	seriesseasons_adapter.notifyDataSetChanged();
            }
        };
        
        try {
        	Log.i(TAG, "!!! Reloading seasons!");
        	iseasons = new ArrayList<Integer>();
            Cursor cseasons = droidseries.db.Query("SELECT season FROM serie_seasons WHERE serieId = '" + serieid + "'");
            cseasons.moveToFirst();
            if (cseasons.getCount() != 0) {
    	        do {
    	        	iseasons.add( cseasons.getInt(0) );
    	        } while( cseasons.moveToNext() );
            }
            cseasons.close();
            
            for(int i=0; i < iseasons.size(); i++) {
				String tmpSeason = "";
	            if(iseasons.get(i) == 0) {
	            	tmpSeason = getString(R.string.messages_specials);
	            }
	            else {
	            	tmpSeason = getString(R.string.messages_season) + " " + iseasons.get(i);
	            }
	            
	            boolean completelyWatched = false;
		        int epNotSeen = droidseries.db.getSeasonEPWatched(serieid, iseasons.get(i));
		        
		        if(epNotSeen != -1) {
			        if(epNotSeen == 0) {
			            completelyWatched = true;
			        }
		        }
		        
				int visibility = 0;
				String tmpNextEpisode = "";
		        if(!completelyWatched) {
		        	tmpNextEpisode = droidseries.db.getNextEpisode(serieid, iseasons.get(i));
		        	visibility = View.VISIBLE;
	            }
	            else {
	            	visibility = View.GONE;
	            }
		        
		        Season season = new Season(serieid, iseasons.get(i), tmpSeason, epNotSeen, completelyWatched, tmpNextEpisode, visibility);
            	seasons.add(season);
            }
          } catch (Exception e) { 
            Log.e(TAG, "Error getting seasons");
          }
          
          //this only updates the list after getting all the seasons
      	  runOnUiThread(updateList);
          m_ProgressDialog.dismiss();
    }
	
	private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
        	getSeasons();
        }
    };
	
	public static Runnable updateListView = new Runnable() {
        @Override
        public void run() {
        	for(int i = 0; i < seasons.size(); i++) {
        		int epNotSeen = droidseries.db.getSeasonEPWatched(seasons.get(i).getSerieId(), seasons.get(i).getSNumber());
        		if (seasons.get(i).getEpNotSeen() != epNotSeen) {
        			seasons.get(i).setEpNotSeen(epNotSeen);
        			if(epNotSeen != 0) {
        				seasons.get(i).setCompletelyWatched(false);
			        }
			        else {
			        	seasons.get(i).setCompletelyWatched(true);
			        }
        			
        			if(!seasons.get(i).getCompletelyWatched()) {
        				seasons.get(i).setNextEpisode(droidseries.db.getNextEpisode(seasons.get(i).getSerieId(), seasons.get(i).getSNumber()));
        				seasons.get(i).setVisibility(View.VISIBLE);
     	            }
     	            else {
     	            	seasons.get(i).setVisibility(View.GONE);
     	            }
        			seriesseasons_adapter.notifyDataSetChanged();
        		}
        	}
        	seriesseasons_adapter.notifyDataSetChanged();
        }
    };
	
	private class SeriesSeasonsAdapter extends ArrayAdapter<Season> {

		private List<Season> items;
			
        public SeriesSeasonsAdapter(Context context, int textViewResourceId, List<Season> seasons) {
			super(context, textViewResourceId, seasons);
			this.items = seasons;
		}

        public View getView(int position, View convertView, ViewGroup parent) {
        	final ViewHolder holder;
	        
        	if (convertView == null) {
        		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            convertView = vi.inflate(R.layout.row_serie_seasons, null);
	            
	            holder = new ViewHolder();
		        holder.sn = (TextView) convertView.findViewById(R.id.serieseason);
		        holder.si = (TextView) convertView.findViewById(R.id.seasoninfo);
		        
		        convertView.setTag(holder);
	        }
        	else {
        		holder = (ViewHolder) convertView.getTag();
        	}
	                
	        Season s = items.get(position);
	                        
	        if (holder.sn != null) {
	            holder.sn.setText( s.getSeason() );
	        }
	                              
	        String infoPart1 = "";
	        if(holder.si != null) {
	        	int epNotSeen = s.getEpNotSeen();
	        	if(epNotSeen != -1) {
			        if(epNotSeen != 0) {
			        	if(epNotSeen == 1) {
			        		infoPart1 = epNotSeen + " " + getString(R.string.messages_episode_not_watched);
			        	}
			        	else {
			        		infoPart1 = epNotSeen + " " + getString(R.string.messages_episodes_not_watched);
			        	}
			        }
			        else {
			        	infoPart1 = getString(R.string.messages_season_completely_watched);
			        }
		        }
	        	
	        	if(!s.getCompletelyWatched()) {
	            	holder.si.setText(infoPart1 + "\n" + getString(R.string.messages_next_episode) + " " + s.getNextEpisode());
	            }
	            else {
	            	holder.si.setText(infoPart1);
	            }
	        	
	        }
	        return convertView;
        }
    }
	
	static class ViewHolder {
		TextView sn;
        TextView si;
	}
}