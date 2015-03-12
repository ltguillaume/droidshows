package org.droidseries.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.droidseries.R;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import org.droidseries.droidseries;
import org.droidseries.thetvdb.TheTVDB;
import org.droidseries.thetvdb.model.Serie;
import org.droidseries.thetvdb.model.TVShowItem;
import org.droidseries.utils.Utils;

public class AddSerie extends ListActivity
{
	private final String TAG = "DroidSeries";
	private static List<Serie> search_series = null;
	private TheTVDB theTVDB;
	private SeriesSearchAdapter seriessearch_adapter;
	/* DIALOGS */
	private ProgressDialog m_ProgressDialog = null;
	// private final static int ID_DIALOG_SEARCHING = 1;
	// private final static int ID_DIALOG_ADD = 2;
	/* Option Menus */
	private static final int ADD_SERIE_MENU_ITEM = Menu.FIRST;
	/* Context Menus */
	private static final int ADD_CONTEXT = Menu.FIRST;
	private ListView lv;
	private Utils utils = new Utils();
	static String searchQuery = "";
	// public static Thread threadAddShow = null;
	private volatile Thread threadAddShow;
	private static boolean bAddShowTh = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE); // Guillaume--
		setContentView(R.layout.add_serie);
		List<Serie> search_series = new ArrayList<Serie>();
		this.seriessearch_adapter = new SeriesSearchAdapter(this, R.layout.row_search_series, search_series);
		setListAdapter(seriessearch_adapter);
		Intent intent = getIntent();
		getSearchResults(intent); // Guillaume
	}

	/* Options Menu */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_SERIE_MENU_ITEM, 0, getString(R.string.search_serie)).setIcon(android.R.drawable.ic_menu_search);
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
			m_ProgressDialog.dismiss();
			seriessearch_adapter.notifyDataSetChanged();
		}
	};

	private void searchSeries(String searchQuery) {
		try {
			search_series = new ArrayList<Serie>();
			search_series = theTVDB.searchSeries(searchQuery, getString(R.string.lang_code));
			if (search_series == null) {
				// dismissDialog(AddSerie.ID_DIALOG_SEARCHING);
				m_ProgressDialog.dismiss();
				CharSequence text = getText(R.string.messages_thetvdb_con_error);
				int duration = Toast.LENGTH_LONG;
				Looper.prepare();
				Toast toast = Toast.makeText(getApplicationContext(), text, duration);
				toast.show();
				Looper.loop();
			} else {
				runOnUiThread(reloadSearchSeries);
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	/* This methods handle the progress dialogs */
	/* @Override protected Dialog onCreateDialog(int id) { if(id == ID_DIALOG_SEARCHING){
	 * ProgressDialog m_ProgressDialog = new ProgressDialog(this);
	 * m_ProgressDialog.setMessage(getString(R.string.messages_search_series));
	 * m_ProgressDialog.setIndeterminate(true); m_ProgressDialog.setCancelable(true); return
	 * m_ProgressDialog; } else if(id == ID_DIALOG_ADD) { ProgressDialog m_ProgressDialog = new
	 * ProgressDialog(this); m_ProgressDialog.setMessage(getString(R.string.messages_adding_serie));
	 * m_ProgressDialog.setIndeterminate(true); m_ProgressDialog.setCancelable(true); return
	 * m_ProgressDialog; } return super.onCreateDialog(id); } protected void removeDialogs() { try {
	 * dismissDialog(ID_DIALOG_SEARCHING); removeDialog(ID_DIALOG_SEARCHING); } catch (Exception e) {
	 * //Log.d(TAG, "ID_DIALOG_SEARCHING - Remove Failed", e); } try { dismissDialog(ID_DIALOG_ADD);
	 * removeDialog(ID_DIALOG_ADD); } catch (Exception e) { //Log.d(TAG,
	 * "ID_DIALOG_SEARCHING - Remove Failed", e); } } */
	private void Search() {
		// showDialog(ID_DIALOG_SEARCHING);
		m_ProgressDialog = ProgressDialog.show(AddSerie.this, getString(R.string.messages_title_search_series), getString(R.string.messages_search_series), true, true);
		new Thread(new Runnable() {
			public void run() {
				theTVDB = new TheTVDB("8AC675886350B3C3");
				if (theTVDB.getMirror() != null) {
					searchSeries(searchQuery);
				} else {
					Log.e(TAG, "Error searching for TV shows");
				}
			}
		}).start();
	}

	public synchronized void startAddShowTh() {
		if (threadAddShow == null) {
			threadAddShow = new Thread();
			threadAddShow.start();
		}
	}

	public synchronized void stopAddShowTh() {
		if (threadAddShow != null) {
			Thread moribund = threadAddShow;
			threadAddShow = null;
			moribund.interrupt();
			Log.d(TAG, "addShow Thread stopped");
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		m_ProgressDialog.dismiss();
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onPause() {
		if (threadAddShow != null) {
			if (threadAddShow.isAlive()) {
				Log.i(TAG, "!!! Thread is running!");
				bAddShowTh = true;
			}
		}
		super.onPause();
	}

	public void addSerie(final Serie s) {
		Runnable addnewserie = new Runnable() {
			public void run() {
				// gathers the TV show and all of its data
				Serie sToAdd = theTVDB.getSerie(s.getId(), getString(R.string.lang_code));
				if (sToAdd == null) {
					m_ProgressDialog.dismiss();
					// dismissDialog(AddSerie.ID_DIALOG_ADD);
					CharSequence text = getText(R.string.messages_thetvdb_con_error);
					int duration = Toast.LENGTH_LONG;
					Looper.prepare();
					Toast.makeText(getApplicationContext(), text, duration).show();
					Looper.loop();
				} else {
					Log.d(TAG, "Adding TV show: getting the poster");
					// get the poster and save it in cache
					URL imageUrl = null;
					URLConnection uc = null;
					String contentType = "";
					try {
						if (bAddShowTh) {
							stopAddShowTh();
							bAddShowTh = false;
							return;
						}
						try {
							imageUrl = new URL(sToAdd.getPoster());
							uc = imageUrl.openConnection();
							// timetout, 20s for slow connections
							uc.setConnectTimeout(10000);
							contentType = uc.getContentType();
						} catch (MalformedURLException e) {
							Log.e(TAG, e.getMessage());
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
						} catch (Exception e) {
							Log.d(TAG, e.getMessage());
						}
						int contentLength = uc.getContentLength();
						if (!TextUtils.isEmpty(contentType)) {
							if (!contentType.startsWith("image/") || contentLength == -1) {
								// throw new IOException("This is not a binary file.");
								Log.e(TAG, "This is not a image.");
							}
						}
						try {
							if (bAddShowTh) {
								stopAddShowTh();
								bAddShowTh = false;
								return;
							}
							File cacheImage = new File(getApplicationContext().getFilesDir().getAbsolutePath()
								+ imageUrl.getFile().toString());
							FileUtils.copyURLToFile(imageUrl, cacheImage);
							Bitmap posterThumb = BitmapFactory.decodeFile(getApplicationContext().getFilesDir().getAbsolutePath() + imageUrl.getFile().toString());
							Display display = getWindowManager().getDefaultDisplay();
							int width = display.getWidth();
							int height = display.getHeight();
							int newHeight = (int) ((height > width ? height : width) * 0.265);
							int newWidth = (int) (1.0 * posterThumb.getWidth() / posterThumb.getHeight() * newHeight);
							if (bAddShowTh) {
								stopAddShowTh();
								bAddShowTh = false;
								return;
							}
							Log.d(TAG, "Adding TV show: resizing the poster and creating the thumbnail");
							Bitmap resizedBitmap = Bitmap.createScaledBitmap(posterThumb, newWidth, newHeight, true);
							File dirTmp = new File(getApplicationContext().getFilesDir().getAbsolutePath() +"/thumbs/banners/posters");
							if (!dirTmp.isDirectory()) {
								dirTmp.mkdirs();
							}
							OutputStream fOut = null;
							File thumFile = new File(getApplicationContext().getFilesDir().getAbsolutePath(), "thumbs"
								+ imageUrl.getFile().toString());
							// fOut = openFileOutput(getApplicationContext().getFilesDir().getAbsolutePath() +
							// "/thumbs" + imageUrl.getFile().toString(), Context.MODE_PRIVATE);
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
							Log.e(TAG, "Error copying the poster to cache.");
						}
					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
					}
					boolean sucesso = false;
					try {
						if (bAddShowTh) {
							stopAddShowTh();
							bAddShowTh = false;
							return;
						}
						Log.d(TAG, "Adding TV show: saving TV show to database");
						// if(sToAdd.getSerieId() == null) {
						// Log.d(TAG, "NULL SERIE ID! HAVE TO CHANGE THIS!");
						// }
						sToAdd.saveToDB(droidseries.db);
						Log.d(TAG, "Adding TV show: creating the TV show item");
						int nseasons = droidseries.db.getSeasonCount(sToAdd.getId());
						String nextEpisode = droidseries.db.getNextEpisode(sToAdd.getId(), -1).replace("[on]", droidseries.on);
						Date nextAir = droidseries.db.getNextAir(sToAdd.getId(), -1);
						int unwatchedAired = droidseries.db.getEPUnwatchedAired(sToAdd.getId());
						int unwatched = droidseries.db.getEPUnwatched(sToAdd.getId());
						Drawable d = Drawable.createFromPath(sToAdd.getPosterThumb());
						TVShowItem tvsi = new TVShowItem(sToAdd.getId(), sToAdd.getPosterThumb(), d, sToAdd.getSerieName(), nseasons, nextEpisode, nextAir, unwatchedAired, unwatched, false, sToAdd.getStatus());
						droidseries.series.add(tvsi);
						runOnUiThread(droidseries.updateListView);
						runOnUiThread(reloadSearchSeries);
						sucesso = true;
					} catch (Exception e) {
						Log.e(TAG, "Error adding TV show");
					}
					// m_ProgressDialog.dismiss();
					if (!bAddShowTh) {
						// dismissDialog(AddSerie.ID_DIALOG_ADD);
						m_ProgressDialog.dismiss();
						bAddShowTh = false;
					}
					if (sucesso) {
						CharSequence text = String.format(getString(R.string.messages_series_success), sToAdd.getSerieName());
						int duration = Toast.LENGTH_LONG;
						Looper.prepare();
						Toast toast = Toast.makeText(getApplicationContext(), text, duration);
						toast.show();
						Looper.loop();
						// TODO: automatically close the intent
						/* handler.post(new Runnable() { public void run() { AddSerie.this.dispatchKeyEvent(new
						 * KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)); AddSerie.this.finish(); } }); */
					}
				}
			}
		};
		boolean alreadyExists = false;
		for (int i = 0; i < droidseries.series.size(); i++) {
			if (droidseries.series.get(i).getSerieId().equals(s.getSerieId())) {
				alreadyExists = true;
				break;
			}
		}
		if (!alreadyExists) {
			// showDialog(ID_DIALOG_ADD);
			threadAddShow = new Thread(null, addnewserie, "MagentoBackground");
			threadAddShow.start();
			m_ProgressDialog = ProgressDialog.show(AddSerie.this, getString(R.string.messages_title_adding_serie), getString(R.string.messages_adding_serie), true, false);
		}
	}

	/* @Override protected void onResume(){ if (threadAddShow != null) { if (threadAddShow.isAlive())
	 * { showDialog(ID_DIALOG_ADD); } } super.onResume(); } */
	// Guillaume: searches from within this activity were discarded
	@Override
	protected void onNewIntent(Intent intent) {
		getSearchResults(intent);
	}

	private void getSearchResults(Intent intent) { // Guillaume: was in onCreate() before
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			searchQuery = intent.getStringExtra(SearchManager.QUERY);
			TextView title = (TextView) findViewById(R.id.add_serie_title);
			title.setText(getString(R.string.dialog_search) + " " + searchQuery);
			if (utils.isNetworkAvailable(AddSerie.this)) {
				Search();
			} else {
				CharSequence text = getString(R.string.messages_no_internet);
				int duration = Toast.LENGTH_LONG;
				Toast toast = Toast.makeText(getApplicationContext(), text, duration);
				toast.show();
			}
		}
		lv = getListView();
		lv.setOnTouchListener(new SwipeDetect());
		// register context menu
		registerForContextMenu(getListView());
	}
	// END Guillaume
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		try {
			Intent serieOverview = new Intent(AddSerie.this, SerieOverview.class);
			if (!AddSerie.search_series.get(position).getOverview().equals("")) {
				serieOverview.putExtra("serieId", AddSerie.search_series.get(position).getId());
				serieOverview.putExtra("overview", AddSerie.search_series.get(position).getOverview());
				serieOverview.putExtra("name", AddSerie.search_series.get(position).getSerieName());
				startActivity(serieOverview);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error showing the serie's overview");
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
					sn.setText(o.getSerieName());
				}
				if (ctv != null) {
					boolean alreadyExists = false;
					for (int i = 0; i < droidseries.series.size(); i++) {
						if (droidseries.series.get(i).getSerieId().equals(o.getId())) {
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
								bAddShowTh = false;
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