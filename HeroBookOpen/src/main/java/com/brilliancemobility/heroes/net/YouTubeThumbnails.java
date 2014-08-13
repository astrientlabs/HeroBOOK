package com.brilliancemobility.heroes.net;

import com.google.gson.annotations.SerializedName;

public class YouTubeThumbnails
{
	@SerializedName("default")
    public YouTubeThumbnail defaultUrl;
    public YouTubeThumbnail medium;
    public YouTubeThumbnail high;
    
    /*"thumbnails": {
     "default": {
      "url": "https://i.ytimg.com/vi/l6_SQYDuGjM/default.jpg"
     },
     "medium": {
      "url": "https://i.ytimg.com/vi/l6_SQYDuGjM/mqdefault.jpg"
     },
     "high": {
      "url": "https://i.ytimg.com/vi/l6_SQYDuGjM/hqdefault.jpg"
     }
    },*/
}
