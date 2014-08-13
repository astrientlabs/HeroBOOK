package com.brilliancemobility.heroes.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.TextView;

public class FooterTextView extends TextView
{
	public FooterTextView(Context context)
	{
		super(context);
	}

	public FooterTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public FooterTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	public boolean performClick()
	{
		Context context = this.getContext();
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.marvel.com"));
		context.startActivity(intent);
		return true;
	}
}
