package com.tle.beans.item;

import java.util.Date;
import java.util.List;

import com.tle.annotation.NonNull;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.IItemNavigationNode;
import com.tle.beans.item.attachments.INavigationSettings;
import com.tle.common.util.UnmodifiableIterable;

public interface IItem<A extends IAttachment>
{
	@NonNull
	String getUuid();

	int getVersion();

	LanguageBundle getName();

	LanguageBundle getDescription();

	List<A> getAttachments();

	UnmodifiableIterable<A> getAttachmentsUnmodifiable();

	@NonNull
	ItemId getItemId();

	List<? extends IItemNavigationNode> getTreeNodes();

	<N extends INavigationSettings> N getNavigationSettings();

	float getRating();

	Date getDateCreated();

	Date getDateModified();
}
