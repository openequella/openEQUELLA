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

package com.tle.core.activation;

@SuppressWarnings("nls")
public final class ActivationConstants
{
	public static final String VIEW_ACTIVATION_ITEM = "VIEW_ACTIVATION_ITEM";
	public static final String VIEW_ACTIVATION_ITEM_PFX = "ACLVA-";
	public static final String DEACTIVATE_ACTIVATION_ITEM = "DEACTIVATE_ACTIVATION_ITEM";
	public static final String DELETE_ACTIVATION_ITEM = "DELETE_ACTIVATION_ITEM";
	public static final String DELETE_ACTIVATION_ITEM_PFX = "ACLDA-";
	public static final String COPYRIGHT_ITEM = "COPYRIGHT_ITEM";
	public static final String VIEW_INACTIVE_PORTIONS = "VIEW_INACTIVE_PORTIONS";
	public static final String VIEW_LINKED_PORTIONS = "VIEW_LINKED_PORTIONS";
	public static final String ACTIVATION_INDEX_ID = "activation";
	public static final String COPYRIGHT_OVERRIDE = "COPYRIGHT_OVERRIDE";
	public static final String EDIT_ACTIVATION_ITEM = "EDIT_ACTIVATION_ITEM";
	public static final String AUTO_CREATE_COURSE = "AUTO_CREATE_COURSE";

	private ActivationConstants()
	{
		throw new Error();
	}
}