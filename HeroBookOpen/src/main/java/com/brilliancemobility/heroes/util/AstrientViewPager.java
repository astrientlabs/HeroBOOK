package com.brilliancemobility.heroes.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AstrientViewPager extends ViewPager
{
	private ScrollChecker scrollChecker;

	public AstrientViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public AstrientViewPager(Context context)
	{
		super(context);
	}
	
	
	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) 
	{
		return !shouldScroll(null);
	}
	
	protected boolean shouldScroll(MotionEvent event)
	{
		if ( scrollChecker == null )
		{
			return true;
		}
		else
		{
			return scrollChecker.shouldScroll(event);
		}
	}

	public ScrollChecker getScrollChecker()
	{
		return scrollChecker;
	}

	public void setScrollChecker(ScrollChecker scrollChecker)
	{
		this.scrollChecker = scrollChecker;
	}
	
	
}
