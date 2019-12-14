package nl.asymmetrics.droidshows.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.thetvdb.TheTVDB;
import nl.asymmetrics.droidshows.thetvdb.model.Serie;
import nl.asymmetrics.droidshows.thetvdb.model.TVShowItem;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import nl.asymmetrics.droidshows.utils.Utils;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import org.apache.commons.io.FileUtils;

public class AddSerie extends ListActivity
{
	private static List<Serie> search_series = null;
	private TheTVDB theTVDB;
	private SeriesSearchAdapter seriessearch_adapter;
	/* DIALOGS */
	private ProgressDialog m_ProgressDialog = null;
	/* Option Menus */
	private static final int ADD_SERIE_MENU_ITEM = Menu.FIRST;
	/* Context Menus */
	private static final int ADD_CONTEXT = Menu.FIRST;
	private ListView listView;
	private Utils utils = new Utils();
	static String searchQuery = "";
	private SQLiteStore db;
	private List<String> series;
	private String langCode = DroidShows.langCode;
	private AsyncAddSerie addSerieTask = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_serie);
		db = SQLiteStore.getInstance(this);
		series = db.getSeries(2, false, null);	// 2 = archive and current shows, false = don't filter networks, null = ignore networks filter
		List<Serie> search_series = new ArrayList<Serie>();
		this.seriessearch_adapter = new SeriesSearchAdapter(this, R.layout.row_search_series, search_series);
		setListAdapter(seriessearch_adapter);
		((TextView) findViewById(R.id.change_language)).setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
		Intent intent = getIntent();
		getSearchResults(intent);
	}

	/* Options Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.menu_add_serie)).setIcon(android.R.drawable.ic_menu_add);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case ADD_SERIE_MENU_ITEM :
				onSearchRequested();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/* context menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, ADD_CONTEXT, 0, getString(R.string.menu_context_add_serie));
	}

	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final ListView serieList = getListView();
		switch (item.getItemId()) {
			case ADD_CONTEXT :
				final Serie tmpSerie = (Serie) serieList.getAdapter().getItem(info.position);
				addSerie(tmpSerie);
				return true;
			default :
				return super.onContextItemSelected(item);
		}
	}
	private Runnable reloadSearchSeries = new Runnable() {
		public void run() {
			seriessearch_adapter.clear();
			if (search_series != null && search_series.size() > 0) {
				seriessearch_adapter.notifyDataSetChanged();
				for (int i = 0; i < search_series.size(); i++)
					seriessearch_adapter.add(search_series.get(i));
			}
			seriessearch_adapter.notifyDataSetChanged();
			m_ProgressDialog.dismiss();
		}
	};

	private void searchSeries(String searchQuery) {
		try {
			search_series = new ArrayList<Serie>();
			search_series = theTVDB.searchSeries(searchQuery, langCode);
			if (search_series == null) {
				m_ProgressDialog.dismiss();
				Looper.prepare();
					Toast.makeText(getApplicationContext(), R.string.messages_thetvdb_con_error, Toast.LENGTH_LONG).show();
				Looper.loop();
			} else {
				runOnUiThread(reloadSearchSeries);
			}
		} catch (Exception e) {
			Log.e(SQLiteStore.TAG, e.getMessage());
		}
	}

	private void Search() {
		m_ProgressDialog = ProgressDialog.show(AddSerie.this, getString(R.string.messages_title_search_series), getString(R.string.messages_search_series), true, true);
		new Thread(new Runnable() {
			public void run() {
				theTVDB = new TheTVDB("8AC675886350B3C3", DroidShows.useMirror);
				searchSeries(searchQuery);
			}
		}).start();
	}

	public void changeLanguage(View v) {
		AlertDialog.Builder changeLang = new AlertDialog.Builder(this);
		changeLang.setTitle(R.string.dialog_change_language)
			.setItems(R.array.languages, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					langCode = getResources().getStringArray(R.array.langcodes)[item];
					TextView changeLangB = (TextView) findViewById(R.id.change_language);
					changeLangB.setText(getString(R.string.dialog_change_language) +" ("+ langCode +")");
					doSearch();
				}
			})
			.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		m_ProgressDialog.dismiss();
		super.onSaveInstanceState(outState);
	}
	
	private void addSerie(Serie s) {
		if (addSerieTask == null || addSerieTask.getStatus() != AsyncTask.Status.RUNNING) {
			addSerieTask = new AsyncAddSerie();
			addSerieTask.execute(s);
		} else {
			Log.d(SQLiteStore.TAG, "Still busy, not adding "+ s.getSerieName());
			Toast.makeText(getApplicationContext(), R.string.messages_error_dbupdate, Toast.LENGTH_SHORT).show();
		}
	}

	private class AsyncAddSerie extends AsyncTask<Serie, Void, Boolean> {
		String msg = null;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			m_ProgressDialog = ProgressDialog.show(AddSerie.this, getString(R.string.messages_title_adding_serie), getString(R.string.messages_adding_serie), true, false);
		}

		protected Boolean doInBackground(Serie... params) {
			Serie s = params[0];
			Boolean success = false;
			
			boolean alreadyExists = false;
			for (String serieId : series)
				if (serieId.equals(s.getId())) {
					alreadyExists = true;
					break;
				}
			if (alreadyExists) return false;
			
			Serie sToAdd = theTVDB.getSerie(s.getId(), s.getLanguage());
			if (sToAdd == null) {
				msg = getString(R.string.messages_thetvdb_con_error);
			} else {
				Log.d(SQLiteStore.TAG, "Adding "+ sToAdd.getSerieName() +": getting the poster");
				// get the poster and save it in cache
				URL imageUrl = null;
				URLConnection uc = null;
				String contentType = "";
				try {
					try {
						imageUrl = new URL(sToAdd.getPoster());
						uc = imageUrl.openConnection();
						// timetout, 10s for slow connections
						uc.setConnectTimeout(10000);
						contentType = uc.getContentType();
					} catch (MalformedURLException e) {
						Log.e(SQLiteStore.TAG, e.getMessage());
					} catch (IOException e) {
						Log.e(SQLiteStore.TAG, e.getMessage());
					} catch (Exception e) {
						Log.d(SQLiteStore.TAG, e.getMessage());
					}
					int contentLength = uc.getContentLength();
					if (!TextUtils.isEmpty(contentType)) {
						if (!contentType.startsWith("image/") || contentLength == -1) {
							// throw new IOException("This is not a binary file.");
							Log.e(SQLiteStore.TAG, "This is not a image.");
						}
					}
					try {
						File cacheImage = new File(getApplicationContext().getFilesDir().getAbsolutePath()
							+ imageUrl.getFile().toString());
						FileUtils.copyURLToFile(imageUrl, cacheImage);
						Bitmap posterThumb = BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getAbsolutePath() + imageUrl.getFile().toString());
						Display display = getWindowManager().getDefaultDisplay();
						int width = display.getWidth();
						int height = display.getHeight();
						int newHeight = (int) ((height > width ? height : width) * 0.265);
						int newWidth = (int) (1.0 * posterThumb.getWidth() / posterThumb.getHeight() * newHeight);
						Log.d(SQLiteStore.TAG, "Adding "+ sToAdd.getSerieName() +": resizing the poster and creating the thumbnail");
						Bitmap resizedBitmap = Bitmap.createScaledBitmap(posterThumb, newWidth, newHeight, true);
						File dirTmp = new File(getApplicationContext().getFilesDir().getAbsolutePath() +"/thumbs/banners/posters");
						if (!dirTmp.isDirectory()) {
							dirTmp.mkdirs();
						}
						OutputStream fOut = null;
						File thumFile = new File(getApplicationContext().getFilesDir().getAbsolutePath(), "thumbs"
							+ imageUrl.getFile().toString());
						fOut = new FileOutputStream(thumFile);
						resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
						fOut.flush();
						fOut.close();
						// removes the bitmaps from memory
						posterThumb.recycle();
						resizedBitmap.recycle();
						System.gc();
						posterThumb = null;
						resizedBitmap = null;
						sToAdd.setPosterThumb(getApplicationContext().getFilesDir().getAbsolutePath()
							+ "/thumbs" + imageUrl.getFile().toString());
						cacheImage.delete();
						sToAdd.setPosterInCache("true");
					} catch (Exception e) {
						sToAdd.setPosterInCache("");
						Log.e(SQLiteStore.TAG, "Error copying the poster to cache.");
					}
				} catch (Exception e) {
					Log.e(SQLiteStore.TAG, e.getMessage());
				}
				try {
					Log.d(SQLiteStore.TAG, "Adding "+ sToAdd.getSerieName() +": saving TV show to database");
					sToAdd.setPassiveStatus((DroidShows.showArchive == 1 ? 1 : 0));
					sToAdd.saveToDB(db);
					Log.d(SQLiteStore.TAG, "Adding "+ sToAdd.getSerieName() +": creating the TV show item");
					int nseasons = db.getSeasonCount(sToAdd.getId());
					SQLiteStore.NextEpisode nextEpisode = db.getNextEpisode(sToAdd.getId());
					int unwatchedAired = db.getEPUnwatchedAired(sToAdd.getId());
					int unwatched = db.getEPUnwatched(sToAdd.getId());
					String nextEpisodeStr = db.getNextEpisodeString(nextEpisode, DroidShows.showNextAiring && 0 < unwatchedAired && unwatchedAired < unwatched);
					Drawable d = Drawable.createFromPath(sToAdd.getPosterThumb());
					TVShowItem tvsi = new TVShowItem(sToAdd.getId(), sToAdd.getLanguage(), sToAdd.getPosterThumb(), d, sToAdd.getSerieName(), nseasons,
						nextEpisodeStr, nextEpisode.firstAiredDate, unwatchedAired, unwatched, sToAdd.getPassiveStatus() == 1,
						(sToAdd.getStatus() == null ? "null" : sToAdd.getStatus()), "");
					DroidShows.series.add(tvsi);
					series.add(sToAdd.getId());
					runOnUiThread(DroidShows.updateListView);
					success = true;
				} catch (Exception e) {
					Log.e(SQLiteStore.TAG, "Error adding "+ sToAdd.getSerieName());
				}
				if (success) {
					msg = String.format(getString(R.string.messages_series_success), sToAdd.getSerieName())
						+ (DroidShows.showArchive == 1 ? " ("+ getString(R.string.messages_context_archived) +")": "");
				}
			}
			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			runOnUiThread(reloadSearchSeries);
			if (msg != null) Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			m_ProgressDialog.dismiss();
		}

		@Override
		protected void onCancelled(Boolean result) {
			this.onPostExecute(result);
			super.onCancelled();
		}
	}
	
	// Guillaume: searches from within this activity were discarded
	@Override
	protected void onNewIntent(Intent intent) {
		getSearchResults(intent);
	}

	private void getSearchResults(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			searchQuery = intent.getStringExtra(SearchManager.QUERY);
			if (searchQuery.length() == 0) {
				onSearchRequested();
				return;
			}
			TextView title = (TextView) findViewById(R.id.add_serie_title);
			title.setText(getString(R.string.dialog_search) + " " + searchQuery);
			doSearch();
		}
		listView = getListView();
		listView.setOnTouchListener(new SwipeDetect());
		registerForContextMenu(getListView());
	}
	
	private void doSearch() {
		if (utils.isNetworkAvailable(AddSerie.this))
			Search();
		else
			Toast.makeText(getApplicationContext(), R.string.messages_no_internet, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final Serie sToAdd = AddSerie.search_series.get(position);
		AlertDialog sOverview = new AlertDialog.Builder(this)
		.setIcon(R.drawable.icon)
		.setTitle(sToAdd.getSerieName())
		.setMessage(sToAdd.getOverview())
		.setPositiveButton(getString(R.string.menu_context_add_serie), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				addSerie(sToAdd);
			}
		})
		.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.show();
		
		for (String serieId : series)
			if (serieId.equals(sToAdd.getId())) {
				sOverview.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				break;
			}
	}
	
	private class SeriesSearchAdapter extends ArrayAdapter<Serie>
	{
		private List<Serie> items;

		public SeriesSearchAdapter(Context context, int textViewResourceId, List<Serie> series) {
			super(context, textViewResourceId, series);
			this.items = series;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.row_search_series, parent, false);
			}
			final Serie o = items.get(position);
			if (o != null) {
				TextView sn = (TextView) v.findViewById(R.id.seriename);
				CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.addserieBtn);
				if (sn != null) {
					String lang = (o.getLanguage() == null ? "" : " ("+ o.getLanguage() +")");
					sn.setText(o.getSerieName() + lang);
				}
				if (ctv != null) {
					boolean alreadyExists = false;
					for (String serieId : series) {
						if (serieId.equals(o.getId())) {
							alreadyExists = true;
							break;
						}
					}
					if (alreadyExists) {
						ctv.setCheckMarkDrawable(getResources().getDrawable(R.drawable.star));
						// ctv.setVisibility(View.GONE);
						ctv.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								// does nothing
								return;
							}
						});
					} else {
						// ctv.setVisibility(View.VISIBLE);
						ctv.setCheckMarkDrawable(getResources().getDrawable(R.drawable.add));
						ctv.setOnClickListener(new OnClickListener() {
							public void onClick(View v) {
								addSerie(o);
							}
						});
					}
				}
			}
			return v;
		}
	}
}