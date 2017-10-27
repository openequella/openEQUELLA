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

package com.tle.core.freetext.index;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.document.Document;

import com.tle.beans.item.ItemIdKey;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.freetext.FreetextIndex;

@Bind
@Singleton
@SuppressWarnings("nls")
public class NormalItemIndex extends ItemIndex<FreetextResult>
{
	@Inject
	private FreetextIndex freetextIndex;

	@PostConstruct
	@Override
	public void afterPropertiesSet() throws IOException
	{
		setIndexPath(new File(freetextIndex.getRootIndexPath(), "index"));
		super.afterPropertiesSet();
	}

	@Override
	protected FreetextResult createResult(ItemIdKey key, Document doc, float relevance, boolean sortByRelevance)
	{
		return new FreetextResult(key, relevance, sortByRelevance);
	}

}
