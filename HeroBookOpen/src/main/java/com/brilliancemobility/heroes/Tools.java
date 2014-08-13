package com.brilliancemobility.heroes;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.brilliancemobility.heroes.db.BookmarkRecord;
import com.brilliancemobility.heroes.db.HistoryRecord;
import com.brilliancemobility.heroes.db.SimpleDAO;
import com.brilliancemobility.heroes.net.Comic;
import com.brilliancemobility.heroes.net.MarvelCharacter;
import com.brilliancemobility.heroes.net.MarvelResponse;
import com.brilliancemobility.heroes.net.REST;
import com.google.gson.Gson;

public class Tools
{
	public static final ThreadPoolExecutor executor =  (ThreadPoolExecutor)Executors.newFixedThreadPool(4);
    
	private static final Random random = new Random();
	
	
	
	public static Bitmap createBlurredImage(Context context, Bitmap bitmap, int radius)
	{
		if ( radius == 0 )
		{
			return bitmap;
		}
		else
		{
		    Bitmap blurredBitmap = Bitmap.createBitmap(bitmap);
		    RenderScript rs = RenderScript.create(context);

		    // Allocate memory for Renderscript to work with
		    Allocation input = Allocation.createFromBitmap (rs, bitmap, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SCRIPT);
		    Allocation output = Allocation.createTyped(rs, input.getType());

		    // Load up an instance of the specific script that we want to use.
		    ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4 (rs));
		    script.setInput(input);

		    // Set the blur radius
		    script.setRadius(radius);

		    // Start the ScriptIntrinisicBlur
		    script.forEach(output);

		    // Copy the output to the blurred bitmap
		    output.copyTo(blurredBitmap);

		    return blurredBitmap;	
		}
	}	
	
	public static MarvelCharacter getRandomCharacter(Context context)
	{
		MarvelResponse<MarvelCharacter> response = REST.getMarvel(context).characters(0, 1, "name");
		int total = response.data.total;
		
		return getRandomCharacter(context, total);
	}	
	
	
	public static MarvelCharacter getRandomCharacter(Context context, int total)
	{
		
		int who = random.nextInt(total);
		
		MarvelResponse<MarvelCharacter> response = REST.getMarvel(context).characters(who, 1, "name");
		
		return response.data.results[0];
	}
	
	
	public static ProgressDialog showProgressDialog(Context context, String title, String message)
	{
		ProgressDialog dialog = ProgressDialog.show(context, title, message, true);		
		return dialog;
	}
	
	public static ProgressDialog showProgressDialog(Context context, int title, int message)
	{
		ProgressDialog dialog = ProgressDialog.show(context, context.getString(title), context.getString(message), true);
		return dialog;
	}	
	
	public static void toFile(File file, Object object) throws IOException
	{
		file.getParentFile().mkdirs();
		Writer writer = new FileWriter(file);
		Gson gson = new Gson();
        gson.toJson(object, writer);
        writer.close();
	}
	
	public static <T>T fromFile(File file,  Class<T> valueType) throws IOException
	{
		if ( file.exists() )
		{
			Reader reader = new FileReader(file);
			Gson gson = new Gson();
	        T ret = gson.fromJson(reader,valueType);
	        reader.close();
	        
	        return ret;
		}

        
        return null;
	}
	
	
	public static <T>T fromFile(File file,  Type collectionType) throws IOException
	{
		if ( file.exists() )
		{
			Reader reader = new FileReader(file);
			Gson gson = new Gson();
	        T ret = gson.fromJson(reader,collectionType);
	        reader.close();
	        
	        return ret;
		}

        
        return null;
	}	
	
	
	public static void setBackgroundActivity(Activity context, View activityView, boolean active)
	{
		try
		{
			if ( activityView == null )
			{
				ViewGroup g = (ViewGroup)context.findViewById(android.R.id.content);
				activityView = g.findViewById(R.id.activityview);
				
				if ( activityView == null )
				{
					
					WindowManager.LayoutParams params = new WindowManager.LayoutParams(
			                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
			                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
			                PixelFormat.TRANSLUCENT);
			        params.gravity = Gravity.RIGHT | Gravity.TOP;
			        params.width = LayoutParams.MATCH_PARENT;
			        params.height = LayoutParams.WRAP_CONTENT;

					activityView = LayoutInflater.from(context).inflate(R.layout.overlay_progress,null);
					
					g.addView(activityView,params);
				}

			}
			
			
			if ( active )
			{
				activityView.setVisibility(View.VISIBLE);
			}
			else
			{
				activityView.setVisibility(View.GONE);
			}	
		}
		catch (Throwable t)
		{
			
		}
	}
	
	public static void showErrorMessage(Context context, int rsid, Object... args)
	{
		showErrorMessage(context,context.getString(rsid,args));
	}
	
	public static void showErrorMessage(Context context, String message)
	{
		try
		{
			LayoutInflater layoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = layoutInflator.inflate(R.layout.toast_error, (ViewGroup) ((Activity)context).findViewById(R.id.toast_layout_root));

			
			TextView text = (TextView) layout.findViewById(R.id.text);
			text.setText(message);

			Toast toast = new Toast(context);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setView(layout);
			toast.show();
		}
		catch (Exception e)
		{
			Log.e("ba","sm",e);
		}
	}
	
	public static void showCharacter(Activity activity, Bitmap bitmap, ArrayAdapter<MarvelCharacter> adapter, Comic comic,  MarvelCharacter item, int position, int pageNumber, int pageSize, Bundle args)
	{
		try
		{
			Gson gson = new Gson();
			String json = gson.toJson(item);
			Tools.track(activity, item);
		
			Intent intent = new Intent(activity, ViewCharacter.class);
			intent.putExtra("character", json);
			
			if ( comic != null )
			{
				intent.putExtra("comic", gson.toJson(comic));
			}
			
        	ArrayList<MarvelCharacter> items = new ArrayList<MarvelCharacter>();
        	
        	if ( adapter == null )
        	{
        		items.add(item);
        	}
        	else
        	{
            	for ( int i = 0, stop = adapter.getCount(); i < stop; i++ )
            	{
            		item = adapter.getItem(i);
            		items.add(item);	
            	}
        	}


        	String guid = Integer.toHexString(intent.hashCode());
        	ViewCharacter.LAST_MODEL = items;
        	ViewCharacter.LAST_BITMAP = bitmap;
        	
        	intent.putExtra("guid", guid);
        	intent.putExtra("position", position);
        	intent.putExtra("pagenumber", pageNumber);
        	intent.putExtra("pagesize", pageSize);
        	intent.putExtra("args", args);
        	
        	activity.startActivity(intent);
        	
        	
        }
        catch (Exception e)
        {
            Log.e(activity.getClass().getName(),e.getMessage(),e);
        }
	}
	
	
	
	public static void showComic(Activity activity, ArrayAdapter<Comic> adapter, MarvelCharacter character, Comic item, int position, int pageNumber, int pageSize, Bundle args)
	{
		try
		{
			Gson gson = new Gson();
			String json = gson.toJson(item);
		
			Intent intent = new Intent(activity, ViewComic.class);
			intent.putExtra("comic", json);
			
			if ( character != null )
			{
				intent.putExtra("character", gson.toJson(character));
			}
			
        	ArrayList<Comic> items = new ArrayList<Comic>();
        	
        	if ( adapter == null )
        	{
        		items.add(item);
        	}
        	else
        	{
            	for ( int i = 0, stop = adapter.getCount(); i < stop; i++ )
            	{
            		item = adapter.getItem(i);
            		items.add(item);	
            	}
        	}

        	String guid = Integer.toHexString(intent.hashCode());
        	ViewComic.LAST_MODEL = items;
        	
        	intent.putExtra("guid", guid);
        	intent.putExtra("position", position);
        	intent.putExtra("pagenumber", pageNumber);
        	intent.putExtra("pagesize", pageSize);
        	intent.putExtra("args", args);
        	
        	activity.startActivity(intent);
        }
        catch (Exception e)
        {
            Log.e(activity.getClass().getName(),e.getMessage(),e);
        }
	}	
	
	
	
	
	public static void toggleBookmark(final Context context, Handler handler, final MarvelCharacter mc)
	{

		Runnable r = new Runnable()
		{
			public void run()
			{
				try
				{
					
					
					SimpleDAO<BookmarkRecord> dao = BookmarkRecord.getDao(context);
					BookmarkRecord record = dao.get(BookmarkRecord.COL_NAME, "\"" + mc.name + "\"");
		    		Object[] args = new Object[] { mc.name };
		    		
		    		if ( record == null )
		    		{
						record = new BookmarkRecord();
						record.setMarvelCharacter(mc);
						record.setModified(System.currentTimeMillis());
						record.setCreated(System.currentTimeMillis());
						
						Uri uri = BookmarkRecord.CONTENT_URI;
				        context.getContentResolver().insert(uri, record.getContentValues(null));
				        
				        CharSequence text = context.getString(R.string.action_bookmark_added,args);
				        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
				        toast.show();
		    		}
		    		else
		    		{
		    	        Uri uri = Uri.parse(BookmarkRecord.CONTENT_URI + "/" + Long.toString(record.getLuid()));
		    	        context.getContentResolver().delete(uri, null, null);
		    	        
				        CharSequence text = context.getString(R.string.action_bookmark_deleted,args);
				        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
				        toast.show();
		    		}
				}
				catch (Exception e)
				{
					Log.e("b","u",e);
				}
			}
		};
		
		
		if ( handler == null )
		{
			r.run();
		}
		else
		{
			handler.post(r);
		}
	}
	
	
	public static void track(final Context context, final MarvelCharacter mc)
	{

		Runnable r = new Runnable()
		{
			public void run()
			{
				try
				{					
					SimpleDAO<HistoryRecord> dao = HistoryRecord.getDao(context);
					HistoryRecord record = dao.get(HistoryRecord.COL_NAME, "\"" + mc.name + "\"");
		    		
		    		if ( record == null )
		    		{
						record = new HistoryRecord();
						record.setMarvelCharacter(mc);
						record.setModified(System.currentTimeMillis());
						record.setCreated(System.currentTimeMillis());
						
						Uri uri = HistoryRecord.CONTENT_URI;
				        context.getContentResolver().insert(uri, record.getContentValues(null));	
		    		}
		    		else
		    		{
		    			record.setModified(System.currentTimeMillis());
		    			record.setMarvelCharacter(mc);
		    			
		    	        dao.save(record);		
		    		}
				}
				catch (Exception e)
				{
					Log.e("b","u",e);
				}
			}
		};
		
		
		Tools.executor.execute(r);
	}	
}
