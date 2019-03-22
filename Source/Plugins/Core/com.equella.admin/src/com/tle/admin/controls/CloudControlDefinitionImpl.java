package com.tle.admin.controls;

import com.dytech.edge.admin.wizard.editor.Editor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.model.CustomControlModel;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.collect.ImmutableSet;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.cloudproviders.CloudControlDefinition;
import java.util.Set;

public class CloudControlDefinitionImpl implements ControlDefinition {

  private final CloudControlDefinition def;

  public CloudControlDefinitionImpl(CloudControlDefinition def) {
    this.def = def;
  }

  @Override
  public EditorFactory editorFactory() {
    return new StandardEditorFactory();
  }

  @Override
  public Set<String> getContexts() {
    return ImmutableSet.of("page");
  }

  @Override
  public String getName() {
    return def.name();
  }

  @Override
  public String getId() {
    return "BLAH";
  }

  @Override
  public boolean hasContext(String context) {
    return false;
  }

  @Override
  public Editor createEditor(Control control, int type, SchemaModel schema) {
    return null;
  }

  @Override
  public String getIcon() {
    return def.iconUrl();
  }

  @Override
  public Control createControlModel() {
    return new CustomControlModel<CustomControl>(this);
  }

  @Override
  public Object createWrappedObject() {

    CustomControl customControl = new CustomControl();
    customControl.setClassType(getId());
    return customControl;
  }
}
