package com.brilliancemobility.heroes;

import java.util.Locale;

import android.content.Context;

public enum URLName
{
	DETAIL(R.string.link_details),
	PURCHASE(R.string.link_purchase),
	READER(R.string.link_reader),
	INAPPLINK(R.string.link_inapplink),
	UNKNOWN(R.string.link_unknown);

	private int id;
	
	URLName(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getName(Context context)
	{
		return context.getString(id);
	}
	
	public String getName(Context context, Object... args)
	{
		return context.getString(id,args);
	}
	
	
	public static String getURLName(Context context, String type)
	{
		try
		{
			URLName name = valueOf(type.toUpperCase(Locale.US));
			return name.getName(context);
		}
		catch (IllegalArgumentException e)
		{
			return UNKNOWN.getName(context, type);
		}		
	}
}
