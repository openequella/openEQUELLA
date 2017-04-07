package com.tle.web.sections;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.ParametersEvent;

@NonNullByDefault
public interface MutableSectionInfo extends SectionInfo
{
	void setRequest(@Nullable HttpServletRequest request);

	void setResponse(@Nullable HttpServletResponse response);

	void addTree(SectionTree tree);

	void removeTree(SectionTree tree);

	void addTreeToBottom(SectionTree tree, boolean processParams);

	/**
	 * Process the event queue, until it is empty. <br>
	 * Typically this is only called by the {@code SectionsController}
	 */
	void processQueue();

	SectionTree getRootTree();

	void fireBeforeEvents();

	void fireReadyToRespond(boolean redirect);

	List<SectionId> getRootIds();

	List<SectionTree> getTrees();

	/**
	 * Add a parameters event which will be processed by any new trees added
	 * with processParams flag set.
	 * 
	 * @see #addTreeToBottom(SectionTree, boolean)
	 * @param event
	 */
	void addParametersEvent(ParametersEvent event);

}
