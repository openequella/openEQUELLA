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

package com.tle.web.freemarker;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;

@NonNullByDefault
public interface FreemarkerFactory
{
	SectionRenderable createResult(String template, SectionId sectionId);

	SectionRenderable createResult(String template, SectionId sectionId, PreRenderable preRenderer);

	SectionRenderable createResultWithModel(String template, Object model);

	SectionRenderable createResultWithModelMap(String template, Object... nameValues);

	NamedSectionResult createNamedResult(String name, String template, SectionId sectionId);

	TemplateResult createTemplateResult(String template, SectionId sectionId);

	TemplateResult createTemplateResultWithModel(String template, Object model);
}
