package com.tle.admin.collection.summarydisplay;

import java.awt.Component;

import com.tle.admin.baseentity.EditorState;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.applet.client.ClientService;

public interface SummaryDisplayConfig
{
	void setClientService(ClientService service);

	void setState(EditorState<ItemDefinition> state);

	void load(SummarySectionsConfig sectionElement);

	void save(SummarySectionsConfig sectionElement);

	void setup();

	Component getComponent();

	void setSchemaModel(SchemaModel model);

	boolean hasDetectedChanges();

	void clearChanges();
}
