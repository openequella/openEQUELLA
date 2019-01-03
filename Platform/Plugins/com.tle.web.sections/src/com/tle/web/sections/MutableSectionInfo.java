/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
