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

package com.tle.common.portal;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class PortletConstants
{
	public static final String PRIV_ADMINISTER_PORTLETS = "ADMINISTER_PORTLETS";
	public static final String CREATE_PORTLET = "CREATE_PORTLET";
	public static final String VIEW_PORTLET = "VIEW_PORTLET";
	public static final String EDIT_PORTLET = "EDIT_PORTLET";
	public static final String DELETE_PORTLET = "DELETE_PORTLET";

	private PortletConstants()
	{
		throw new Error();
	}
}
