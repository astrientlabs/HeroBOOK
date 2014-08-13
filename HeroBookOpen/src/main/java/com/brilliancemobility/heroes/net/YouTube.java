package com.brilliancemobility.heroes.net;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface YouTube
{
	  @GET("/search")
	  YouTubeResponse<YouTubeVideoItem> videos(@Query("channelId") String channelId, @Query("part") String part, @Query("maxResults") int limit, @Query("order") String orderBy);

	  
	  @GET("/playlists")
	 void playlist(@Query("id") String playlistId, @Query("part") String part, @Query("maxResults") int limit, @Query("order") String orderBy, Callback<YouTubeResponse<YouTubeVideoItem>> callback);	  
	  
	  
	  
	  @GET("/search")
	 void videos(@Query("channelId") String channelId, @Query("part") String part, @Query("maxResults") int limit, @Query("order") String orderBy, Callback<YouTubeResponse<YouTubeVideoItem>> callback);	  
}