package com.brilliancemobility.heroes.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public final class DBHelper extends SQLiteOpenHelper
{
    private static DBHelper instance;
    
    private DBHelper(Context context)
    {
        super(context, "heroes", null, 3);
    }
    
    public static DBHelper getInstance(Context context)
    {
    	if ( instance == null )
    	{
    		try
    		{
    			instance = new DBHelper(context.getApplicationContext());
    		}
    		catch (Exception e)
    		{
    			instance = new DBHelper(context);
    			Log.e("DBHelper","getContext",e);
    		}
    	}
    	
    	return instance;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
    		db.execSQL(BookmarkRecord.CREATE_STATEMENT);
    		db.execSQL(HistoryRecord.CREATE_STATEMENT);
    		db.execSQL("PRAGMA foreign_keys=ON;");
        }
        catch(Exception e)
        {
            Log.e(getClass().getName(), "onCreate", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
       try
       {

       }
       catch(Throwable e)
       {
    	   Log.e(getClass().getName(), "onCreate", e);
       }
       
       onCreate(db);
    }
    
    @Override
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();		
	}    
}