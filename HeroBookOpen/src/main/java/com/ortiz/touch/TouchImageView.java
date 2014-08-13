/*
 * TouchImageView.java
 * By: Michael Ortiz
 * Updated By: Patrick Lackemacher
 * Updated By: Babay88
 * Updated By: @ipsilondev
 * Updated By: hank-cp
 * Updated By: singpolyma
 * -------------------
 * Extends Android ImageView to include pinch zooming, panning, fling and double tap zoom.
 */
package com.ortiz.touch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.android.volley.toolbox.NetworkImageView;

public class TouchImageView extends NetworkImageView
{
    Matrix matrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 3;
    int mode = NONE;

    // Remember some things for zooming
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 3f;
    float[] m;
    
    float redundantXSpace, redundantYSpace;
    float width, height;  
    float saveScale = 1f;
    float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
    
    ScaleGestureDetector mScaleDetector;
    
    private float lastWidth = -1;
    private float lastHeight = -1;
    
    private boolean autoMeasure = true;
    
    Context context;


	public TouchImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
        super.setClickable(true);
        this.context = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        matrix.setTranslate(1f, 1f);
        m = new float[9];
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() 
        {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	//dumpEvent(event);
            	mScaleDetector.onTouchEvent(event);
            	//Log.e("mScaleDetector.onTouchEvent(event)",""+);
            	
            	

            	matrix.getValues(m);
            	float x = m[Matrix.MTRANS_X];
            	float y = m[Matrix.MTRANS_Y];
            	PointF curr = new PointF(event.getX(), event.getY());
            	

            	switch (event.getAction()) 
            	{
	            	case MotionEvent.ACTION_DOWN:
	                    last.set(event.getX(), event.getY());
	                    start.set(last);
	                    mode = DRAG;
	                    break;
	                    
                    case MotionEvent.ACTION_POINTER_DOWN:
                        last.set(event.getX(), event.getY());
                        start.set(last);
                        mode = ZOOM;
                        break;	                    
	                    
	            	case MotionEvent.ACTION_MOVE:
	            		if (mode == DRAG) 
	            		{
	            			float deltaX = curr.x - last.x;
	            			float deltaY = curr.y - last.y;
	            			float scaleWidth = Math.round(origWidth * saveScale);
	            			float scaleHeight = Math.round(origHeight * saveScale);
            				if (scaleWidth < width) 
            				{
	            				deltaX = 0;
	            				if (y + deltaY > 0)
		            				deltaY = -y;
	            				else if (y + deltaY < -bottom)
		            				deltaY = -(y + bottom); 
            				} 
            				else if (scaleHeight < height) 
            				{
	            				deltaY = 0;
	            				if (x + deltaX > 0)
		            				deltaX = -x;
		            			else if (x + deltaX < -right)
		            				deltaX = -(x + right);
            				} 
            				else 
            				{
	            				if (x + deltaX > 0)
		            				deltaX = -x;
		            			else if (x + deltaX < -right)
		            				deltaX = -(x + right);
		            			
	            				if (y + deltaY > 0)
		            				deltaY = -y;
		            			else if (y + deltaY < -bottom)
		            				deltaY = -(y + bottom);
	            			}
            				
            				/*
            				if ( deltaX == 0 && deltaY == 0 )
            				{
            					return false;
            				}*/
                        	matrix.postTranslate(deltaX, deltaY);
                        	last.set(curr.x, curr.y);
	                    }
	            		break;
	            		
	            	case MotionEvent.ACTION_UP:
	            		mode = NONE;
	            		int xDiff = (int) Math.abs(curr.x - start.x);
	                    int yDiff = (int) Math.abs(curr.y - start.y);
	                    if (xDiff < CLICK && yDiff < CLICK)
	                    {
	                    	long d = event.getEventTime() - event.getDownTime();
	                        if ( d < 1000 )
	                        {
	                         	performClick();
	                        }
	                        else
	                        {
	                        	performLongClick();
	                        }
	                    }
	            		break;
	            		
	            	case MotionEvent.ACTION_POINTER_UP:
	            		mode = NONE;
	            		break;
            	}
                setImageMatrix(matrix);
                invalidate();
                return true; // indicate event was handled
            }

        });
    }

	
	public boolean canMove(MotionEvent event)
	{
    	matrix.getValues(m);
    	//float x = m[Matrix.MTRANS_X];
    	float scaleWidth = Math.round(origWidth * saveScale);
		if (scaleWidth > this.getWidth() )
		{
			return true;
		}

		
		return false;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		try
		{
			super.onDraw(canvas);
		}
		catch (Throwable e)
		{
			Log.e("ImageView", "draw",  e);
		}		
	}
	
    @Override
	public void setImageDrawable(Drawable drawable)
	{
		super.setImageDrawable(drawable);
        if  (  drawable == null )
        {
        	bmWidth = 0;
        	bmHeight = 0;
        }
        else
        {
        	bmWidth = drawable.getIntrinsicWidth();
        	bmHeight = drawable.getIntrinsicHeight();        	
        }
	}


	@Override
    public void setImageBitmap(Bitmap bm) { 
        super.setImageBitmap(bm);
        if  (  bm == null )
        {
        	bmWidth = 0;
        	bmHeight = 0;
        }
        else
        {
        	bmWidth = bm.getWidth();
        	bmHeight = bm.getHeight();        	
        }
    }
    
    public void setMaxZoom(float x)
    {
    	maxScale = x;
    }
    
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    	@Override
    	public boolean onScaleBegin(ScaleGestureDetector detector) {
    		mode = ZOOM;
    		return true;
    	}
    	
		@Override
	    public boolean onScale(ScaleGestureDetector detector) {
			float mScaleFactor = (float)Math.min(Math.max(.95f, detector.getScaleFactor()), 1.05);
		 	float origScale = saveScale;
	        saveScale *= mScaleFactor;
	        if (saveScale > maxScale) 
	        {
	        	saveScale = maxScale;
	        	mScaleFactor = maxScale / origScale;
	        } 
	        else if (saveScale < minScale) {
	        	saveScale = minScale;
	        	mScaleFactor = minScale / origScale;
	        }
	        
        	right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
        	if (origWidth * saveScale <= width || origHeight * saveScale <= height) 
        	{
        		matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2);
            	if (mScaleFactor < 1)
            	{
            		matrix.getValues(m);
            		float x = m[Matrix.MTRANS_X];
                	float y = m[Matrix.MTRANS_Y];
                	if (mScaleFactor < 1) 
                	{
        	        	if (Math.round(origWidth * saveScale) < width) 
        	        	{
        	        		if (y < -bottom)
            	        		matrix.postTranslate(0, -(y + bottom));
        	        		else if (y > 0)
            	        		matrix.postTranslate(0, -y);
        	        	}
        	        	else 
        	        	{
	                		if (x < -right) 
	        	        		matrix.postTranslate(-(x + right), 0);
	                		else if (x > 0) 
	        	        		matrix.postTranslate(-x, 0);
        	        	}
                	}
            	}
        	} 
        	else 
        	{
            	matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
            	matrix.getValues(m);
            	float x = m[Matrix.MTRANS_X];
            	float y = m[Matrix.MTRANS_Y];
            	if (mScaleFactor < 1) {
    	        	if (x < -right) 
    	        		matrix.postTranslate(-(x + right), 0);
    	        	else if (x > 0) 
    	        		matrix.postTranslate(-x, 0);
    	        	if (y < -bottom)
    	        		matrix.postTranslate(0, -(y + bottom));
    	        	else if (y > 0)
    	        		matrix.postTranslate(0, -y);
            	}
        	}
	        return true;
	        
	    }
	}
    
    
    
    public boolean isAutoMeasure()
	{
		return autoMeasure;
	}


	public void setAutoMeasure(boolean autoMeasure)
	{
		this.autoMeasure = autoMeasure;
	}


	@Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        
        if ( autoMeasure || (lastWidth == -1 || lastHeight == -1) )
        {
        	lastWidth  = width;
        	lastHeight = height;
            
            //Fit to screen.
            float scale;
            float scaleX =  (float)width / (float)bmWidth;
            float scaleY = (float)height / (float)bmHeight;
            scale = Math.min(scaleX, scaleY);
            matrix.setScale(scale, scale);
            setImageMatrix(matrix);
            saveScale = 1f;

            // Center the image
            redundantYSpace = (float)height - (scale * (float)bmHeight) ;
            redundantXSpace = (float)width - (scale * (float)bmWidth);
            redundantYSpace /= (float)2;
            redundantXSpace /= (float)2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);
            
            origWidth = width - 2 * redundantXSpace;
            origHeight = height - 2 * redundantYSpace;
            right = width * saveScale - width - (2 * redundantXSpace * saveScale);
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale);
            setImageMatrix(matrix);
        }
    } 
}
