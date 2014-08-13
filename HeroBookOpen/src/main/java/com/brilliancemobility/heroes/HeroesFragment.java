package com.brilliancemobility.heroes;

import java.util.ArrayList;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.brilliancemobility.heroes.net.MarvelCharacter;
import com.brilliancemobility.heroes.net.MarvelData;
import com.brilliancemobility.heroes.net.MarvelResponse;
import com.brilliancemobility.heroes.net.REST;
import com.brilliancemobility.heroes.net.Thumbnail;
import com.brilliancemobility.heroes.net.VolleySingleton;
import com.brilliancemobility.heroes.util.AnalyticsUtils;


public class HeroesFragment extends Fragment implements OnScrollListener, OnItemLongClickListener, OnItemClickListener
{
	private Handler mHandler = new Handler();
    private GridView mGridView;   
    private GridAdapter mAdapter;
	private View mContentView;
	private int mPageSize = 20;
	private int mPageNumber;
	
	private boolean mActive;
	private int mTotal = Integer.MAX_VALUE;
	
	private MyCallBack mCallback;
	private boolean mSkipPhotoless = true;


	public static HeroesFragment newInstance(String param1, String param2)
	{
		HeroesFragment fragment = new HeroesFragment();
		return fragment;
	}

	public HeroesFragment()
	{
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mAdapter = new GridAdapter();
		
		startTask(mPageNumber);
	}
	
	
	public void reset()
	{
		mAdapter.clear();
		startTask(0);
	}
	
	public void search(String term)
	{
		mAdapter.clear();
		mCallback = new MyCallBack();
		mSkipPhotoless = false;
		
		REST.getMarvel(getActivity()).characters(term,0, mPageSize, "name", mCallback);
		AnalyticsUtils.trackSearch(getActivity(), term, 1);
		mActive = true;
		Tools.setBackgroundActivity(getActivity(), null, true);
	}
	
	private void startTask(int pageNumber)
	{
		mSkipPhotoless = true;
		mCallback = new MyCallBack();
		REST.getMarvel(getActivity()).characters(pageNumber*mPageSize, mPageSize, "name", mCallback);
		mActive = true;
		Tools.setBackgroundActivity(getActivity(), null, true);
	}
	
	
	private void endTask()
	{
		mActive = false;
		Tools.setBackgroundActivity(getActivity(), null, false);
	}
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	mContentView = inflater.inflate(R.layout.fragment_heroes, null);

    	mGridView = (GridView)mContentView.findViewById(android.R.id.list);
    	mGridView.setOnItemClickListener(this);
    	mGridView.setOnItemLongClickListener(this);
    	mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(this);
        
        return mContentView;
    }
    
    class MyCallBack implements Callback<MarvelResponse<MarvelCharacter>>
    {
    	@Override
    	public void failure(RetrofitError error)
    	{
    		if ( mCallback == this )
    		{
    			error.printStackTrace();
	    		endTask();
    		}
    	}


    	@Override
    	public void success(MarvelResponse<MarvelCharacter> marvelResponse, Response response)
    	{
    		if ( mCallback == this )
    		{
        		MarvelData<MarvelCharacter> data = marvelResponse.data;
        		if ( data != null )
        		{
        			mTotal = data.total;
            		for ( MarvelCharacter mc : data.results )
            		{
            			try
            			{
                			if ( !(mSkipPhotoless && mc.thumbnail.path.contains("http://i.annihil.us/u/prod/marvel/i/mg/b/40/image_not_available")) ) 
                			{
                				mAdapter.add(mc);
                			}
            			}
            			catch (Exception e)
            			{
            				Log.e("callback","getphoto", e);
            			}
            		}
        		}
        		else
        		{
        			mTotal = 0;
        		}
        		

        		endTask();
    		}
    	}
    }
    

	
	
    
    
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
	{
		int lastVisibleItem = (firstVisibleItem+visibleItemCount);
		
		if ( !mActive && totalItemCount < mTotal && lastVisibleItem >= totalItemCount )
		{
			startTask(++mPageNumber);
		}
	}



	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// TODO Auto-generated method stub
		
	}   
    
	
	@Override
	public boolean onItemLongClick(AdapterView<?> a, View view, int position, long id)
	{
		if ( a.getAdapter() == mAdapter )
		{
			final MarvelCharacter mc = mAdapter.getItem(position);
			Tools.toggleBookmark(getActivity(),mHandler,mc);
		}
		
		return true;
	}



	public void onItemClick(AdapterView<?> a, View view, int position, long id)
	{
		if ( a.getAdapter() == mAdapter )
		{
			MarvelCharacter mc = mAdapter.getItem(position);
			
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
			
			Tools.showCharacter(getActivity(), bitmap, mAdapter, null, mc, position, mPageNumber, mPageSize, null);
		}
	}
	

	class GridAdapter extends ArrayAdapter<MarvelCharacter>
	{
		LayoutInflater layoutInflater;
		Resources resources;
		Locale locale;
		
		public GridAdapter()
		{
			super(getActivity(), R.layout.cell_hero, new ArrayList<MarvelCharacter>());
			
			layoutInflater = LayoutInflater.from(getActivity());
			resources = getResources();
			locale = Locale.getDefault();
		}
		
		
	    private class ViewHolder
	    {
	    	NetworkImageView imageView;
	    	TextView title; 
	    }
	    
	    public View getView(int position, View convertView, ViewGroup parent) 
	    {
	    	ViewHolder holder;
	    	if (convertView == null) 
	    	{
	    		convertView = layoutInflater.inflate(R.layout.cell_hero_round, null);
		    	
	    		holder = new ViewHolder();
	    	    holder.imageView = (NetworkImageView) convertView.findViewById(R.id.icon);
	    	    holder.title = (TextView)convertView.findViewById(R.id.title);
	    	    
	    	    convertView.setTag(holder);
	    	}
	    	else 
	    	{
	    	    holder = (ViewHolder) convertView.getTag();
	    	}
	    	
	        try
	        {
	        	MarvelCharacter record = getItem(position);

	        	holder.title.setText(record.getAlias());
	        	holder.imageView.setImageUrl(record.thumbnail.getPicUrl(Thumbnail.SIZE.standard_xlarge),VolleySingleton.getInstance(getActivity()).getImageLoader());
	        }
	        catch (Exception e)
	        {
	        	Log.e("null", "null", e);
	        }
	        
	        return convertView;
	    }
		
	}

}
