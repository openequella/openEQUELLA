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

package com.tle.conversion;

import java.io.File;
import java.io.IOException;

public class Main
{
	private static Converter exporter = new Converter();

	public static void main(String[] args)
	{
		try
		{
			exporter = new Converter();
			convert(new File(args[0]), new File(args[1]));
		}
		catch( Exception th )
		{
			th.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.remoting.RemoteConversionService#convert(java.io.File,
	 * java.io.File)
	 */
	public static void convert(File from, File to) throws RuntimeException
	{
		String fromPath = from.getAbsolutePath();
		String toPath = to.getAbsolutePath();
		try
		{
			exporter.exportFile(fromPath, toPath);
		}
		catch( IOException ex )
		{
			throw new RuntimeException("Error", ex);
		}
	}
}
