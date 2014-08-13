package com.brilliancemobility.heroes.util;

import android.content.Context;

public class AnalyticsUtils 
{
	public static final String CATEGORY_COMICS = "Comics";
	public static final String CATEGORY_CHARACTERS = "Characters";
	public static final String CATEGORY_PHOTOS = "Photos";
	public static final String CATEGORY_SEARCH = "Search";
	public static final String CATEGORY_VIDEOS = "Videos";

    public static void trackSearch(Context context, String label, long value) 
    {

    }	
	
	
    public static void trackPhotoViews(Context context, String label, long value) 
    {

    }
    
    
    
    public static void trackVideoViews(Context context, String label, long value) 
    {

    }    
    
    
    public static void trackCharacterViews(Context context, int id, String label, long value) 
    {

    }
    
    
    public static void trackComicViews(Context context, int id, String label, long value) 
    {

    }    
	

    public static void trackEvent(Context context, String category, String action, String label, long value) 
    {

    }
}