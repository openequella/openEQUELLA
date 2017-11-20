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
