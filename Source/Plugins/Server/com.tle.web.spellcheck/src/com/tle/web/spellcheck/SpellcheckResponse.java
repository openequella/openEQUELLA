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

package com.tle.web.spellcheck;

import java.util.List;

public class SpellcheckResponse
{
	private String id;
	private List<String> result;

	public String getId()
	{
		return null;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public List<String> getResult()
	{
		return result;
	}

	public void setResult(List<String> result)
	{
		this.result = result;
	}

	public List<String> getError()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "id: \n" + id + "\n\n results: \n " + getResult().toString() + "\n\n error: \n " + getError();
	}
}
