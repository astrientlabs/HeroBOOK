package com.brilliancemobility.heroes.net;

public class MarvelResponse<T>
{
	public int code;
	public String status;
	public String etag;
	public MarvelData<T> data;
}
