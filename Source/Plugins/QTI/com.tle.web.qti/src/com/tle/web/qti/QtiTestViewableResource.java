package com.tle.web.qti;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;

import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.FileSizeUtils;
import com.tle.common.Utils;
import com.tle.core.qti.QtiConstants;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

public class QtiTestViewableResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(QtiTestViewableResource.class);
	}

	@PlugKey("details.type")
	private static Label TYPE;
	@PlugKey("details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("details.name")
	private static Label NAME;
	@PlugKey("details.size")
	private static Label SIZE;
	@PlugKey("details.tool.name")
	private static Label TOOL_NAME;
	@PlugKey("details.tool.version")
	private static Label TOOL_VERSION;
	@PlugKey("details.timelimit.max")
	private static Label TIME_LIMIT_MAX;
	@PlugKey("details.questions.count")
	private static Label QUESTION_COUNT;
	@PlugKey("details.sections.count")
	private static Label SECTION_COUNT;
	@PlugKey("details.navigationmode")
	private static Label NAVIGATION_MODE;

	@PlugKey("details.value.yes")
	private static Label YES;
	@PlugKey("details.value.no")
	private static Label NO;
	// NAVIGATION_MODE_LINEAR;
	// NAVIGATION_MODE_NONLINEAR
	@PlugKey("notimelimit")
	private static Label NO_TIME_LIMIT_MAX;

	private final CustomAttachment qtiAttachment;

	// private final QTIService qtiService;

	public QtiTestViewableResource(ViewableResource resource, CustomAttachment attachment)
	{
		super(resource);
		this.qtiAttachment = attachment;
		// this.qtiService = qtiService;
	}

	@Override
	public ViewAuditEntry getViewAuditEntry()
	{
		return new ViewAuditEntry("qtitest", qtiAttachment.getUrl());
	}

	@Override
	public String getMimeType()
	{
		return "equella/qtitest";
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

		commonDetails.add(makeDetail(TYPE, MIMETYPE));
		commonDetails.add(makeDetail(NAME, new TextLabel(qtiAttachment.getDescription())));

		Long size = (Long) qtiAttachment.getData(QtiConstants.KEY_FILE_SIZE);
		if( size != null )
		{
			commonDetails.add(makeDetail(SIZE, new TextLabel(FileSizeUtils.humanReadableFileSize(size))));
		}

		// FIXME: sprint 2, read this from the DB objects
		addStringIfPresent(qtiAttachment, commonDetails, TOOL_NAME, QtiConstants.KEY_TOOL_NAME);
		addStringIfPresent(qtiAttachment, commonDetails, TOOL_VERSION, QtiConstants.KEY_TOOL_VERSION);
		addTimeLimit(commonDetails, TIME_LIMIT_MAX, QtiConstants.KEY_MAX_TIME);
		addNumberIfPresent(qtiAttachment, commonDetails, QUESTION_COUNT, QtiConstants.KEY_QUESTION_COUNT);
		// addNumberIfPresent(qtiAttachment, commonDetails, MAX_ATTEMPTS,
		// QtiConstants.KEY_MAX_ATTEMPTS);
		addNumberIfPresent(qtiAttachment, commonDetails, SECTION_COUNT, QtiConstants.KEY_SECTION_COUNT);

		// addBooleanIfPresent(qtiAttachment, commonDetails, ALLOW_REVIEW,
		// QtiConstants.KEY_ALLOW_REVIEW);
		// addBooleanIfPresent(qtiAttachment, commonDetails, ALLOW_SKIPPING,
		// QTIConstants.KEY_ALLOW_SKIPPING);

		final String navigationMode = (String) qtiAttachment.getData(QtiConstants.KEY_NAVIGATION_MODE);
		if( navigationMode != null )
		{
			NavigationMode nm = NavigationMode.parseNavigationMode(navigationMode);
			commonDetails.add(makeDetail(NAVIGATION_MODE, nm == NavigationMode.NONLINEAR ? YES : NO));
		}

		return commonDetails;
	}

	private void addStringIfPresent(CustomAttachment attachment, List<AttachmentDetail> commonDetails, Label label,
		String key)
	{
		final String value = (String) attachment.getData(key);
		if( value != null )
		{
			commonDetails.add(makeDetail(label, new TextLabel(value)));
		}
	}

	private void addTimeLimit(List<AttachmentDetail> commonDetails, Label label, String key)
	{
		final Long value = (Long) qtiAttachment.getData(key);
		if( value != null )
		{
			final long seconds = value / 1000;
			Label maxTimeLabel;
			if( seconds != 0 )
			{
				maxTimeLabel = new TextLabel(Utils.formatDuration(seconds));
			}
			else
			{
				maxTimeLabel = NO_TIME_LIMIT_MAX;
			}
			commonDetails.add(makeDetail(label, maxTimeLabel));
		}
	}

	private void addNumberIfPresent(CustomAttachment attachment, List<AttachmentDetail> commonDetails, Label label,
		String key)
	{
		final Number value = (Number) attachment.getData(key);
		if( value != null )
		{
			commonDetails.add(makeDetail(label, new NumberLabel(value)));
		}
	}
}