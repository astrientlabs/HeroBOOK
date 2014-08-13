package com.brilliancemobility.heroes.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.brilliancemobility.heroes.R;

public class DatabaseTable
{
	private static final String TAG = "DictionaryDatabase";
	public static final String COL_WORD = "WORD";

	private static final String DATABASE_NAME = "HEROBOOKDB";
	private static final String FTS_VIRTUAL_TABLE = "NAMES";
	private static final int DATABASE_VERSION = 1;

	private final DatabaseOpenHelper mDatabaseOpenHelper;

	
	private static DatabaseTable instance;
	
	public static DatabaseTable getInstance(Context context)
	{
		if ( instance == null )
		{
			instance = new DatabaseTable(context);
		}
		
		return instance;
	}
	
	private DatabaseTable(Context context)
	{
		mDatabaseOpenHelper = new DatabaseOpenHelper(context);
	}
	
	public void init()
	{
		mDatabaseOpenHelper.getReadableDatabase();
	}

	public Cursor getWordMatches(String query, String[] columns)
	{
		String selection = COL_WORD + " MATCH ?";
		String[] selectionArgs = new String[] { query + "*" };

		return query(selection, selectionArgs, columns);
	}

	private Cursor query(String selection, String[] selectionArgs, String[] columns)
	{
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(FTS_VIRTUAL_TABLE);

		Cursor cursor = builder.query(
				mDatabaseOpenHelper.getReadableDatabase(), columns, selection,
				selectionArgs, null, null, null);

		if (cursor == null)
		{
			return null;
		} 
		else if (!cursor.moveToFirst())
		{
			cursor.close();
			return null;
		}
		return cursor;
	}
	
	public ArrayList<String> getWordMatches(String query)
	{
		String selection = COL_WORD + " MATCH ?";
		String[] selectionArgs = new String[] { query + "*" };

		Cursor cursor = query(selection, selectionArgs, null);
		ArrayList<String> results = new ArrayList<String>();
		try
		{
			
			if ( cursor != null && cursor.getCount() > 0 )
			{
				int index = cursor.getColumnIndex(COL_WORD);
				cursor.moveToFirst();
				while ( !cursor.isAfterLast() )
				{
					results.add(cursor.getString(index));

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
		
		return results;
	}	
	
	public String[] getWordMatchesArray(String query)
	{
		String selection = COL_WORD + " MATCH ?";
		String[] selectionArgs = new String[] { query + "*" };

		Cursor cursor = query(selection, selectionArgs, null);
		ArrayList<String> results = new ArrayList<String>();
		try
		{
			
			if ( cursor != null && cursor.getCount() > 0 )
			{
				int index = cursor.getColumnIndex(COL_WORD);
				cursor.moveToFirst();
				while ( !cursor.isAfterLast() )
				{
					results.add(cursor.getString(index));

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
		
		return results.toArray(new String[results.size()]);
	}
	
	
	private static class DatabaseOpenHelper extends SQLiteOpenHelper
	{

		private final Context mHelperContext;
		private SQLiteDatabase mDatabase;

		private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
				+ FTS_VIRTUAL_TABLE + " USING fts4 (" + COL_WORD + ")";

		DatabaseOpenHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mHelperContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			mDatabase = db;
			mDatabase.execSQL(FTS_TABLE_CREATE);
			loadDictionary();
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
			onCreate(db);
		}

		private void loadDictionary()
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						loadWords();
					} 
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}).start();
		}

		private void loadWords() throws IOException
		{
			final Resources resources = mHelperContext.getResources();
			InputStream inputStream = resources.openRawResource(R.raw.names);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			try
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					long id = addWord(line.trim());
					if (id < 0)
					{
						Log.e(TAG, "unable to add word: " + line);
					}
				}
			} finally
			{
				reader.close();
			}
		}

		public long addWord(String word)
		{
			ContentValues initialValues = new ContentValues();
			initialValues.put(COL_WORD, word);
			return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
		}
		
	    @Override
		protected void finalize() throws Throwable
		{
			close();
			super.finalize();		
		}		

	}
}
