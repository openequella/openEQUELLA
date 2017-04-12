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