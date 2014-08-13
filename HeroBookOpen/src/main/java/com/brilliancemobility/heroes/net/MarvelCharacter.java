package com.brilliancemobility.heroes.net;

import android.content.Context;

import com.brilliancemobility.heroes.URLName;
import com.brilliancemobility.heroes.util.Strings;

public class MarvelCharacter
{
	public int id;
	public String name;
	public String description;
	public String modified;
	public String resourceURI;
	public Thumbnail thumbnail;
	public MarvelUrl[] urls;
	public Comics comics;
	private String alias;
	private String realName;
	
	private String longDescription;
	
	public String getLink()
	{
		String link = "http://www.marvel.com";
		if ( urls != null && urls.length > 0 )
		{
			for (MarvelUrl u : urls)
			{
				if ( "details".equalsIgnoreCase(u.type) )
				{
					return u.url;
				}
			}
			
			link = urls[0].url;
		}
		
		return link;
	}
	
	
	public String getLongDescription(Context context)
	{
		if ( longDescription == null )
		{
			StringBuffer buffer = new StringBuffer();
			if ( !Strings.isNull(description) )
			{
				buffer.append(Strings.fixQuotes(description).replaceAll("\\p{C}", " "));
			}
			
			if ( this.urls != null )
			{
				buffer.append("<p>");
				for ( MarvelUrl u : urls )
				{
					buffer.append("&#149; <a href=\"").append(u.url).append("\">").append(URLName.getURLName(context,u.type)).append("</a><br/>\n");
				}
				buffer.append("</p>");
			}
			
			longDescription = buffer.toString();
		}
		
		return longDescription;
	}
	

	public String getRealName()
	{
		if ( name != null && realName == null )
		{
			String[] parts = name.split("\\(");
			if ( parts.length > 1 )
			{
				this.realName = parts[1].replaceAll("\\)","").trim();
				this.alias = parts[0].trim();
			}
			else
			{
				this.realName = name;
				this.alias = name;
			}
		}
		
		return realName;
	}
	
	
	public String getAlias()
	{
		if ( name != null && alias == null )
		{
			String[] parts = name.split("\\(");
			if ( parts.length > 1 )
			{
				this.realName = parts[1].replaceAll("\\)","").trim();
				this.alias = parts[0].trim();
			}
			else
			{
				this.realName = name;
				this.alias = name;
			}
		}
		
		return alias;
	}
}
