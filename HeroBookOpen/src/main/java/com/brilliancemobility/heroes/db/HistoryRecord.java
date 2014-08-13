package com.brilliancemobility.heroes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.brilliancemobility.heroes.net.MarvelCharacter;
import com.brilliancemobility.heroes.util.Strings;
import com.google.gson.Gson;




public class HistoryRecord extends Record
{
	public static Uri CONTENT_URI = Uri.parse("content://com.brilliancemobility.heroes.provider/history");
	
	public static final String TABLE_NAME = "history";
	public static final String COL_TITLE = "title";
	public static final String COL_NAME = "name";
	public static final String COL_URL = "url";
	public static final String COL_TYPE = "type";
	public static final String COL_PAGE_NUMBER = "page_number";
	public static final String COL_PAGE_SIZE = "page_size";	
	
	public static final String CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + STD_CREATE + " title text not null, name text not null, url text not null, type text not null, page_number number, page_size number)";
	public static final String[] COLUMNS = { COL_ID, COL_GUID, COL_TITLE, COL_NAME, COL_URL, COL_TYPE, COL_PAGE_NUMBER, COL_PAGE_SIZE, COL_SYNC_STATE, COL_MODIFIED, COL_CREATED };

	private MarvelCharacter marvelCharacter;
	
    private String title;
    private String name;
    private String url;
    private String type;
    private int pageNumber;
    private int pageSize;
    
    public static SimpleDAO<HistoryRecord> getDao(Context context)
    {
    	return new SimpleDAO<HistoryRecord>(context, TABLE_NAME, COLUMNS, HistoryRecord.class);
    }
    
    public static String getTableName()
    {
    	return TABLE_NAME;
    }

    public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public int getPageNumber()
	{
		return pageNumber;
	}

	public void setPageNumber(int pageNumber)
	{
		this.pageNumber = pageNumber;
	}

	public int getPageSize()
	{
		return pageSize;
	}

	public void setPageSize(int pageSize)
	{
		this.pageSize = pageSize;
	}

	public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
        dirty = true;
    }
    
    

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
		dirty = true;
	}

	@Override
	protected void _load(Cursor cursor)
	{
		title = cursor.getString(cursor.getColumnIndex(COL_TITLE));
		name = cursor.getString(cursor.getColumnIndex(COL_NAME));
		url = cursor.getString(cursor.getColumnIndex(COL_URL));
		type = cursor.getString(cursor.getColumnIndex(COL_TYPE));
		pageNumber = cursor.getInt(cursor.getColumnIndex(COL_PAGE_NUMBER));
		pageSize = cursor.getInt(cursor.getColumnIndex(COL_PAGE_SIZE));		
	}

	protected void setContentValues(ContentValues values)
	{
		values.put(COL_TITLE,title);
		values.put(COL_NAME,name);
		values.put(COL_URL,url);
		values.put(COL_TYPE,type);
		values.put(COL_PAGE_NUMBER,pageNumber);
		values.put(COL_PAGE_SIZE,pageSize);		
	}
	
	public static void record(Context context, String name, String title, String type, String url, int pageNumber, int pageSize)
	{
		if ( !Strings.isNull(name) )
		{
			SimpleDAO<HistoryRecord> dao = HistoryRecord.getDao(context.getApplicationContext());
			HistoryRecord record = new HistoryRecord();
			record.setName(name);
			record.setTitle(Strings.ifNull(title, name));
			record.setType(type);
			record.setUrl(url);
			record.setPageNumber(pageNumber);
			record.setPageSize(pageSize);
			
			record.setCreated(System.currentTimeMillis());
			record.setModified(System.currentTimeMillis());
			dao.save(record);
		}
	}
	
	
	public static HistoryRecord get(SimpleDAO<HistoryRecord> dao, String type, String url)
	{
		HistoryRecord record = new HistoryRecord();
		record.setType(type);
		record.setUrl(url);

		return dao.getDupe(record);
	}
	

    public void getDupeFields(ContentValues values)
    {
    	values.put(COL_URL,url);
    	values.put(COL_SYNC_STATE,SYNC_STATE_OK);
    }
    
    public void setMarvelCharacter(MarvelCharacter mc)
    {
    	marvelCharacter = mc;
    	Gson gson = new Gson();
    	String json = gson.toJson(mc);
    	type = json;
    	name = mc.name;
    	title = mc.getAlias();
    	url = mc.resourceURI;
    }
    
    public MarvelCharacter getMarvelCharacter()
    {
    	if ( marvelCharacter == null )
    	{
        	Gson gson = new Gson();
        	marvelCharacter =  gson.fromJson(type,MarvelCharacter.class); 		
    	}

    	return marvelCharacter;
    }      
}