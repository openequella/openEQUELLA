/*
 * Copyright 2019 Apereo
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

package com.tle.common.security;

public enum Privilege
{
	CREATE_ITEM,
	EDIT_ITEM,
	VIEW_ITEM,
	CLONE_ITEM,
	MOVE_ITEM,
	COMMENT_CREATE_ITEM,
	COMMENT_DELETE_ITEM,
	COMMENT_VIEW_ITEM,
	DISCOVER_ITEM,
	ARCHIVE_ITEM,
	MANAGE_WORKFLOW,
	VIEW_ACTIVATION_ITEM,
	VIEW_VIEWCOUNT,
}
