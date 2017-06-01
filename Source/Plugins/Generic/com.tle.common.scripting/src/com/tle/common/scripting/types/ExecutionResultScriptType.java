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

package com.tle.common.scripting.types;

/**
 * @author Aaron
 */
public interface ExecutionResultScriptType
{
	/**
	 * @return The return code as returned by the external program
	 */
	int getCode();

	/**
	 * @return The error text emitted by the external program
	 */
	String getErrorOutput();

	/**
	 * @return The standard text emitted by the external program
	 */
	String getStandardOutput();
}
