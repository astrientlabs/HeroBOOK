package com.brilliancemobility.heroes;

import com.brilliancemobility.heroes.db.BookmarkRecord;
import com.brilliancemobility.heroes.db.DBHelper;
import com.brilliancemobility.heroes.db.HistoryRecord;
import com.brilliancemobility.heroes.db.Record;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class DBContentProvider extends ContentProvider
{
	private static final String AUTHORITY = "com.brilliancemobility.heroes.provider";
	
	public static final int BOOKMARKS = 10;
	public static final int BOOKMARKS_ID = 11;
	public static final int HISTORY = 20;	
	public static final int HISTORY_ID = 21;
	
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static
	{
		sURIMatcher.addURI(AUTHORITY, "bookmarks", BOOKMARKS);
		sURIMatcher.addURI(AUTHORITY, "bookmarks/#", BOOKMARKS_ID);
		sURIMatcher.addURI(AUTHORITY, "history", HISTORY);
		sURIMatcher.addURI(AUTHORITY, "history/#", HISTORY_ID);
	}
	
	
	private DBHelper mDBHelper;
	
	public DBContentProvider()
	{
		
	}
	
	@Override
	public boolean onCreate()
	{
		mDBHelper = DBHelper.getInstance(getContext());
		return true;
	}
	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		int uriType = sURIMatcher.match(uri);
	    
		
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    switch (uriType) 
	    {
	    	case BOOKMARKS: queryBuilder.setTables(BookmarkRecord.TABLE_NAME);
	    		break;
	    	case BOOKMARKS_ID: queryBuilder.setTables(BookmarkRecord.TABLE_NAME);
	    		queryBuilder.appendWhere(Record.COL_ID + "=" + uri.getLastPathSegment());
	    		break;
	    	case HISTORY: queryBuilder.setTables(HistoryRecord.TABLE_NAME);
    			break;
	    	case HISTORY_ID: queryBuilder.setTables(HistoryRecord.TABLE_NAME);
    			queryBuilder.appendWhere(Record.COL_ID + "=" + uri.getLastPathSegment());
    			break;	    		
	    	default: throw new IllegalArgumentException("Unknown URI: " + uri);
	    }

	    
	    SQLiteDatabase db = mDBHelper.getWritableDatabase();
	    Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    
	    return cursor;
	}	

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String path;

		int uriType = sURIMatcher.match(uri);
	    switch (uriType) 
	    {
	    	case BOOKMARKS: path = "bookmarks/" + db.insert(BookmarkRecord.TABLE_NAME, null, values);
	    		break;
	    	case HISTORY: path = "history/" + db.insert(HistoryRecord.TABLE_NAME, null, values);
    			break;    		
	    	default: throw new IllegalArgumentException("Unknown URI: " + uri);
	    }

		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(path);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		
		int rowsDeleted = 0;

	    switch (uriType) 
	    {
	    	case BOOKMARKS: rowsDeleted = db.delete(BookmarkRecord.TABLE_NAME, selection,selectionArgs);
	    		break;
	    	case HISTORY: rowsDeleted = db.delete(HistoryRecord.TABLE_NAME, selection,selectionArgs);
    			break;  
    			
	    	case BOOKMARKS_ID: String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection))
				{
					rowsDeleted = db.delete(BookmarkRecord.TABLE_NAME,BookmarkRecord.COL_ID + "=" + id, null);
				} 
				else
				{
					rowsDeleted = db.delete(BookmarkRecord.TABLE_NAME,BookmarkRecord.COL_ID + "=" + id + " and " + selection, selectionArgs);
				}	    	
	    		
    			break;
	    	case HISTORY_ID: id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection))
				{
					rowsDeleted = db.delete(HistoryRecord.TABLE_NAME,HistoryRecord.COL_ID + "=" + id, null);
				} 
				else
				{
					rowsDeleted = db.delete(HistoryRecord.TABLE_NAME,HistoryRecord.COL_ID + "=" + id + " and " + selection, selectionArgs);
				}	    	
    		
    			break;     			
    			
	    	default: throw new IllegalArgumentException("Unknown URI: " + uri);
	    }		

		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int rowsUpdated = 0;
		
		switch (uriType)
		{
			case BOOKMARKS: rowsUpdated = db.update(BookmarkRecord.TABLE_NAME, values, selection, selectionArgs);
				break;
			case BOOKMARKS_ID: String id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection))
				{
					rowsUpdated = db.update(BookmarkRecord.TABLE_NAME, values, BookmarkRecord.COL_ID + "=" + id, null);
				} 
				else
				{
					rowsUpdated = db.update(BookmarkRecord.TABLE_NAME, values, BookmarkRecord.COL_ID + "=" + id + " and " + selection, selectionArgs);
				}
				break;
			case HISTORY: rowsUpdated = db.update(HistoryRecord.TABLE_NAME, values, selection, selectionArgs);
				break;
			case HISTORY_ID: id = uri.getLastPathSegment();
				if (TextUtils.isEmpty(selection))
				{
					rowsUpdated = db.update(HistoryRecord.TABLE_NAME, values, HistoryRecord.COL_ID + "=" + id, null);
				} 
				else
				{
					rowsUpdated = db.update(HistoryRecord.TABLE_NAME, values, HistoryRecord.COL_ID + "=" + id + " and " + selection, selectionArgs);
				}
				break;				
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public String getType(Uri uri)
	{
		throw null;
	}
}
