/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.installer.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import com.dytech.common.text.Substitution;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.XpathResolver;

public class CResolver extends Command
{
	protected PropBagEx resultBag;
	protected String source;
	protected String destination;
	protected Substitution sub;

	public CResolver(PropBagEx resultBag, String holder, String source, String destination)
	{
		this.resultBag = resultBag;
		this.source = source;
		this.destination = destination;

		sub = new Substitution(new XpathResolver(resultBag), holder);
	}

	@Override
	public void execute() throws InstallerException
	{
		propogateTaskStarted(2);

		try( BufferedReader r = new BufferedReader(new FileReader(source));
			BufferedWriter w = new BufferedWriter(new FileWriter(destination)) )
		{
			propogateSubtaskCompleted();
			sub.resolve(r, w);
		}
		catch( Exception e )
		{
			throw new InstallerException("Problem Resolving File", e);
		}

		propogateSubtaskCompleted();
		propogateTaskCompleted();
	}

	@Override
	public String toString()
	{
		return "Customising " + destination + " file to machine.";
	}
}