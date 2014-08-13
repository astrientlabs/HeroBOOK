package com.brilliancemobility.heroes;

import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.brilliancemobility.heroes.db.BookmarkRecord;
import com.brilliancemobility.heroes.db.Record;
import com.brilliancemobility.heroes.net.MarvelCharacter;
import com.brilliancemobility.heroes.net.Thumbnail;
import com.brilliancemobility.heroes.net.VolleySingleton;
import com.brilliancemobility.heroes.util.Days;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class BookmarksFragment extends ListFragment implements OnItemLongClickListener, LoaderManager.LoaderCallbacks<Cursor>
{
    // These are the Contacts rows that we will retrieve.
    static final String[] PROJECTION = BookmarkRecord.COLUMNS;
    
    
    private SimpleCursorAdapter mAdapter;
	private View mContentView;


	public static BookmarksFragment newInstance(String param1, String param2)
	{
		BookmarksFragment fragment = new BookmarksFragment();
		return fragment;
	}

	public BookmarksFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        mAdapter = new ListSimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_2, null,
                new String[] {
                        BookmarkRecord.COL_URL, BookmarkRecord.COL_TITLE
                },
                new int[] {
                        android.R.id.text1, android.R.id.text2
                }, 0);
        setListAdapter(mAdapter);
        
        getLoaderManager().initLoader(0, null, this);
	}
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	mContentView = inflater.inflate(R.layout.fragment_sidelist, null);
    	
    	((TextView)mContentView.findViewById(R.id.title)).setText(R.string.title_bookmarks);

    	ListView listView = (ListView)mContentView.findViewById(android.R.id.list);
    	listView.setOnItemLongClickListener(this);
                
        return mContentView;
    }	
	

	@Override
	public void onListItemClick(ListView l, View view, int position, long id)
	{
		Cursor cursor = (Cursor) mAdapter.getItem(position);
		BookmarkRecord record = new BookmarkRecord();
		record.load(cursor);
		
		MarvelCharacter mc = record.getMarvelCharacter();

		Bitmap bitmap = null;
		ImageView imageView = (ImageView)view.findViewById(R.id.icon);
		if ( imageView != null )
		{
			if ( imageView.getDrawable() instanceof BitmapDrawable )
			{
				BitmapDrawable bd = (BitmapDrawable)imageView.getDrawable();
				bitmap = bd.getBitmap();
			}
		}		
		
		Tools.showCharacter(getActivity(), bitmap, null, null, mc, record.getPageNumber(), record.getPageSize(), 1, null);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> a, View view, int position, long id)
	{
        Uri uri = Uri.parse(BookmarkRecord.CONTENT_URI + "/" + Long.toString(id));
        getActivity().getContentResolver().delete(uri, null, null);		

		return true;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) 
    {
        Uri baseUri = BookmarkRecord.CONTENT_URI;
        
        String select = "((" + Record.COL_SYNC_STATE + " != " + Record.SYNC_STATE_DELETED + "))";
        String order = Record.COL_MODIFIED + " DESC";

        return new CursorLoader(getActivity(), baseUri, PROJECTION, select, null, order);
    }




    public void onLoadFinished(Loader<Cursor> loader, Cursor data) 
    {
        mAdapter.swapCursor(data);
    }




    public void onLoaderReset(Loader<Cursor> loader) 
    {
        mAdapter.swapCursor(null);
    }	
	
	
	class ListSimpleCursorAdapter extends SimpleCursorAdapter
	{
		LayoutInflater layoutInflater;
		Resources resources;
		Locale locale;
		
		public ListSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags)
		{
			super(context, layout, c, from, to, flags);
			// TODO Auto-generated constructor stub
			
			layoutInflater = LayoutInflater.from(context);
			resources = getResources();
			locale = Locale.getDefault();
		}
		
		
	    private class ViewHolder
	    {
	    	NetworkImageView imageView;
	    	TextView title; 
	    	TextView description; 
	    }
	    
	    public View getView(int position, View convertView, ViewGroup parent) 
	    {
	    	ViewHolder holder;
	    	if (convertView == null) 
	    	{
	    		convertView = layoutInflater.inflate(R.layout.row_sidelist, null);
		    	
	    		holder = new ViewHolder();
	    	    holder.imageView = (NetworkImageView) convertView.findViewById(R.id.icon);
	    	    holder.title = (TextView)convertView.findViewById(R.id.title);
	    	    holder.description = (TextView)convertView.findViewById(R.id.description);
	    	    
	    	    convertView.setTag(holder);   		
	    		
	    	}
	    	else 
	    	{
	    	    holder = (ViewHolder) convertView.getTag();
	    	}
	    	
	        try
	        {
	        	Cursor cursor = mAdapter.getCursor();
	        	cursor.moveToPosition(position);
	        	
	        	BookmarkRecord record = new BookmarkRecord();
	        	record.load(cursor);
	        	
		        	
	        	holder.title.setText(record.getTitle());
	        	holder.description.setText(Days.getDaysString(resources, record.getModified(), locale));
	        	
	        	
	        	MarvelCharacter mc = record.getMarvelCharacter();
	        	holder.imageView.setImageUrl(mc.thumbnail.getPicUrl(Thumbnail.SIZE.standard_small),VolleySingleton.getInstance(getActivity()).getImageLoader());
	        }
	        catch (Exception e)
	        {
	        	Log.e("null", "null", e);
	        }
	        
	        return convertView;
	    }
		
	}

}
