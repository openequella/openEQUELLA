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

package com.tle.core.settings.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tle.web.api.newuitheme.impl.NewUITheme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ThemeSettingsService {

	NewUITheme getTheme() throws IOException;
	InputStream getCustomLogo() throws IOException;

	void setTheme(NewUITheme theme) throws JsonProcessingException;
	void setLogo(File logoFile) throws IOException;

	boolean isCustomLogo();

	void deleteLogo();
}
