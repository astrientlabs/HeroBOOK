package com.brilliancemobility.heroes.net;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import android.content.Context;
import android.util.Log;

import com.brilliancemobility.heroes.util.Strings;
import com.squareup.okhttp.OkHttpClient;

public class REST
{
	private static final String YOU_TUBE_PUBLIC_KEY = "";
	private static final String MARVEL_PUBLIC_KEY = "";
	private static final String MARVEL_PRIVATE_KEY = "";
	
	static final RequestInterceptor youTubeRequestInterceptor = new RequestInterceptor() 
	{
		  @Override
		  public void intercept(RequestFacade request) 
		  {
			  request.addQueryParam("key", YOU_TUBE_PUBLIC_KEY);
		  }
	};
	
	
	static final RequestInterceptor mRequestInterceptor = new RequestInterceptor() 
	{
		  @Override
		  public void intercept(RequestFacade request) 
		  {
			  try
			  {
				  long ts = System.currentTimeMillis();
				  
			        MessageDigest md = MessageDigest.getInstance("MD5");
			        md.update((String.valueOf(ts)+MARVEL_PRIVATE_KEY+MARVEL_PUBLIC_KEY).getBytes());
			        
			        String hash = new String(Strings.hexEncode(md.digest()));

				    
					request.addQueryParam("apikey", MARVEL_PUBLIC_KEY);
					request.addQueryParam("ts", String.valueOf(ts));
					request.addQueryParam("hash", hash);
			  }
			  catch ( Exception e )
			  {
				  Log.e("MD5", "", e);
			  }
		    
		  }
	};
	
	
	static YouTube youTube;
	static Marvel marvel;

	
	public static YouTube getYouTube(Context context)
	{
		if ( youTube == null )
		{
			youTube = get(context, "https://www.googleapis.com/youtube/v3",youTubeRequestInterceptor,YouTube.class);
		}
		
		return youTube;
	}
	
	
	public static Marvel getMarvel(Context context)
	{
		if ( marvel == null )
		{
			marvel = get(context, "http://gateway.marvel.com:80/v1/public",mRequestInterceptor,Marvel.class);
		}
		
		return marvel;
	}	

	
	public static final <T> T get(Context context, String url, RequestInterceptor requestInterceptor, Class<T> clazz)
	{
        OkHttpClient okHttpClient = new OkHttpClient();

		return new RestAdapter.Builder()
		.setEndpoint(url)
		.setRequestInterceptor(requestInterceptor)
		.setClient(new OkClient(okHttpClient))
		//.setLogLevel(RestAdapter.LogLevel.FULL)
		.build().create(clazz);
	}
}
