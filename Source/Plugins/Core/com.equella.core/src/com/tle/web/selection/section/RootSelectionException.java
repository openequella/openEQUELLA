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

package com.tle.web.selection.section;

/**
 * Simple wrapper for RuntimeException that allows a particular Handler
 * configured in the plugin to be directed to places where this particular
 * exception is thrown. It is a potential weakness of the default section-error
 * handlers that if an error originates in the root, the handler will endlessly
 * attempt to rebuild the root (and presumably re-cause and re-throw the error).
 * See Redmine #7607
 * 
 * @author larry
 */
public class RootSelectionException extends RuntimeException
{
	public RootSelectionException(Throwable thrown)
	{
		super(thrown);
	}
}
