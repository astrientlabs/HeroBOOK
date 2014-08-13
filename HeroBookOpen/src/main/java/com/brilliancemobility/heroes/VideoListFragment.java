package com.brilliancemobility.heroes;

import java.util.ArrayList;
import java.util.List;

import org.lucasr.twowayview.TwoWayView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.brilliancemobility.heroes.net.REST;
import com.brilliancemobility.heroes.net.VolleySingleton;
import com.brilliancemobility.heroes.net.YouTubeResponse;
import com.brilliancemobility.heroes.net.YouTubeVideoItem;
import com.brilliancemobility.heroes.util.AnalyticsUtils;

public class VideoListFragment extends Fragment implements OnItemClickListener, Callback<YouTubeResponse<YouTubeVideoItem>>
{
	private static final int ANIMATION_DURATION_MILLIS = 200;

	private TwoWayView mListView;
	private View mContentView;
	private PageAdapter mAdapter;
	private View videoBox;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mAdapter = new PageAdapter(getActivity(), new ArrayList<YouTubeVideoItem>());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mContentView = inflater.inflate(R.layout.fragment_view_videolist, null);

		mListView = (TwoWayView) mContentView.findViewById(R.id.list);
		mListView.setItemMargin(1);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);

		REST.getYouTube(getActivity()).videos(getString(R.string.playlistid), "snippet,id", 20, "date", this);

		return mContentView;
	}

	@Override
	public void failure(RetrofitError error)
	{
		Log.e("failure", error.toString());
	}

	@Override
	public void success(YouTubeResponse<YouTubeVideoItem> yResponse, Response response)
	{
		YouTubeVideoItem[] data = yResponse.items;
		if (data != null)
		{
			for (YouTubeVideoItem mc : data)
			{
				mAdapter.add(mc);

			}

			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		videoBox = getActivity().findViewById(R.id.video_box);
	}

	
	
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		YouTubeVideoItem item = mAdapter.getItem(position);
		String videoId = item.id.videoId;

		VideoFragment videoFragment = (VideoFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.video_fragment_container);
		
		
		if (  videoId.equals(videoFragment.getVideoId()) && videoBox.getVisibility() == View.VISIBLE )
		{
			videoFragment.stopPlaying();
			videoBox.setVisibility(View.GONE);
		}
		else
		{
			AnalyticsUtils.trackVideoViews(getActivity(), item.toString(), 1);
			
			videoFragment.setVideoId(videoId);
			if (videoBox.getVisibility() != View.VISIBLE)
			{
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
				{
					videoBox.setTranslationY(videoBox.getHeight());
				}
				videoBox.setVisibility(View.VISIBLE);
			}

			if (videoBox.getTranslationY() > 0)
			{
				videoBox.animate().translationY(0).setDuration(ANIMATION_DURATION_MILLIS);
			}
		}

	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		mAdapter.releaseLoaders();
	}


	private static final class PageAdapter extends ArrayAdapter<YouTubeVideoItem>
	{
		private final LayoutInflater inflater;


		public PageAdapter(Context context, List<YouTubeVideoItem> entries)
		{
			super(context, R.layout.video_list_item2);

			inflater = LayoutInflater.from(context);
		}

		public void releaseLoaders()
		{
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			YouTubeVideoItem entry = getItem(position);

			// There are three cases here
			if (view == null)
			{
				view = inflater.inflate(R.layout.video_list_item2, parent, false);
			} 

			
			((NetworkImageView) view.findViewById(R.id.thumbnail)).setImageUrl(entry.snippet.thumbnails.high.url,VolleySingleton.getInstance(getContext()).getImageLoader());;
			
			((TextView)view.findViewById(R.id.title)).setText(entry.snippet.title);

			return view;
		}

	}
}