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

package com.tle.web.sections.equella.impl;

import com.tle.web.errors.DefaultErrorSection;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.template.Decorations;

public class ModalErrorSection extends DefaultErrorSection
{
	@Override
	public String getDefaultPropertyName()
	{
		return "moderr"; //$NON-NLS-1$
	}

	@Override
	public SectionResult renderErrorHtml(DefaultErrorModel model, RenderEventContext context) throws Exception
	{
		Decorations.getDecorations(context).clearAllDecorations();
		return super.renderErrorHtml(model, context);
	}
}
