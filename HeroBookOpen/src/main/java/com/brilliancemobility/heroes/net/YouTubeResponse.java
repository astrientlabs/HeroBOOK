package com.brilliancemobility.heroes.net;

public class YouTubeResponse<T extends YouTubeData>
{
	public String kind;
	public String nextPageToken;
	public String etag;
	public YouTubePageInfo pageInfo;
	
	public T[] items;
}
