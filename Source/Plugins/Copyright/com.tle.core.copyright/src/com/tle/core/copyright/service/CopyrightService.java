package com.tle.core.copyright.service;

import java.util.List;
import java.util.Map;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.exception.CopyrightViolationException;

public interface CopyrightService<H extends Holding, P extends Portion, S extends Section>
{
	String getActivationType();

	Item getCopyrightedItem(ItemId item);

	boolean isCopyrightedItem(Item item);

	S getSectionForAttachment(Item item, String attachmentUuid);

	H getHoldingForItem(Item item);

	List<ActivateRequest> getCurrentOrPendingActivations(H holding);

	Attachment getSectionAttachmentForFilepath(Item item, String filepath);

	CopyrightViolationException createViolation(Item item);

	String citate(H holding, P portion);

	Map<Long, H> getHoldingsForItems(List<Item> items);

	Map<Long, List<P>> getPortionsForItems(List<Item> items);

	AgreementStatus getAgreementStatus(Item item, IAttachment attachment);

	void acceptAgreement(Item item, IAttachment attachment);
}
