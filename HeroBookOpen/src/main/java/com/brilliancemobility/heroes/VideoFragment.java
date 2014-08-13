package com.brilliancemobility.heroes;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

public class VideoFragment extends YouTubePlayerSupportFragment implements OnInitializedListener
{
	private YouTubePlayer player;
	private String videoId;

	public static VideoFragment newInstance()
	{
		return new VideoFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		initialize(DeveloperKey.DEVELOPER_KEY, this);
	}
	
	
	
	

	@Override
	public void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public void onResume()
	{
		//initialize(DeveloperKey.DEVELOPER_KEY, this);
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle arg0)
	{
		// TODO Auto-generated method stub
		//super.onSaveInstanceState(arg0);
	}

	@Override
	public void onStart()
	{
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onStop()
	{
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroy()
	{
		if (player != null)
		{
			player.release();
		}
		super.onDestroy();
	}
	
	public void stopPlaying()
	{
		if (player != null)
		{
			player.pause();
		}
	}

	public String getVideoId()
	{
		return this.videoId;
	}
	
	public void setVideoId(String videoId)
	{
		if (videoId != null && !videoId.equals(this.videoId))
		{
			this.videoId = videoId;
			if (player != null)
			{
				player.loadVideo(videoId);
			}
		}
	}

	public void pause()
	{
		if (player != null)
		{
			player.pause();
		}
	}

	@Override
	public void onInitializationSuccess(Provider provider,	YouTubePlayer player, boolean restored)
	{
		if (!restored)
		{
			this.player = player;
			player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
			player.setOnFullscreenListener((MainActivity) getActivity());
			if (!restored && videoId != null)
			{
				player.loadVideo(videoId);
			}
		}

	}

	@Override
	public void onInitializationFailure(Provider provider, YouTubeInitializationResult result)
	{
		this.player = null;
	}

}
