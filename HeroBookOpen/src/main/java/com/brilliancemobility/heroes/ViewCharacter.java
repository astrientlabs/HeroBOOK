package com.brilliancemobility.heroes;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.brilliancemobility.heroes.net.Comic;
import com.brilliancemobility.heroes.net.MarvelCharacter;
import com.brilliancemobility.heroes.net.MarvelData;
import com.brilliancemobility.heroes.net.MarvelResponse;
import com.brilliancemobility.heroes.net.REST;
import com.brilliancemobility.heroes.util.AstrientViewPager;
import com.brilliancemobility.heroes.util.DeleteFile;
import com.brilliancemobility.heroes.util.ScrollChecker;
import com.ortiz.touch.TouchImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class ViewCharacter extends FragmentActivity implements OnPageChangeListener, ScrollChecker, Callback<MarvelResponse<MarvelCharacter>>
{
	public static ArrayList<MarvelCharacter> LAST_MODEL = null;
	public static Bitmap LAST_BITMAP = null;
	
	private Handler mHandler = new Handler();
	private MarvelCharacter mCurrent;
	private Comic mComic;
	private int mPosition;
	private int mPageSize;
	private int mPageNumber;
	
	private AstrientViewPager mViewPager;
	private PreviewFragmentAdapter mAdapter;
	private ArrayList<MarvelCharacter> mModel = new ArrayList<MarvelCharacter>();
	private File mStateFile;
	private String mGuid;	
	private Gson gson = new Gson();
	
	private boolean mActive;
	private int mTotal = Integer.MAX_VALUE;
	private View activityView;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.fadein,R.anim.fadeout);
		setContentView(R.layout.activity_view_character);
		
		
		Bundle extras = getIntent().getExtras();
		getActionBar().hide();
		
		Gson gson = new Gson();
		String json = extras.getString("character");
		mCurrent = gson.fromJson(json, MarvelCharacter.class);
		if ( mCurrent != null )
		{
			mModel.add(mCurrent);
			getActionBar().setTitle(mCurrent.getAlias());
		}
		
		json = extras.getString("comic");
		if ( json != null )
		{
			mComic = gson.fromJson(json, Comic.class);
		}
		
		if ( LAST_MODEL != null )
		{
			mModel = LAST_MODEL;
			LAST_MODEL = null;
		}
		
		mPosition = extras.getInt("position");
		mPageNumber = extras.getInt("pagenumber");
		mPageSize = extras.getInt("pagesize");
		mGuid = extras.getString("guid");
		
		File dir = new File(getFilesDir(),"state");
		dir.mkdirs();
		mStateFile = new File(dir,mGuid+".vpjson");
		
        if ( savedInstanceState != null )
        {
        	mPageNumber = savedInstanceState.getInt("pagenumber",0);
        	
        	try
        	{
            	if ( mStateFile.exists() )
            	{
            		Type collectionType = new TypeToken<ArrayList<MarvelCharacter>>(){}.getType();

            		mModel = Tools.fromFile(mStateFile, collectionType);
            	}
        	}
        	catch (Exception e)
        	{
        		Tools.showErrorMessage(this, e.getMessage());
        	}
        }
        
        mViewPager = (AstrientViewPager)findViewById(R.id.viewpager);
        mViewPager.setScrollChecker(this);

        mAdapter = new PreviewFragmentAdapter();
        
        mViewPager.setAdapter(mAdapter);
        //mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mViewPager.setOnPageChangeListener(this);
        
		
		if ( mAdapter.getCount() > mPosition && mViewPager.getCurrentItem() == 0 )
		{
			mViewPager.setCurrentItem(mPosition);
		} 
		
        getActionBar().setBackgroundDrawable(null);
        getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public void failure(RetrofitError error)
	{
		endTask();
	}


	@Override
	public void success(MarvelResponse<MarvelCharacter> marvelResponse, Response response)
	{
		MarvelData<MarvelCharacter> data = marvelResponse.data;
		mTotal = data.total;
		
		for ( MarvelCharacter mc : data.results )
		{
            if ( mc.thumbnail != null )
            {
                if ( mc.thumbnail.path != null )
                {
                    if ( !mc.thumbnail.path.contains("http://i.annihil.us/u/prod/marvel/i/mg/b/40/image_not_available") )
                    {
                        mModel.add(mc);
                    }
                }
            }
		}
		mAdapter.notifyDataSetChanged();
		
		endTask();
	}	
	
	
	@Override
	public void onBackPressed()
	{	
		DeleteFile deleteFile = new DeleteFile(mStateFile);
		Tools.executor.execute(deleteFile);
		
		super.onBackPressed();
	}
	
	
    @Override
	protected void onPause()
	{
		super.onPause();
		
        try
        {
        	//Socialize.onPause(this);
        	Tools.toFile(mStateFile,mModel);
        }
        catch (Exception e)
        {
        	Log.e("userStream","save",e);
        }
	}

	@Override
    protected void onSaveInstanceState(Bundle bundle) 
    {
        super.onSaveInstanceState(bundle);
        
        try
        {
        	bundle.putInt("pagenumber", mPageNumber);
        }
        catch (Exception e)
        {
        	Log.e("userStream","save",e);
        }
    }    
    
    
	@Override
	public void onPageScrollStateChanged(int arg0)
	{	
	}

	@Override
	public void onPageScrolled(int arg0, float arg1,int arg2)
	{	
	}

	@Override
	public void onPageSelected(int page)
	{
		int totalCount = mModel.size();
		if ( page > (mPageSize>>1) && (page >= (totalCount - (mPageSize>>1))) )
		{			
			if ( !mActive && totalCount < mTotal )
			{
				startTask(++mPageNumber);
			}
		}
		
		mCurrent = mModel.get(page);
        getActionBar().setTitle(mCurrent.getAlias());
	}
	
	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.viewcharacter, menu);
        return true;
    }    
    
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {

       switch (item.getItemId()) 
       {
          case R.id.menu_item_share:
        	 if ( mCurrent != null )
        	 {
                 Intent intent = new Intent(Intent.ACTION_SEND);
                 intent.setType("text/plain");
                 intent.putExtra(Intent.EXTRA_TEXT, mCurrent.getLink());
                 intent.putExtra(android.content.Intent.EXTRA_SUBJECT, mCurrent.name);
                 startActivity(Intent.createChooser(intent, getString(R.string.share)));
        	 }
             return true;
          case android.R.id.home:
        	 if ( mCurrent != null )
        	 {
                 Tools.toggleBookmark(this, mHandler, mCurrent);
        	 }
             return true;              
          default:
             return super.onOptionsItemSelected(item);
       }
    } 	
	
	private void startTask(int pageNumber)
	{
		if ( mComic == null )
		{
			REST.getMarvel(this).characters(pageNumber*mPageSize, mPageSize, "name", this);
		}
		else
		{
			REST.getMarvel(this).comicCharacters(mComic.id,pageNumber*mPageSize, mPageSize, "name", this);
		}
		
		mActive = true;
		Tools.setBackgroundActivity(this, activityView, true);
	}
	
	
	private void endTask()
	{
		mActive = false;
		Tools.setBackgroundActivity(this, activityView, false);
	}	
	


	@Override
	public boolean shouldScroll(MotionEvent event)
	{
		try
		{
			View view = mViewPager.getFocusedChild();
			
			if ( view != null )
			{
				TouchImageView preview = (TouchImageView)view.findViewById(R.id.preview);
				return !(preview != null && preview.canMove(event));
			}
		}
		catch (Exception e)
		{
			Log.e("viewblog","roll",e);
		}
		
		return true;
	}
	
	
	public void openMarvel(View view)
	{
		String link = "http://www.marvel.com";
		if ( mCurrent != null )
		{
			link = mCurrent.getLink();
		}
		
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		startActivity(intent);
	}
	
	class PreviewFragmentAdapter extends FragmentStatePagerAdapter 
	{
		private Bundle args;
		
	    public PreviewFragmentAdapter() 
	    {
	        super(getSupportFragmentManager());
	    }

	    @Override
	    public int getCount() 
	    {
	        return mModel.size();
	    }

	    @Override
	    public Fragment getItem(int position) 
	    {
	    	PreviewFragment fragment = null;

	    	try
	    	{
	        	MarvelCharacter item = mModel.get(position);
	       		fragment = new PreviewFragment();
	       		
	            Bundle args = new Bundle();
	            args.putString("character", gson.toJson(item));
	            args.putInt("position", position);
	            if (this.args !=null)
	            {
	            	args.putBundle("activityargs",this.args);
	            }	            
	            fragment.setArguments(args);
	    	}
	    	catch (Exception e)
	    	{
	    		Log.e("fragadapt","getfrag",e);
	    	}

	        return fragment;
	    }
	    
		@Override
		public Parcelable saveState()
		{
			return null;
		}
	}
	
	
	
	class ZoomOutPageTransformer implements ViewPager.PageTransformer
	{
		private static final float MIN_SCALE = 0.99f;
		private static final float MIN_ALPHA = 0.95f;

		public void transformPage(View view, float position)
		{
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1)
			{ // [-Infinity,-1)
				// This page is way off-screen to the left.
				view.setAlpha(0);

			} else if (position <= 1)
			{ // [-1,1]
				// Modify the default slide transition to shrink the page as
				// well
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0)
				{
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else
				{
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				// Scale the page down (between MIN_SCALE and 1)
				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				// Fade the page relative to its size.
				view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
						/ (1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else
			{ // (1,+Infinity]
				// This page is way off-screen to the right.
				view.setAlpha(0);
			}
		}
	}
	
	class DepthPageTransformer implements ViewPager.PageTransformer 
	{
		
	    private static final float MIN_SCALE = 0.75f;

	    public void transformPage(View view, float position) 
	    {
	        int pageWidth = view.getWidth();

	        if (position < -1) 
	        { // [-Infinity,-1)
	            // This page is way off-screen to the left.
	            view.setAlpha(0);
	        } 
	        else if (position <= 0) 
	        { // [-1,0]
	            // Use the default slide transition when moving to the left page
	            view.setAlpha(1);
	            view.setTranslationX(0);
	            view.setScaleX(1);
	            view.setScaleY(1);

	        }
	        else if (position <= 1)
	        { // (0,1]
	            // Fade the page out.
	            view.setAlpha(1 - position);

	            // Counteract the default slide transition
	            view.setTranslationX(pageWidth * -position);

	            // Scale the page down (between MIN_SCALE and 1)
	            float scaleFactor = MIN_SCALE
	                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
	            view.setScaleX(scaleFactor);
	            view.setScaleY(scaleFactor);

	        } 
	        else 
	        { // (1,+Infinity]
	            // This page is way off-screen to the right.
	            view.setAlpha(0);
	        }
	    }
	}	
}
