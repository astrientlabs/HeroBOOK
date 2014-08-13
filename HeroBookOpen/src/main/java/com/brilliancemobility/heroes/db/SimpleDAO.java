package com.brilliancemobility.heroes.db;


import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SimpleDAO<E extends Record>
{
    private static SQLiteDatabase db;
    private String tableName;
    private Class<E> clazz;
    private String[] columns;
    
    
    protected SimpleDAO()
    {

    }
    
    public SimpleDAO(Context context, String tableName, String[] columns, Class<E> clazz)
    {
        this.tableName = tableName;
        this.clazz = clazz;
        this.columns = columns;

        db = DBHelper.getInstance(context).getWritableDatabase();
    }
    
    public void sql(String sql)
    {
        try
        {
            db.execSQL(sql);
        }
        catch(Exception e)
        {
            Log.e(getClass().getName(), "sql", e);
        }
    }
        
	public E get(long id)
    {
        Cursor cursor = db.query(true, tableName, columns, Record.COL_ID + "=" + id, null, null, null, null, null);
        try
        {
            if (cursor != null && cursor.getCount() > 0) 
            {
                cursor.moveToFirst();
                
            	try
            	{
                	E record = clazz.newInstance();
                	cursor.moveToFirst();
                	record.load(cursor);
                	
                	
                	return record;
            	}
            	catch (Exception e)
            	{
            		Log.e(tableName,"get()",e);
            	}
               
            }
        }
    	finally
    	{
    		if ( cursor != null ) cursor.close();
    	} 
    	

    	
    	
    	return null;
    }
    
	public E get(String guid)
    {
        Cursor cursor = db.query(true, tableName, columns, Record.COL_GUID + " = '" + guid + "'", null, null, null, null, null);
        try
        {
            if (cursor != null && cursor.getCount() > 0) 
            {
                cursor.moveToFirst();
                
            	try
            	{
                	E record = (clazz.newInstance());
                	record.load(cursor);
                	cursor.close();
                	
                	return record;
            	}
            	catch (Exception e)
            	{
            		Log.e(tableName,"get()",e);
            	}             
            }
        }
    	finally
    	{
    		if ( cursor != null ) cursor.close();
    	} 


    	return null;
    }    
    
	public E newInstance() throws InstantiationException, IllegalAccessException
	{
		return (clazz.newInstance());
	}
	
	public E get(String column, String value)
    {
        Cursor cursor = this.find(column, value);
        try
        {
            if (cursor != null && cursor.getCount() > 0) 
            {
                cursor.moveToFirst();
                
            	try
            	{
                	E record = (clazz.newInstance());
                	record.load(cursor);
                	cursor.close();
                	
                	return record;
            	}
            	catch (Exception e)
            	{
            		Log.e(tableName,"get(k,v)",e);
            	}             
            }
        }
    	finally
    	{
    		if ( cursor != null ) cursor.close();
    	} 


    	return null;		
    }
    
	
    public List<E> records() throws IllegalAccessException, InstantiationException
    {
    	List<E> records = new ArrayList<E>();
		Cursor cursor = null;
		try
		{
			cursor = find("modified desc");
			
			if ( cursor != null && cursor.getCount() > 0 )
			{
				cursor.moveToFirst();
				E record = (clazz.newInstance());
				while ( !cursor.isAfterLast() )
				{
					record = (clazz.newInstance());
					record.load(cursor);
					
					records.add(record);

					cursor.moveToNext();
				}
			}
		}
		finally
		{
			if ( cursor != null )
			{
				cursor.close();
			}
		}
		
		return records;
    }
	
    public Cursor all()
    {
    	return db.query(tableName, columns, null, null, null, null, null);
    }
    
    public Cursor find()
    {
    	return db.query(tableName, columns, Record.COL_SYNC_STATE + " != " + Record.SYNC_STATE_DELETED, null, null, null, null);
    }
    
    public Cursor find(String where, String[] whereArgs, String orderBy)
    {
    	return db.query(tableName, columns, Record.COL_SYNC_STATE + " != " + Record.SYNC_STATE_DELETED + " and " + where, whereArgs, null, null, orderBy);
    }  
    
    public Cursor find(String orderBy)
    {
    	return db.query(tableName, columns, Record.COL_SYNC_STATE + " != " + Record.SYNC_STATE_DELETED, null, null, null, orderBy);
    }    
    
    public Cursor find(String column, String value)
    {
    	return db.query(tableName, columns, column + " = " + value + " and " + Record.COL_SYNC_STATE + " != " + Record.SYNC_STATE_DELETED, null, null, null, null);
    }
    
    public Cursor find(String column, String value, String orderBy)
    {
    	return db.query(tableName, columns, column + " = " + value + " and " + Record.COL_SYNC_STATE + " != " + Record.SYNC_STATE_DELETED, null, null, null, orderBy);
    }       
    
    public Cursor findChanges(long modifiedAfter)
    {
    	return db.query(tableName, columns, Record.COL_MODIFIED + " > " + modifiedAfter + " or guid is null or sync_state = 2", null, null, null, null);
    }
    
    public int delete(String guid)
    {
    	return db.delete(tableName, Record.COL_GUID + "=" + guid, null);
    }
    
    public int delete(long id)
    {
    	return delete(id,false);
    }
    
    public int delete(long id, boolean hard) 
    {
    	int rc = 0;
    	if ( hard )
    	{
    		db.beginTransaction();
    		try
    		{
    			rc = db.delete(tableName, Record.COL_ID + "=" + id, null);
    			db.setTransactionSuccessful();		
	        }
	    	finally
	    	{
	    		db.endTransaction();
	    	}
    	}
    	else
    	{
    		ContentValues values = new ContentValues();
    		values.put(Record.COL_SYNC_STATE,Record.SYNC_STATE_DELETED);
    		values.put(Record.COL_MODIFIED,System.currentTimeMillis());
    		rc = db.update(tableName, values, Record.COL_ID + "=" + id, null);
    	}
    	
    	return rc;
    } 
    
    public int delete(long id, String where, String... tableNames) 
    {
		ContentValues values = new ContentValues();
		values.put(Record.COL_SYNC_STATE,Record.SYNC_STATE_DELETED);
		values.put(Record.COL_MODIFIED,System.currentTimeMillis());
		int rc = db.update(tableName, values, Record.COL_ID + "=" + id, null);
    	
		if ( rc > 0 )
		{
			String sql; 
			for ( String tableName : tableNames )
			{
				sql = "update " + tableName + " set sync_state = 2 where " + where;
				db.execSQL(sql);
			}
		}
		
		return rc;
    }       
    
    public int update(E record) 
    {
    	int rc = 0;
    	db.beginTransaction();
        try
        {
        	rc = db.update(tableName, record.getContentValues(), Record.COL_ID + "=" + record.getLuid(), null);
        	db.setTransactionSuccessful();
        }
    	finally
    	{
    		db.endTransaction();
    	}
		
		return rc;
    }
    
    public long add(E record)
    {
    	return db.insert(tableName,null,record.getContentValues());
    }
    
    public long save(E record)
    {
    	if ( record.getLuid() == Record.NO_RECORD_ID )
    	{
    		long rowId = add(record);
    		if ( rowId != Record.NO_RECORD_ID ) record.setLuid(rowId);
    		return rowId;
    	}
    	else
    	{
    		return update(record);
    	}
    }
    
    public void save(ThreadPoolExecutor executor, E record)
    {
    	SaveRunnable sr = new SaveRunnable(record);
    	executor.execute(sr);
    }
    
    private class SaveRunnable implements Runnable
    {
    	E record;
    	
    	SaveRunnable(E record)
    	{
    		this.record = record;
    	}
    	
    	public void run()
    	{
    		save(record);
    	}
    }
    
    public int deleteAll(boolean hard) 
    {
    	int rc = 0;
    	if ( hard )
    	{
    		db.beginTransaction();
    		rc = db.delete(tableName, "1", null);
    		db.setTransactionSuccessful();
    		db.endTransaction();
    	}
    	else
    	{
    		ContentValues values = new ContentValues();
    		values.put(Record.COL_SYNC_STATE,Record.SYNC_STATE_DELETED);
    		values.put(Record.COL_MODIFIED,System.currentTimeMillis());
    		rc = db.update(tableName, values, "1", null);
    	}
    	
    	return rc;
    }   
    
    
    public int clearGuids() 
    {
		db.beginTransaction();
		int rc = db.delete(tableName, Record.COL_SYNC_STATE +  " = " + Record.SYNC_STATE_DELETED, null);

		
		ContentValues values = new ContentValues();
		values.put(Record.COL_SYNC_STATE,Record.SYNC_STATE_OK);
		values.put(Record.COL_GUID,(String)null);
		rc = db.update(tableName, values, "1", null);
		
		db.setTransactionSuccessful();
		db.endTransaction();
    	
    	return rc;
    } 
    
    
    public E getDupe(E record)
    {
		Cursor cursor = null;
		try
		{
			ContentValues values = new ContentValues();
			record.getDupeFields(values);
			StringBuffer where = new StringBuffer();
			ArrayList<String> whereArgs = new ArrayList<String>(values.size());
			
			boolean first = true;
			for ( Entry<String,Object> entry : values.valueSet() )
			{
				if ( !first )
				{
					
					where.append("and ");
				}
				
				first = false;
				where.append(entry.getKey()).append(" = ? ");
				whereArgs.add(String.valueOf(entry.getValue()));
			}
			
			cursor = this.find(where.toString(), (String[])whereArgs.toArray(new String[0]), null);
			
			if ( cursor != null && cursor.getCount() > 0 )
			{
				cursor.moveToFirst();
				E dupe = (clazz.newInstance());
				dupe.load(cursor);
				return dupe;
			}
		}
		catch (Exception e)
		{
			Log.e(this.tableName,"getdupe",e);
		}
		finally
		{
			if ( cursor != null )
			{
				cursor.close();
			}
		}
		
		return null;
    }
    
    public String getTableName()
    {
    	return tableName;
    }
}