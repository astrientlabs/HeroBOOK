package com.brilliancemobility.heroes.net;

public class YouTubeVideoItem extends YouTubeData
{
	public YouTubeVideoSnippet snippet;
	
	public String toString()
	{
		if ( id != null && snippet != null )
		{
			return id.videoId + "/" + snippet.title;
		}
		else
		{
			return super.toString();
		}
	}
}
