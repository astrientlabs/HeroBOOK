package com.brilliancemobility.heroes.net;

import java.io.File;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import android.os.Environment;

public interface Marvel
{
	public static enum ENTITY_TYPE  {
		comic, character
	};
	public static final File EXTERNAL_DIR = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"herobook");

	  @GET("/characters")
	  MarvelResponse<MarvelCharacter> characters(@Query("offset") int offset, @Query("limit") int limit, @Query("orderBy") String orderBy);
	  
	  @GET("/characters")
	  void characters(@Query("offset") int offset, @Query("limit") int limit, @Query("orderBy") String orderBy, Callback<MarvelResponse<MarvelCharacter>> cb);
	  
	  @GET("/characters")
	  void charactersByName(@Query("name") String name, @Query("offset") int offset, @Query("limit") int limit, @Query("orderBy") String orderBy, Callback<MarvelResponse<MarvelCharacter>> cb);
	  
	  @GET("/characters")
	  void characters(@Query("nameStartsWith") String name, @Query("offset") int offset, @Query("limit") int limit, @Query("orderBy") String orderBy, Callback<MarvelResponse<MarvelCharacter>> cb);
	    
	  @GET("/characters/{userid}/comics")
	  void comics(@Path("userid") int userId, @Query("offset") int offset, @Query("limit") int limit, @Query("orderBy") String orderBy, Callback<MarvelResponse<Comic>> cb);
	
	  @GET("/comics/{id}/characters")
	  void comicCharacters(@Path("id") int id, @Query("offset") int offset, @Query("limit") int limit, @Query("orderBy") String orderBy, Callback<MarvelResponse<MarvelCharacter>> cb);	  
}