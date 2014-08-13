package com.brilliancemobility.heroes.util;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

public class HtmlTextView extends TextView
{

	public HtmlTextView(Context context)
	{
		super(context);
	}

	public HtmlTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public HtmlTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	
	
	
	
	@Override
	public void setText(CharSequence text, BufferType type)
	{
		super.setText(Html.fromHtml(text.toString()), BufferType.SPANNABLE);
	}
}
