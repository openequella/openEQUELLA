package com.tle.core.payment.service.session;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.EntityPack;
import com.tle.common.payment.entity.Catalogue;
import com.tle.core.payment.service.session.CatalogueEditingSession.CatalogueEditingBean;
import com.tle.core.services.entity.EntityEditingBean;
import com.tle.core.services.entity.impl.EntityEditingSessionImpl;

public class CatalogueEditingSession extends EntityEditingSessionImpl<CatalogueEditingBean, Catalogue>
{
	private static final long serialVersionUID = 1L;

	public CatalogueEditingSession(String sessionId, EntityPack<Catalogue> pack, CatalogueEditingBean bean)
	{
		super(sessionId, pack, bean);
	}

	public static class CatalogueEditingBean extends EntityEditingBean
	{
		private static final long serialVersionUID = 1L;

		private boolean regionFiltered;
		private final List<Long> regions = Lists.newArrayList();
		private Long dynamicCollection;
		private String manageCatalogueExpression;

		public boolean isRegionFiltered()
		{
			return regionFiltered;
		}

		public void setRegionFiltered(boolean regionFiltered)
		{
			this.regionFiltered = regionFiltered;
		}

		public List<Long> getRegions()
		{
			return regions;
		}

		public Long getDynamicCollection()
		{
			return dynamicCollection;
		}

		public void setDynamicCollection(Long dynamicCollection)
		{
			this.dynamicCollection = dynamicCollection;
		}

		public String getManageCatalogueExpression()
		{
			return manageCatalogueExpression;
		}

		public void setManageCatalogueExpression(String manageCatalogueExpression)
		{
			this.manageCatalogueExpression = manageCatalogueExpression;
		}
	}
}
