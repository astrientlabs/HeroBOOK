package com.brilliancemobility.heroes;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.brilliancemobility.heroes.db.DatabaseTable;
import com.brilliancemobility.heroes.util.Strings;
import com.google.android.youtube.player.YouTubePlayer;


public class MainActivity extends FragmentActivity implements YouTubePlayer.OnFullscreenListener, OnClickListener, OnItemClickListener, OnEditorActionListener, TextWatcher
{
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	
	
	private AutoCompleteTextView searchField;
    private InputMethodManager in;	
    private SuggestionsAdapter mSuggestionsAdapter;
    private View mSearchButton;
    private HeroesFragment mHeroesFragment;
    private boolean mSearchClicked = false;

	private static final int LANDSCAPE_VIDEO_PADDING_DP = 5;
	  private VideoListFragment listFragment;
	  private VideoFragment videoFragment;

	  private View videoBox;

	 private boolean isFullscreen;	
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fadein,R.anim.fadeout);
		setContentView(R.layout.activity_main);
		
		mHeroesFragment = new HeroesFragment();

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(1);
		mViewPager.setOffscreenPageLimit(3);
		
               
        mSuggestionsAdapter = new SuggestionsAdapter();
        
        searchField = (AutoCompleteTextView) findViewById(R.id.searchTermField);
        searchField.addTextChangedListener(this);
        searchField.setOnEditorActionListener(this);
        searchField.setOnItemClickListener(this);
        searchField.setAdapter(mSuggestionsAdapter);
        
        in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        
		mSearchButton = this.findViewById(R.id.searchbutton);
		mSearchButton.setOnClickListener(this);
		
		this.getActionBar().setHomeButtonEnabled(true);

		
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
		lp.gravity = Gravity.END;
		getActionBar().setDisplayShowCustomEnabled(true);

		
		
	    listFragment = (VideoListFragment) getSupportFragmentManager().findFragmentById(R.id.list_fragment);
	    videoFragment = (VideoFragment) getSupportFragmentManager().findFragmentById(R.id.video_fragment_container);

	    videoBox = findViewById(R.id.video_box);

	    videoBox.setVisibility(View.INVISIBLE);

	    layout();
	}

	
	@Override
	protected void onResume()
	{
		if ( videoFragment != null )
		{
			if ( videoFragment.isDetached() )
			{
				videoFragment = VideoFragment.newInstance();
				
				
				FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.video_fragment_container, videoFragment);//.attach(videoFragment);//.remove(videoFragment);
				ft.commit();	
			}
		}
		
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		ft.detach(videoFragment);//.remove(videoFragment);
		ft.commit();
		
		videoBox.setVisibility(View.INVISIBLE);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if ( item.getItemId() == android.R.id.home )
		{
			mHeroesFragment.reset();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}



	public void onClick(View view)
	{
		if ( view.getId() == R.id.searchbutton )
		{
			search();
		}
	}
	
    @Override
	public void onItemClick(AdapterView<?> a, View view, int position, long id)
	{
		searchField.performCompletion();
		search();
	}


    public void search()
    {
    	mSearchClicked = true;
    	mViewPager.setCurrentItem(1,false);
        in.hideSoftInputFromWindow(searchField.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        searchField.dismissDropDown();
		String term = searchField.getText().toString();
		if ( Strings.isNull(term) )
		{
			mHeroesFragment.reset();
		}
		else
		{
			term = term.trim();
			mHeroesFragment.search(term);			
		}
    }
    
	@Override
	public void onBackPressed()
	{	
		if ( mViewPager.getCurrentItem() != 1 )
		{
			mViewPager.setCurrentItem(1);
		}
		else if ( mSearchClicked )
		{
			mHeroesFragment.reset();
			mSearchClicked = false;
		}
		else
		{
			super.onBackPressed();
		}

	}    


	@Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
    {
        if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) 
        {
        	search();
        }
        return false;
    } 
    
	public void afterTextChanged(Editable editable)
	{

	}    
	
	public void beforeTextChanged(CharSequence s, int start, int count,	int after)
	{
	}

	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
	}
	
	
	
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	overridePendingTransition(R.anim.fadeout, R.anim.fadein);
    }    
    
	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{

		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			Fragment fragment;
			if ( position == 0 )
			{
				fragment = new HistoryFragment();
			}
			else if ( position == 1 )
			{
				fragment = mHeroesFragment;
			}
			else
			{
				fragment = new BookmarksFragment();
			}

			return fragment;
		}

		@Override
		public int getCount()
		{
			return 3;
		}
	}
	
	public void openMarvel(View view)
	{
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.marvel.com"));
		startActivity(intent);
	}
	
	
	
	public class SuggestionsAdapter extends ArrayAdapter<String> implements Filterable 
	{
	    ArrayList<String> mData;
	    DatabaseTable mDb;
	    
	    public SuggestionsAdapter() 
	    {
	        super(MainActivity.this, R.layout.row_suggestion, R.id.textview);
	        mData = new ArrayList<String>();
	        mDb = DatabaseTable.getInstance(MainActivity.this);
	    }

	    @Override
	    public int getCount() 
	    {
	        return mData.size();
	    }

	    @Override
	    public String getItem(int index) 
	    {
	        return mData.get(index);
	    }

	    @Override
	    public Filter getFilter()
	    {
	        Filter myFilter = new Filter() 
	        {
	            @Override
	            protected FilterResults performFiltering(CharSequence constraint) 
	            {
	                FilterResults filterResults = new FilterResults();
	                
	                if ( constraint != null )
	                {
	                    try 
	                    {
	                        mData = mDb.getWordMatches(constraint.toString());
	                    }
	                    catch(Exception e)
	                    {
	                        Log.e("myException", e.getMessage());
	                    }

	                    filterResults.values = mData;
	                    filterResults.count = mData.size();
	                }
	                return filterResults;
	            }

	            @Override
	            protected void publishResults(CharSequence contraint, FilterResults results) 
	            {
	                if (results != null && results.count > 0)
	                {
	                	notifyDataSetChanged();
	                }
	                else
	                {
	                    notifyDataSetInvalidated();
	                }
	            }
	        };
	        return myFilter;
	    }
	}
	
	
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		layout();
	}

	@Override
	public void onFullscreen(boolean isFullscreen)
	{
		this.isFullscreen = isFullscreen;

		layout();
	}
	  

	private void layout()
	{
		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

		listFragment.getView().setVisibility(isFullscreen ? View.GONE : View.VISIBLE);

		if (isFullscreen)
		{
			videoBox.setTranslationY(0); // Reset any translation that was
											// applied in portrait.
			setLayoutSize(videoFragment.getView(), LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			setLayoutSizeAndGravity(false,videoBox, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		} 
		else if (isPortrait)
		{
			setLayoutSize(listFragment.getView(), LayoutParams.MATCH_PARENT, dpToPx(100));
			setLayoutSize(videoFragment.getView(), LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			setLayoutSizeAndGravity(true,videoBox, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM| Gravity.CENTER_HORIZONTAL);
		} 
		else
		{
			videoBox.setTranslationY(0); // Reset any translation that was
											// applied in portrait.
			int screenWidth = dpToPx(getResources().getConfiguration().screenWidthDp);
			setLayoutSize(listFragment.getView(), LayoutParams.MATCH_PARENT, dpToPx(100));
			int videoWidth = screenWidth - screenWidth / 4 - dpToPx(LANDSCAPE_VIDEO_PADDING_DP);
			setLayoutSize(videoFragment.getView(), videoWidth, LayoutParams.WRAP_CONTENT);
			setLayoutSizeAndGravity(true,videoBox, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		}
	} 


	private int dpToPx(int dp)
	{
		return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
	}

	private void setLayoutSize(View view, int width, int height)
	{
		android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
		params.width = width;
		params.height = height;
		view.setLayoutParams(params);
	}

	private void setLayoutSizeAndGravity(boolean margin, View view, int width, int height, int gravity)
	{
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
		params.width = width;
		params.height = height;
		params.gravity = gravity;
		
		if ( margin )
		{
			params.bottomMargin = dpToPx(127);
		}
		else
		{
			params.bottomMargin = 0;
		}
		
		view.setLayoutParams(params);
	}
}
