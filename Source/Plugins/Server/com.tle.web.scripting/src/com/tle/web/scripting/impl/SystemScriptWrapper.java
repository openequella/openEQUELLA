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

package com.tle.web.scripting.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.scripting.objects.SystemScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.ExecutionResultScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.web.scripting.impl.AttachmentsScriptWrapper.AttachmentScriptTypeImpl;
import com.tle.web.scripting.impl.FileScriptingObjectImpl.FileHandleScriptTypeImpl;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class SystemScriptWrapper implements SystemScriptObject
{
	@Inject
	private FileSystemService fileSystem;

	// @Override
	// public BinaryDataScriptType executeWithBinaryResult(String programPath,
	// String[] parameters)
	// {
	// final ExecResult res = ExecUtils.exec(getCommand(programPath,
	// parameters));
	//
	// return null;
	// }

	@Override
	public ExecutionResultScriptType execute(String programPath, Object[] parameters)
	{
		return new ExecutionResultTypeImpl(ExecUtils.exec(getCommand(programPath, parameters)));
	}

	@Override
	public void executeInBackground(String programPath, Object[] parameters)
	{
		final String[] cmd = getCommand(programPath, parameters);
		new Thread("SystemScriptWrapper execution thread")
		{
			@Override
			public void run()
			{
				ExecUtils.exec(cmd);
			}
		}.start();
	}

	private String[] getCommand(String programPath, Object[] parameters)
	{
		final String[] cmd = new String[parameters.length + 1];
		cmd[0] = programPath;
		// System.arraycopy(parameters, 0, cmd, 1, parameters.length);
		for( int i = 0; i < parameters.length; i++ )
		{
			Object param = parameters[i];
			// munge the param
			String strParam = "";
			if( param instanceof String )
			{
				strParam = (String) param;
			}
			else if( param instanceof Number )
			{
				strParam = Integer.toString(((Number) param).intValue());
			}
			else if( param instanceof AttachmentScriptType )
			{
				AttachmentScriptTypeImpl attachmentType = ((AttachmentScriptTypeImpl) param);
				strParam = fileSystem.getExternalFile(attachmentType.getStagingFile(), attachmentType.getUrl())
					.getAbsolutePath();
			}
			else if( param instanceof FileHandleScriptType )
			{
				FileHandleScriptTypeImpl fileType = ((FileHandleScriptTypeImpl) param);
				strParam = fileSystem.getExternalFile(fileType.getHandle(), fileType.getFilepath()).getAbsolutePath();
			}

			cmd[i + 1] = strParam;
		}
		return cmd;
	}

	public static class ExecutionResultTypeImpl implements ExecutionResultScriptType
	{
		private final ExecResult execResult;

		public ExecutionResultTypeImpl(ExecResult execResult)
		{
			this.execResult = execResult;
		}

		@Override
		public int getCode()
		{
			return execResult.getExitStatus();
		}

		@Override
		public String getErrorOutput()
		{
			return execResult.getStderr();
		}

		@Override
		public String getStandardOutput()
		{
			return execResult.getStdout();
		}
	}
}
