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

package com.tle.web.bulk.metadata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.tle.web.bulk.metadata.model.Modification.ModificationKeys;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.BookmarkEvent;

@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
public class BulkEditMetadataModel
{

	@Bookmarked
	private boolean schemaSelection;
	@Bookmarked
	private boolean actionSelection;
	@Bookmarked
	private long selectedSchema;
	@Bookmarked
	private List<String> selectedNodes;
	@Bookmarked
	private String nodeDisplay;
	@Bookmarked
	private boolean actionReplace;
	@Bookmarked
	private boolean actionAdd;
	@Bookmarked
	private boolean actionSet;
	@Bookmarked
	private List<Modification> modifications = new ArrayList<Modification>();
	@Bookmarked
	private int editIndex;
	@Bookmarked
	private boolean edit;


	public boolean isEdit()
	{
		return edit;
	}

	public void setEdit(boolean edit)
	{
		this.edit = edit;
	}

	public int getEditIndex()
	{
		return editIndex;
	}

	public void setEditIndex(int editIndex)
	{
		this.editIndex = editIndex;
	}

	public void wipe()
	{
		nodeDisplay = "";
		selectedSchema = 0;
		editIndex = -1;
		edit = false;
		setActionNone();
		selectedNodes = Collections.emptyList();
	}

	public List<Modification> getModifications()
	{
		return modifications;
	}

	public void setModifications(List<Modification> mods)
	{
		this.modifications = mods;
	}

	public void addModification(String info, Map<ModificationKeys, String> params)
	{
		modifications.add(new Modification(selectedNodes, info, nodeDisplay, params));
	}

	public void addModification(String info, Map<ModificationKeys, String> params, int index)
	{
		modifications.remove(index);
		modifications.add(index, new Modification(selectedNodes, info, nodeDisplay, params));
	}


	public boolean isActionReplace()
	{
		return actionReplace;
	}

	public boolean isActionAdd()
	{
		return actionAdd;
	}

	public boolean isActionSet()
	{
		return actionSet;
	}

	public void setActionAdd()
	{
		actionAdd = true;
		actionReplace = false;
		actionSet = false;
	}

	public void setActionReplace()
	{
		actionAdd = false;
		actionReplace = true;
		actionSet = false;
	}

	public void setActionSet()
	{
		actionAdd = false;
		actionReplace = false;
		actionSet = true;
	}

	public void setActionNone()
	{
		actionAdd = false;
		actionReplace = false;
		actionSet = false;
	}

	public String getNodeDisplay()
	{
		return nodeDisplay;
	}

	public void setNodeDisplay(String nodeDisplay)
	{
		this.nodeDisplay = nodeDisplay;
	}

	public boolean isSchemaSelection()
	{
		return schemaSelection;
	}

	public void setSchemaSelection(boolean schemaSelection)
	{
		this.schemaSelection = schemaSelection;
	}

	public boolean isActionSelection()
	{
		return actionSelection;
	}

	public void setActionSelection(boolean actionSelection)
	{
		this.actionSelection = actionSelection;
	}

	public long getSelectedSchema()
	{
		return selectedSchema;
	}

	public void setSelectedSchema(long selectedSchema)
	{
		this.selectedSchema = selectedSchema;
	}

	public List<String> getSelectedNodes()
	{
		return selectedNodes;
	}

	public void setSelectedNodes(List<String> selectedNodes)
	{
		this.selectedNodes = selectedNodes;
	}

}
