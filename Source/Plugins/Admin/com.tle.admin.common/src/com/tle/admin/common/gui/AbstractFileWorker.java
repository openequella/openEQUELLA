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

package com.tle.admin.common.gui;

import java.io.File;

import javax.swing.JComponent;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.common.applet.client.FileWorker;

/*
 * @author aholland
 */
public abstract class AbstractFileWorker<T> extends GlassSwingWorker<T> implements FileWorker
{
	protected final String finishedMessage;
	protected final String errorMessage;
	protected JComponent parent;
	protected File file;

	public AbstractFileWorker(String finishedMessage, String errorMessage)
	{
		this.finishedMessage = finishedMessage;
		this.errorMessage = errorMessage;
	}

	@Override
	public void finished()
	{
		Driver.displayInformation(parent, finishedMessage);
	}

	@Override
	public void exception()
	{
		Driver.displayError(parent, errorMessage, getException());
	}

	public void setComponent(JComponent parent)
	{
		this.parent = parent;
	}

	@Override
	public void setFile(File file)
	{
		this.file = file;
	}
}
