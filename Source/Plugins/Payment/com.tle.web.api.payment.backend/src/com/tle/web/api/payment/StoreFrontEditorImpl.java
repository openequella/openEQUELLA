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
		@Assisted("lockId") @Nullable String lockId, @Assisted boolean editing)
	{
		super(entity, stagingUuid, lockId, editing);
	}

	@AssistedInject
	public StoreFrontEditorImpl(@Assisted StoreFront entity, @Assisted("stagingUuid") @Nullable String stagingUuid)
	{
		this(entity, stagingUuid, null, false);
	}

	@Override
	protected AbstractEntityService<?, StoreFront> getEntityService()
	{
		return storeFrontService;
	}

	@BindFactory
	public interface StoreFrontEditorFactory
	{
		@Nullable
		StoreFrontEditorImpl createExistingEditor(StoreFront storeFront,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			boolean editing);

		StoreFrontEditorImpl createNewEditor(StoreFront storeFront,
			@Assisted("stagingUuid") @Nullable String stagingUuid);
	}
}
