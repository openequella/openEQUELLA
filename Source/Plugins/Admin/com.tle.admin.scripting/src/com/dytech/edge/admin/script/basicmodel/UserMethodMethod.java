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

package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.Equals;
import com.dytech.edge.admin.script.ifmodel.NotEquals;

public abstract class UserMethodMethod
{
	public abstract String toScript();

	public abstract Equality getOp();

	public String toEasyRead()
	{
		return getOp().toEasyRead();
	}

	public static class HasRole extends UserMethodMethod
	{
		@Override
		public String toScript()
		{
			return "hasRole";
		}

		@Override
		public Equality getOp()
		{
			return new Equals();
		}
	}

	public static class DoesntHasRole extends UserMethodMethod
	{
		@Override
		public String toScript()
		{
			return "doesntHaveRole";
		}

		@Override
		public Equality getOp()
		{
			return new NotEquals();
		}
	}
}
