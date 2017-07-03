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
