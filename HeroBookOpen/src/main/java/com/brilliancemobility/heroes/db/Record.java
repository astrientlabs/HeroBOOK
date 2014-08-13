/*******************************************************************************
 * Copyright (c) 2009 Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors:
 * 
 * Astrient Foundation Inc. 
 * www.astrientfoundation.org
 * rashid@astrientfoundation.org
 * Rashid Mayes 2009
 *******************************************************************************/
package com.brilliancemobility.heroes.db;

import android.content.ContentValues;
import android.database.Cursor;



public abstract class Record
{
	public static final int NO_RECORD_ID = -1;

	protected static final String STD_CREATE =  "_id INTEGER primary key autoincrement, guid TEXT UNIQUE NULL, sync_state INTEGER, modified INTEGER, created INTEGER, ";
	public static final String DBNAME = "stumblr";
	/*
	protected static String TABLE_NAME;
	protected static String CREATE_STATEMENT;
	protected static String[] COLUMNS;
	protected static Class<? extends Record> CLASS;
	*/
	
	public static final String COL_ID = "_id";
	public static final String COL_SYNC_STATE = "sync_state";
	public static final String COL_CREATED = "created";
	public static final String COL_MODIFIED = "modified";
	public static final String COL_GUID = "guid";
	
	public static final int SYNC_STATE_OK = 0;
	public static final int SYNC_STATE_CONFLICT = 1;
	public static final int SYNC_STATE_DELETED = 2;
	
    protected boolean dirty = false;
    protected boolean new_ = true;
    
    protected long luid = NO_RECORD_ID;
    protected String guid; 
    protected long modified;
    protected long created;
    protected int syncState;

    public static String getTableName()
    {
    	return "Sdf";
    }
    
    public long getLuid()
	{
		return luid;
	}

	public void setLuid(long luid)
	{
		this.luid = luid;
		dirty = true;
	}

	public String getGuid()
	{
		return guid;
	}

	public void setGuid(String guid)
	{
		this.guid = guid;
		dirty = true;
	}

	public long getModified()
	{
		return modified;
	}

	public void setModified(long modified)
	{
		this.modified = modified;
		dirty = true;
	}

	public long getCreated()
	{
		return created;
	}

	public void setCreated(long created)
	{
		this.created = created;
		dirty = true;
	}

	public int getSyncState()
	{
		return syncState;
	}

	public void setSyncState(int syncState)
	{
		this.syncState = syncState;
		dirty = true;
	}

	public boolean isDirty()
    {
        return this.dirty;
    }

    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }
    
    public boolean isNew()
    {
        return this.luid == NO_RECORD_ID;
    }
    
    public void setNew(boolean new_)
    {
        this.new_ = new_;
    }
    
    public void load(Cursor cursor)
    {
    	this.syncState = cursor.getInt(cursor.getColumnIndex(COL_SYNC_STATE));
    	this.luid = cursor.getInt(cursor.getColumnIndex(COL_ID));
    	this.guid = cursor.getString(cursor.getColumnIndex(COL_GUID));
    	this.created = cursor.getLong(cursor.getColumnIndex(COL_CREATED));
    	this.modified = cursor.getLong(cursor.getColumnIndex(COL_MODIFIED));
    	
    	_load(cursor);
    }
    
    protected ContentValues getContentValues()
    {
    	ContentValues values = new ContentValues();
    	values.put(COL_GUID,guid);
    	values.put(COL_CREATED,created);
    	values.put(COL_MODIFIED,modified);
    	values.put(COL_SYNC_STATE,syncState);
    	
    	setContentValues(values);
    	
    	return values;
    }
    
    
    public ContentValues getContentValues(Object o)
    {
    	ContentValues values = new ContentValues();
    	values.put(COL_GUID,guid);
    	values.put(COL_CREATED,created);
    	values.put(COL_MODIFIED,modified);
    	values.put(COL_SYNC_STATE,syncState);
    	
    	setContentValues(values);
    	
    	return values;
    }
    
    protected abstract void _load(Cursor cursor);
    protected abstract void setContentValues(ContentValues values);
    public abstract void getDupeFields(ContentValues values);
}
