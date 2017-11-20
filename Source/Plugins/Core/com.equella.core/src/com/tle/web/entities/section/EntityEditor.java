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

package com.tle.web.entities.section;

import com.tle.beans.entity.BaseEntity;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;

public interface EntityEditor<B extends EntityEditingBean, E extends BaseEntity>
{
	B getEditedEntity(SectionInfo info);

	void create(SectionInfo info);

	void edit(SectionInfo info, String entUuid, boolean clone);

	SectionRenderable renderEditor(RenderContext context);

	SectionRenderable renderHelp(RenderContext context);

	boolean save(SectionInfo info);

	void cancel(SectionInfo info);

	<S extends EntityEditingSession<B, E>> S saveToSession(SectionInfo info);

	<S extends EntityEditingSession<B, E>> S loadFromSession(SectionInfo info);

	void register(SectionTree tree, String parentId);
}
