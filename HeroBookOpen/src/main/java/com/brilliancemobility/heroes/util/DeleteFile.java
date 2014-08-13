package com.brilliancemobility.heroes.util;

import java.io.File;
import java.util.LinkedList;

import android.util.Log;

public class DeleteFile implements Runnable
{
	private File file;
	
	public DeleteFile(File file)
	{
		this.file = file;
	}

	@Override
	public void run()
	{
		try
		{
			if ( file != null && file.exists() )
			{
				if ( file.isDirectory() )
				{
					delete(file);
				}
				else
				{
					file.delete();
				}
			}	
		}
		catch (Throwable t)
		{
			Log.e("DeleteFile","run",t);
		}
	}
	
	public void delete(File path)
	{
		if (path.exists())
		{
			LinkedList<File> files = new LinkedList<File>();
			files.add(path);
			
			File file;
			while ( !files.isEmpty() )
			{
				file = files.removeFirst();
				if ( file.isDirectory() )
				{
					for ( File f : file.listFiles() )
					{
						files.add(f);
					}
				}
				else
				{
					file.delete();
				}
			}
		}
	}
}
