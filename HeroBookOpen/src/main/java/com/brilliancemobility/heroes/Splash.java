package com.brilliancemobility.heroes;

import java.io.File;

import org.joda.time.chrono.ISOChronology;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.brilliancemobility.heroes.db.DatabaseTable;
import com.brilliancemobility.heroes.net.REST;

public class Splash extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_splash);
        new StartupTask().executeOnExecutor(Tools.executor);
	}
		
	class StartupTask extends AsyncTask<Void, Void, Exception>
	{
		public StartupTask()
		{
			super();
		}

		protected Exception doInBackground(Void... args)
		{
			long startTime = System.currentTimeMillis();
			long minimumSplashTime = 2*1000;

			try
	        {
				try
				{
					long httpCacheSize = 5 * 1024 * 1024; // 10 MiB
					File httpCacheDir = new File(getCacheDir(), "http");
					Class.forName("android.net.http.HttpResponseCache")
							.getMethod("install", File.class, long.class)
							.invoke(null, httpCacheDir, httpCacheSize);
				} 
				catch (Throwable httpResponseCacheNotAvailable)
				{
					Log.d("Startup", "HTTP response cache is unavailable.");
				}

	        	
	    		try
	    		{
	    			ISOChronology.getInstance();
	    			DatabaseTable.getInstance(Splash.this.getApplicationContext()).init();
	    			
	    			REST.getMarvel(Splash.this);
	    			REST.getYouTube(Splash.this);
	    		}
	      		catch (Throwable e)
	    		{
	    			Log.e("startup", "isochron", e);
	    		}
	       	
	            try
	            {
	                if ( (System.currentTimeMillis() - startTime) < minimumSplashTime  )
	                {
	                	Thread.sleep(minimumSplashTime - (System.currentTimeMillis() - startTime));
	                }
	            }
	            catch ( InterruptedException ie )
	            {
	            	
	            }
	        }
	        catch (Exception e)
	        {
	            return e;
	        }
	        
	        return null;
		}
		
		
		protected void onPostExecute(Exception result)
		{
			if ( result == null )
			{
				Intent intent = new Intent(Splash.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
			else
			{
				Log.e(getClass().getName(),"Init",result);
				String message = getString(R.string.error_startup);

				AlertDialog.Builder adb = new AlertDialog.Builder(Splash.this);
				adb.setTitle(getString(R.string.error));
				adb.setMessage(message);
				adb.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						finish();	
					}			
				}
				);
				adb.show();
			}
		}
	}	
}
