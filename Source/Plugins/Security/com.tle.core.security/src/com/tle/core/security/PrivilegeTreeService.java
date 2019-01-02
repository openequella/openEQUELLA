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

package com.tle.core.security;

import com.tle.common.security.remoting.RemotePrivilegeTreeService;

/**
 * Eventually this service should replace the PrivilegeTree class, sans the
 * static declarations to hard-coded classes and privileges.
 * 
 * @author nick
 */
public interface PrivilegeTreeService extends RemotePrivilegeTreeService
{
	// Nothing to declare
}
