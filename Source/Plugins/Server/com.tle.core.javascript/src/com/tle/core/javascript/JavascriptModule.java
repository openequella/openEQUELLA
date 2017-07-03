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

package com.tle.core.javascript;

import java.io.Serializable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * The JavascriptLibrary is the top level (e.g. JQuery), the JavascriptModule is
 * a subset of the library (e.g. JQuery UI)
 * 
 * @author aholland
 */
@NonNullByDefault
public interface JavascriptModule extends Serializable
{
	String getId();

	String getDisplayName();

	/**
	 * Don't assume that you won't get a null back from this. You may very well
	 * get one.
	 * 
	 * @return Usually (ok, always) a sections PreRenderable. Obviously a core
	 *         plugin cannot reference a web one though.
	 */
	@Nullable
	Object getPreRenderer();
}
