/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
