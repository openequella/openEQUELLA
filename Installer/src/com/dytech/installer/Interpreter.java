/*
 * Copyright 2019 Apereo
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

package com.dytech.installer;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Vector;

import com.dytech.common.text.ResolverException;
import com.dytech.common.text.Substitution;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.commands.*;

public class Interpreter implements TaskListener
{
	protected Progress progress;
	protected PropBagEx commandBag;
	protected PropBagEx resultBag;
	protected Vector commands;
	protected Substitution resolver;
	protected String thisPlatform;
	protected Command onFailure;

	public Interpreter(PropBagEx commandBag, PropBagEx resultBag, Progress progress) throws InstallerException
	{
		this.commandBag = commandBag;
		this.resultBag = resultBag;
		this.progress = progress;

		thisPlatform = resultBag.getNode("installer/platform");

		resolver = new Substitution(new XpathResolver(resultBag), "${ }");
		commands = processCommands(commandBag);
		onFailure = processFailureCommand(commandBag);
	}

	public void execute() throws InstallerException
	{
		createWindow();

		progress.addMessage("Starting Installation...\n");

		boolean success = true;
		Iterator i = commands.iterator();
		while( i.hasNext() && success )
		{
			Command c = (Command) i.next();
			c.addTaskListener(this);
			c.setProgress(progress);
			progress.addMessage(c.toString());

			try
			{
				c.execute();
			}
			catch( InstallerException e )
			{
				if( c.isMandatory() )
				{
					progress.popupMessage("FAILURE", "An error occurred during the installation process. \n"
						+ "The error provided by the application was: \n" + "\uFFED  " + e.getMessage(), true);
					progress.addMessage("FAILURE: " + e.getMessage());
					e.printStackTrace();

					System.out.println("Executing failure command");
					onFailure.setProgress(progress);
					onFailure.addTaskListener(this);
					onFailure.execute();
					success = false;
				}
				else
				{
					progress.addMessage("SKIPPED: " + e.getMessage());
				}
			}
			progress.addMessage(" ");
		}

		if( success )
		{
			progress.setCurrentAmount(progress.getCurrentMaximum());
			progress.setWholeAmount(progress.getWholeMaximum());

			progress.addMessage("Installation Completed.");

			progress.popupMessage("Complete", "Installation Completed Successfully.", false);
			System.exit(0);
		}
	}

	@Override
	public void taskStarted(int subtasks)
	{
		progress.setCurrentMaximum(subtasks);
		progress.setCurrentAmount(0);
	}

	@Override
	public void taskCompleted()
	{
		progress.setCurrentAmount(progress.getCurrentMaximum());
		progress.setWholeAmount(progress.getWholeAmount() + 1);
	}

	@Override
	public void subtaskCompleted()
	{
		progress.setCurrentAmount(progress.getCurrentAmount() + 1);
	}

	public void createWindow()
	{
		String title = resultBag.getNode("installer/product/name");
		int total = commands.size();
		progress.setup(title, total);
	}

	protected Vector processCommands(PropBagEx bag) throws InstallerException
	{
		final int numberOfCommands = bag.nodeCount("command");
		System.out.println("Processing " + numberOfCommands + " commands");

		Vector commands = new Vector(numberOfCommands);

		for( int i = 0; i < numberOfCommands; i++ )
		{
			System.out.println("Processing command " + i);
			PropBagEx comBag = bag.getSubtree("command[" + i + ']');
			if( commandEnabled(comBag) )
			{
				Command c = commandFactory(comBag);
				if( c != null )
				{
					commands.add(c);
				}
			}
		}
		return commands;
	}

	protected Command processFailureCommand(PropBagEx bag) throws InstallerException
	{
		PropBagEx failBag = bag.getSubtree("failure");
		if( failBag == null )
		{
			return null;
		}
		else
		{
			return createForeign(failBag);
		}
	}

	protected boolean commandEnabled(PropBagEx bag) throws InstallerException
	{
		String enabled = bag.getNode("@enabled");

		if( enabled.length() == 0 )
		{
			return true;
		}
		else
		{
			try
			{
				enabled = resolver.resolve(enabled);
			}
			catch( ResolverException e )
			{
				throw new InstallerException(e);
			}
			return enabled.equals("true") || enabled.equals("!false");
		}
	}

	protected Command commandFactory(PropBagEx bag) throws InstallerException
	{
		Command command = null;

		String platform = bag.getNode("@platform").toLowerCase();
		if( platform.length() == 0 || platform.indexOf(thisPlatform) != -1 )
		{
			String cclass = bag.getNode("@class");
			if( cclass.equals("delete") )
			{
				command = new CDelete(getURI(bag, "source"));
			}
			else if( cclass.equals("move") )
			{
				command = createMove(bag);
			}
			else if( cclass.equals("copy") )
			{
				command = createCopy(bag);
			}
			else if( cclass.equals("zip-extract") )
			{
				command = createZipExtract(bag);
			}
			else if( cclass.equals("resolver") )
			{
				command = createResolver(bag);
			}
			else if( cclass.equals("execute") )
			{
				command = createExecute(bag);
			}
			else if( cclass.equals("mkexec") )
			{
				command = createMkExec(bag);
			}
			else if( cclass.equals("foreign") )
			{
				command = createForeign(bag);
			}
			else
			{
				throw new InstallerException("Command Error: Unknown command class '" + cclass + "'");
			}

			if( bag.isNodeFalse("@mandatory") )
			{
				command.setMandatory(false);
			}
		}

		return command;
	}

	protected CMkExec createMkExec(PropBagEx bag) throws InstallerException
	{
		String file = getURI(bag, "file");
		return new CMkExec(file);
	}

	protected CMove createMove(PropBagEx bag) throws InstallerException
	{
		String source = getURI(bag, "source");
		String destination = getURI(bag, "destination");
		return new CMove(source, destination, getForce(bag));
	}

	protected CCopy createCopy(PropBagEx bag) throws InstallerException
	{
		String source = getURI(bag, "source");
		String destination = getURI(bag, "destination");
		return new CCopy(source, destination, getForce(bag));
	}

	protected CZipExtract createZipExtract(PropBagEx bag) throws InstallerException
	{
		String source = getURI(bag, "source");
		String destination = getURI(bag, "destination");

		if( !bag.nodeExists("pattern") )
		{
			return new CZipExtract(source, destination);
		}
		else
		{
			return new CZipExtract(source, destination, bag.getNode("pattern/@regex"));
		}
	}

	protected CResolver createResolver(PropBagEx bag) throws InstallerException
	{
		String source = getURI(bag, "source");
		String destination = getURI(bag, "destination");

		String holder = bag.getNode("pattern/@resolver");
		if( holder.length() == 0 )
		{
			holder = "${ }";
		}
		return new CResolver(resultBag, holder, source, destination);
	}

	protected CExecute createExecute(PropBagEx bag) throws InstallerException
	{
		String timeout = bag.getNode("timeout/@value");
		String env = bag.getNode("env");
		String cwd = bag.getNode("cwd");
		String exec = bag.getNode("exec");

		int pause = 0;
		if( timeout.length() > 0 )
		{
			pause = Integer.valueOf(timeout).intValue();
		}

		try
		{
			env = resolver.resolve(env);
			cwd = resolver.resolve(cwd);
			exec = resolver.resolve(exec);
		}
		catch( ResolverException e )
		{
			throw new InstallerException(e);
		}

		if( env.length() == 0 )
		{
			env = null;
		}

		if( cwd.length() == 0 )
		{
			cwd = null;
		}

		return new CExecute(exec, cwd, env, pause);
	}

	protected ForeignCommand createForeign(PropBagEx bag) throws InstallerException
	{
		Class[] argumentTypes = new Class[]{bag.getClass(), resultBag.getClass()};
		Object[] arguments = new Object[]{bag, resultBag};

		String name = bag.getNode("class/@name");
		try
		{
			Class foreignClass = Class.forName(name);
			Constructor constructor = foreignClass.getConstructor(argumentTypes);

			return (ForeignCommand) constructor.newInstance(arguments);
		}
		catch( Exception e )
		{
			throw new InstallerException(e);
		}
	}

	protected boolean getForce(PropBagEx bag)
	{
		return bag.isNodeTrue("force/@value");
	}

	protected String getURI(PropBagEx bag, String node) throws InstallerException
	{
		String uri = bag.getNode(node + "/@uri");
		try
		{
			return resolver.resolve(uri);
		}
		catch( ResolverException e )
		{
			throw new InstallerException(e);
		}
	}
}
