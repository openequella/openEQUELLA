package com.dytech.common.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Substitution
{
	protected Resolver resolver;
	protected String begin;
	protected String end;

	public Substitution(Resolver resolver)
	{
		this.resolver = resolver;
		parseMarker("${ }"); //$NON-NLS-1$
	}

	public Substitution(Resolver resolver, String marker)
	{
		this.resolver = resolver;
		parseMarker(marker);
	}

	public String resolve(String s) throws ResolverException
	{
		int start = s.indexOf(begin);
		if( start < 0 )
		{
			return s;
		}

		int start2 = start + begin.length();
		int finish = s.indexOf(end, start2);
		if( finish < 0 || finish < start2 )
		{
			throw new ResolverException("finish is less than 0 or start");
		}
		else
		{
			String result = resolver.valueOf(s.substring(start2, finish));
			return s.substring(0, start) + result + resolve(s.substring(finish + 1));
		}
	}

	public void resolve(BufferedReader in, BufferedWriter out) throws IOException, ResolverException
	{
		for( String line = in.readLine(); line != null; line = in.readLine() )
		{
			line = resolve(line);
			out.write(line);
			out.newLine();
		}
	}

	protected void parseMarker(String marker)
	{
		int space = marker.indexOf(' ');
		begin = marker.substring(0, space);
		end = marker.substring(space + 1);
	}
}
