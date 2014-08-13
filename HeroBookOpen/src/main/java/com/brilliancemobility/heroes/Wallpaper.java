package com.brilliancemobility.heroes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.brilliancemobility.heroes.net.MarvelCharacter;
import com.brilliancemobility.heroes.net.MarvelData;
import com.brilliancemobility.heroes.net.MarvelResponse;
import com.brilliancemobility.heroes.net.REST;
import com.brilliancemobility.heroes.net.Thumbnail;

public class Wallpaper extends WallpaperService
{
    public static final String SHARED_PREFS_NAME = "wallpaper_settings";
    private Handler handler = new Handler();
    
    private File wallpaperDir;
    private static boolean firstSpin = true;
    
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate()
    {
        super.onCreate();
        //android.os.Debug.waitForDebugger(); 
        
        wallpaperDir = getDir("wallpapers", Context.MODE_WORLD_READABLE);
        wallpaperDir.mkdirs();
    }

    @Override
    public void onDestroy() 
    {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() 
    {
        return new WallpaperEngine();
    }
    
    
    

    class WallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener
    {
    	class BitmapHolder 
    	{
    		Bitmap bitmap;
    		String url;
    		String name;
    	}
    	
        private long refreshRate = 86400000L;
        private int blurRadius = 0;
        private long downloadRate;
        private int drawCount = 0;
        
        private SharedPreferences mPrefs;
        private int fetchCount = 0;
        private int maxFiles = 25;
        private BitmapHolder currentImage;
        private Bitmap defaultBitmap;
        
        
        private int xPixelOffset;
        //private float xOffset;
		//private float xOffsetStep;

        private long lastSwap = 0;
        private long lastDownload = 0;
        private boolean running;
        private boolean swapping;
        private boolean direction = false;
        
        private int limit = 5;
        private int maxOffset = 1402/20;
        private Random random = new Random();
        
        private ThreadPoolExecutor executor = new ThreadPoolExecutor (1, 2, 60*5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(4));
        
        private GestureDetector gestureDetector = new GestureDetector (Wallpaper.this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                

                try
                {
        			DisplayMetrics displayMetrics = Wallpaper.this.getResources().getDisplayMetrics();
        			int width = displayMetrics.widthPixels;
        			int height = displayMetrics.heightPixels;
        			Point middle = new Point(width>>1,height>>1);
        			
                	int x = Math.round(event.getRawX());
                	int y = Math.round(event.getRawY());
                	
                	Point press = new Point(x,y);
                	
                	int sliceX = width/3;
                	int sliceY = 300;
                	Rect rect = new Rect( 1*sliceX, 0, 2*sliceX, sliceY);
                	
                	//Log.e("SDf", rect.contains(x, y) + " " + rect.width() + " " + rect.height() + " " + rect.left + " " + rect.top + " " + press.x + " " + press.y);
                	
                	if ( currentImage != null && rect.contains(x, y) )
                	{
                		if ( currentImage != null && currentImage.url != null )
                		{
            				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentImage.url));
            				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            				startActivity(intent);
            				
                			//executor.execute(mSave);             			
                			//Toast.makeText(Wallpaper.this, Wallpaper.this.getString(R.string.saved), Toast.LENGTH_SHORT).show();
                		}
                	}
                	else
                	{
                		if ( press.x > middle.x )
                    	{
                    		direction = false;
                    	}
                    	else
                    	{
                    		direction = true;
                    	}
                    	//Log.e("sdf","double tap");
                    	//swapImage();
                    	
                    	handler.post(mSwapImage);//executor.execute(mSwapImage);
                		if ( !running )
                		{
                			executor.execute(mFetchImages);             
                		}
                	}              	
                }
            	catch (Throwable e)
            	{
            		Log.e(getClass().getName(),"onDoubleTap",e);
            	}
                
                return true;                
            }
        });        
        
        
        private final Runnable mSwapImage = new Runnable() 
        {
            public void run() 
            {
                try
                {
                	swapImage();
                }
                catch (Throwable e)
                {
                	Log.e("Swap","mem",e);
                }
            }
        };
        
        private final Runnable mFetchImages = new Runnable() 
        {
            public void run() 
            {
                fetchImage();
            }
        };
        
        WallpaperEngine() 
        {
        	super();
        	
        	mPrefs = PreferenceManager.getDefaultSharedPreferences(Wallpaper.this);
        	
        	
            //mPrefs = Wallpaper.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
            
			try
			{
				defaultBitmap = BitmapFactory.decodeResource(Wallpaper.this.getResources(), R.drawable.splash);
			}
			catch (Throwable t)
			{
				Log.e("wallpaper","defbit",t);
			}
			
			currentImage = new BitmapHolder();
			currentImage.bitmap = defaultBitmap;

			refreshRate = Long.valueOf(mPrefs.getString("refreshrate", String.valueOf(refreshRate)));
			blurRadius = mPrefs.getInt("blur.radius", 0);
			
			
			
			setDownloadRate(refreshRate);

			//android.os.Debug.waitForDebugger();
        }
        
        
        @Override
        public void onTouchEvent(MotionEvent event) 
        {
            gestureDetector.onTouchEvent(event);
        }
        
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) 
        {   
            refreshRate = Long.valueOf(prefs.getString("refreshrate", String.valueOf(refreshRate)));
            blurRadius = prefs.getInt("blur.radius", 0);
            setDownloadRate(refreshRate);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
        {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
            
            executor.execute(mFetchImages);
            executor.execute(mSwapImage);
        }

        @Override
        public void onDestroy() 
        {
            super.onDestroy();
            executor.shutdownNow();
        }

        @Override
        public void onVisibilityChanged(boolean visible) 
        {
        	try
        	{
                if (visible) 
                {
                    if ( System.currentTimeMillis() > (refreshRate+lastSwap) )
                    {
                    	executor.execute(mSwapImage);
                    }
                    
                    if ( System.currentTimeMillis() > (downloadRate+lastDownload) )
                    {
                    	executor.execute(mFetchImages);
                    }
                }
                else 
                {

                }        		
        	}
        	catch (Throwable e)
        	{
        		Log.e(getClass().getName(),"visitbility change",e);
        	}
        }

        void drawFrame() 
        {
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try 
            {            	
            	int x = xPixelOffset + ((getDesiredMinimumWidth()-currentImage.bitmap.getWidth())>>1);
            	
            	int diff = Math.abs((getDesiredMinimumWidth()-currentImage.bitmap.getWidth())/2);
            	
            	//if ( diff > 0 ) diff = 0;
            	
            	float point = (float)currentImage.bitmap.getWidth() / (float)getDesiredMinimumWidth();
            	x = Math.round((xPixelOffset*(point-.05f)));// + ((getDesiredMinimumWidth()-currentImage.getWidth())>>1);
            	c = holder.lockCanvas();
            	

            	int maxRight = -1 * (currentImage.bitmap.getWidth()/2) + diff;
            	
            	
    			DisplayMetrics displayMetrics = Wallpaper.this.getResources().getDisplayMetrics();
    			int width = displayMetrics.widthPixels;
    			//int height = displayMetrics.heightPixels;

            	if ( x > 0 )
            	{
            		x = 0;
            	}
            	else if (  x < maxRight )
            	{
            		x = maxRight;
            	}
            	
            	if ( currentImage.bitmap.getWidth() + x - width < 0  )
            	{
            		x = width - currentImage.bitmap.getWidth();
            	}
            	
            	if ( c != null )
            	{            	
            		c.drawColor(0xFFFF001e);
                	if ( currentImage == null || currentImage.bitmap == defaultBitmap )
                	{   
                		c.drawBitmap(defaultBitmap, (c.getWidth()>>1)-(defaultBitmap.getWidth()>>1), (c.getHeight()>>1)-(defaultBitmap.getHeight()>>1), null);
                	}
                	else
                	{
                		c.drawBitmap(currentImage.bitmap, x, 0, null);             		
                	}
                	

                	
                	
            		Paint textPaint = new Paint();
            	    textPaint.setTextSize(17);
            	    textPaint.setColor(0xCCFFFFFF);
            	    textPaint.setTextAlign(Align.LEFT);
            	    int xPos = (c.getWidth());// / 2);
            	    int yPos = c.getHeight() - ( 80 ) ; 


            	    String text = "Data provided by Marvel. ©2014 Marvel";

            	    Rect bounds = new Rect();
            	    textPaint.getTextBounds(text, 0, text.length(), bounds);
                	//bounds = new Rect(0, c.getHeight() - ( 100 ), c.getWidth(), c.getHeight());
            	    
                	bounds = new Rect(c.getWidth() - bounds.height(), c.getHeight() - bounds.width() - 100, c.getWidth(), c.getHeight());
                	Paint paint = new Paint();
                	paint.setColor(0xFF000000);
                	c.drawRect(bounds, paint); 
            	    
                	
            	    c.save();
            	    c.rotate(-90, xPos, yPos);
            	    
            	    
                	
            	    
            	    c.drawText(text, xPos, yPos, textPaint);
            	    c.restore();
            	    
            	    if ( currentImage.name != null )
            	    {
            	    	textPaint.setTextSize(36);
            	    	textPaint.setFakeBoldText(true);
            	    	textPaint.setColor(0x88FFFFFF);
            	    	textPaint.setTextAlign(Align.CENTER);
            	    	xPos = (c.getWidth() / 2);
            	    	yPos = c.getHeight() - ( 100 ) ; 
            	    	c.drawText(currentImage.name, xPos, yPos, textPaint);
            	    }
            	}
            } 
        	catch ( Throwable e )
        	{
        		Log.e(getClass().getName(),e.getMessage(),e);
        	}
            finally 
            {
                if (c != null) holder.unlockCanvasAndPost(c);
            }
        }
        
        
        void swapImage() 
        {
        	if ( !swapping )
        	{
        		swapping = true;
                try 
                {
                	//mHandler.removeCallbacks(mSwapImage);
                	
                	File[] files = wallpaperDir.listFiles();
                	if ( files.length == 0 )
                	{
                		
                	}
                	else
                	{
                		if ( direction )
                		{
                			drawCount++;
                		}
                		else
                		{
                			drawCount--;
                			
                			if ( drawCount < 0 )
                			{
                				drawCount = files.length - 1;
                			}
                		}
                		
                    	File file = files[drawCount%files.length];
                    	Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    	if ( bitmap == null )
                    	{
                    		
                    	}
                    	else
                    	{
                    		
                    		currentImage.bitmap = Tools.createBlurredImage(Wallpaper.this, bitmap, blurRadius);// bitmap;
                    		file = new File(file.getPath() + ".json");
                    		if ( file.exists() )
                    		{
                    			MarvelCharacter mc = Tools.fromFile(file, MarvelCharacter.class);
                    			
                                //ExifInterface exif = new ExifInterface(file.getPath());
                                currentImage.url = mc.getLink();//exif.getAttribute("ASTRIENT_URL");
                                currentImage.name = mc.getAlias(); //exif.getAttribute("ASTRIENT_NAME");
                                
                        		/*
                        		
                        		if ( currentImage.url == null )
                        		{
                                    currentImage.url = exif.getAttribute(ExifInterface.TAG_MODEL);
                                    currentImage.name = exif.getAttribute(ExifInterface.TAG_MAKE);
                        		}*/
                    		}

                    		
                    		drawFrame();
                    	}                       
                	}
                } 
            	catch ( Throwable e )
            	{
            		Log.e(getClass().getName(),e.getMessage(),e);
            	}
            	finally
            	{
            		lastSwap = System.currentTimeMillis();
            		swapping = false;
            	}
        	}
        }
        
        void fetchImage()
        {
        	try
        	{
        		if ( !running )
        		{
        			running = true;

        			int offset = random.nextInt(maxOffset);
            		MarvelResponse<MarvelCharacter> response = REST.getMarvel(Wallpaper.this).characters(offset, limit, "name");
            		MarvelData<MarvelCharacter> data = response.data;
            		
            		if (  data != null && data.results != null )
            		{
            			ArrayList<MarvelCharacter> al = new ArrayList<MarvelCharacter>();
            			
                		for ( MarvelCharacter mc : data.results )
                		{
                			if ( !(mc.thumbnail.path.contains("http://i.annihil.us/u/prod/marvel/i/mg/b/40/image_not_available")) ) 
                			{
                				al.add(mc);
                			}
                		}
                		
                		if ( !al.isEmpty() )
                		{
                			Collections.shuffle(al);
                			if ( firstSpin )
                			{
                				for ( MarvelCharacter mc : al )
                    			{
                    				download(mc);
                    			}
                				
                				//firstSpin = false;
                			}
                			else
                			{
                				download(al.get(0));
                			}                			
                		}
            		}                  
        		}
        	}
        	catch ( Throwable e )
        	{
        		Log.e(getClass().getName(),e.getMessage(),e);
        	}
        	finally
        	{            		
        		running = false;
        	}
        }
        
        
    	private void download(File file, String url) throws ClientProtocolException, IOException
    	{
    		FileOutputStream fos = new FileOutputStream(file);
    		InputStream is = null;
    		try
    		{
    			URL u = new URL(url);
                is = u.openStream();
                BufferedOutputStream bos = new BufferedOutputStream(fos); 
                byte[] buffer = new byte[4*1024]; int read = 0;
    			
    			while ( (read = is.read(buffer)) != -1) 
    			{
    				bos.write(buffer, 0, read);
    				
    			}
    			bos.flush();
    			bos.close();
    		} 
    		finally
    		{
    			if ( is != null ) is.close();
    			if (fos != null) fos.close();
    		}
    	}	
        
        private void download(MarvelCharacter mc) throws IllegalStateException, IOException
        {
			try
			{	            
				Thumbnail photo = mc.thumbnail;
	            File outFile = new File(wallpaperDir,((fetchCount++)%maxFiles) + ".jpg");
	            
	            download(outFile, photo.getPicUrl());
	            
                final int width =  getDesiredMinimumWidth();
                final int height = getDesiredMinimumHeight();
                final int len = Math.max(width,height);
                
                
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(outFile.getAbsolutePath(),options);
				int imageHeight = options.outHeight;
				int imageWidth = options.outWidth;

				
            	boolean upscaling = mPrefs.getBoolean("upscaling", true);
            	
                double cw = len / (1.0 * imageWidth);
                double ch = len / (1.0 * imageHeight);
                double scale = Math.min(cw, ch);

                if (scale > 1.0)
                    scale = 1.0;

                
                if (upscaling && imageHeight < getDesiredMinimumHeight())
                {
                	scale = (float)getDesiredMinimumHeight()/(float)imageHeight;
                }
                
                int newHeight = (int) Math.round(scale * imageHeight);
                int newWidth = (int) Math.round(scale * imageWidth);
                
                Bitmap scaled;
                
            	Bitmap bitmap = BitmapFactory.decodeFile(outFile.getAbsolutePath());
            	scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
            	bitmap.recycle();

                            	
            	if ( scaled.getHeight() < height || scaled.getWidth() < width )
            	{
            		Bitmap matte = Bitmap.createBitmap(scaled.getWidth(), height, Bitmap.Config.RGB_565);
            		Canvas canvas = new Canvas(matte);
            		canvas.drawColor(0);
            		
            		int y = (height>>1) - (scaled.getHeight()>>1);
            		canvas.drawBitmap(scaled, 0, y, null);
            		
            		bitmap = scaled;
            		bitmap.recycle();
            		scaled = matte;
            	}
            	
                FileOutputStream fos = null;
                try
                {
                    fos = new FileOutputStream(outFile);
                    scaled.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                }
    	        finally
    	        {
    	    		if ( fos != null )
    	    		{
    	    			fos.close();
    	    		}
    	        }
                
                outFile = new File(outFile.getPath() +".json");
                Tools.toFile(outFile, mc);
                /*
                ExifInterface exif = new ExifInterface(outFile.getPath());
                exif.setAttribute("ASTRIENT_URL", mc.getLink());
                exif.setAttribute("ASTRIENT_NAME", mc.getAlias());
                
                exif.setAttribute(ExifInterface.TAG_MODEL, mc.getLink());
                exif.setAttribute(ExifInterface.TAG_MAKE, mc.getAlias());                
                exif.saveAttributes();*/
                
                lastDownload = System.currentTimeMillis();
			}
	        catch (Throwable e)
	        {
	            Log.e(getClass().getName(),"run",e);
	        }
        	
        }
 
        
        private void setDownloadRate(long refreshRate)
        {
    		long postDelay = Math.min(refreshRate*2, refreshRate+(1000*60*15));
    		try
    		{
    			if ( wallpaperDir.list().length < maxFiles )
    			{
    				postDelay = 15 * 1000;
    			}
    		}
    		catch (Throwable t)
    		{
    			
    		}
    		
    		downloadRate = postDelay;
        }

        
        
		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset)
		{
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			this.xPixelOffset = xPixelOffset;
			drawFrame();
		}
    }
}