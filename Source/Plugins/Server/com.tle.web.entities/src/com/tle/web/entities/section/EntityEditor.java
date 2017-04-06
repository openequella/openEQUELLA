package com.tle.web.entities.section;

import com.tle.beans.entity.BaseEntity;
import com.tle.core.services.entity.EntityEditingBean;
import com.tle.core.services.entity.EntityEditingSession;
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
