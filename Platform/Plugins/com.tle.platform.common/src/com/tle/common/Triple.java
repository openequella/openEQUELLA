/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.common;

public class Triple<FIRST, SECOND, THIRD> extends Pair<FIRST, SECOND>
{
	private static final long serialVersionUID = 1;

	private THIRD third;

	public Triple()
	{
		super();
	}

	public Triple(FIRST first, SECOND second, THIRD third)
	{
		super(first, second);
		this.third = third;
	}

	public THIRD getThird()
	{
		return third;
	}

	public void setThird(THIRD third)
	{
		this.third = third;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode() + third.hashCode();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean checkFields(Pair<FIRST, SECOND> rhs)
	{
		Triple<FIRST, SECOND, THIRD> t = (Triple<FIRST, SECOND, THIRD>) rhs;
		return super.checkFields(t) && Check.bothNullOrEqual(t.getThird(), getThird());
	}
}
