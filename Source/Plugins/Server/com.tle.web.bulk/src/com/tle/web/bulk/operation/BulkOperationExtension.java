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

package com.tle.web.bulk.operation;

import java.util.Collection;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemPack;
import com.tle.core.plugins.BeanLocator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
public interface BulkOperationExtension
{
	void register(SectionTree tree, String parentId);

	void addOptions(SectionInfo info, List<Option<OperationInfo>> options);

	BeanLocator<? extends BulkOperationExecutor> getExecutor(SectionInfo info, String operationId);

	void prepareDefaultOptions(SectionInfo info, String operationId);

	SectionRenderable renderOptions(RenderContext context, String operationId);

	Label getStatusTitleLabel(SectionInfo info, String operationId);

	boolean areOptionsFinished(SectionInfo info, String operationId);

	boolean hasExtraOptions(SectionInfo info, String operationId);

	boolean hasExtraNavigation(SectionInfo info, String operationId);

	Collection<Button> getExtraNavigation(SectionInfo info, String operationId);

	boolean hasPreview(SectionInfo info, String operationId);

	ItemPack runPreview(SectionInfo info, String operationId, long itemId) throws Exception;

	boolean showPreviousButton(SectionInfo info, String opererationId);

	boolean validateOptions(SectionInfo info, String operationId);

	public static class OperationInfo
	{
		private final BulkOperationExtension op;
		private final String opId;

		public OperationInfo(BulkOperationExtension op, String opId)
		{
			this.op = op;
			this.opId = opId;
		}

		public BulkOperationExtension getOp()
		{
			return op;
		}

		public String getOpId()
		{
			return opId;
		}
	}

}
