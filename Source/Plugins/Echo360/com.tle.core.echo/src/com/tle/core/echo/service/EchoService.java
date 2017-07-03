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

package com.tle.core.echo.service;

import com.tle.common.beans.exception.InvalidDataException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.core.echo.entity.EchoServer;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.service.AbstractEntityService;

public interface EchoService extends AbstractEntityService<EntityEditingBean, EchoServer>
{
	EchoServer getForEdit(String uuid);

	String addEchoServer(EchoServer es) throws InvalidDataException;

	void editEchoServer(String uuid, EchoServer es) throws InvalidDataException;

	String getAuthenticatedUrl(String esn, String redirectUrl);

	ObjectMapper getMapper();
}
