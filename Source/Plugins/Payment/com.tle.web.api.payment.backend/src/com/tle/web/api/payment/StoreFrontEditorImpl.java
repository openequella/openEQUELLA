package com.tle.web.api.payment;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.guice.BindFactory;
import com.tle.core.payment.service.StoreFrontService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.payment.beans.StoreFrontBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public class StoreFrontEditorImpl extends AbstractBaseEntityEditor<StoreFront, StoreFrontBean>
	implements
		StoreFrontEditor
{
	@Inject
	private StoreFrontService storeFrontService;

	@AssistedInject
	public StoreFrontEditorImpl(@Assisted StoreFront entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("lockId") @Nullable String lockId, @Assisted("editing") boolean editing,
		@Assisted("importing") boolean importing)
	{
		super(entity, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public StoreFrontEditorImpl(@Assisted StoreFront entity, @Assisted("stagingUuid") @Nullable String stagingUuid,
		@Assisted("importing") boolean importing)
	{
		this(entity, stagingUuid, null, false, importing);
	}

	@Override
	protected AbstractEntityService<?, StoreFront> getEntityService()
	{
		return storeFrontService;
	}

	@BindFactory
	public interface StoreFrontEditorFactory
	{
		StoreFrontEditorImpl createExistingEditor(StoreFront storeFront,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		StoreFrontEditorImpl createNewEditor(StoreFront storeFront,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing);
	}
}
