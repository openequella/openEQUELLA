package com.tle.jpfclasspath;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class JPFClasspathContainerPage extends WizardPage implements IClasspathContainerPage {
  @SuppressWarnings("nls")
  public JPFClasspathContainerPage() {
    super("JPF Classpath");
    setDescription("JPF Classpath will automatically include JPF plugin dependencies");
    setPageComplete(true);
  }

  @SuppressWarnings("restriction")
  @Override
  public void createControl(Composite parent) {
    Label label = new Label(parent, SWT.NULL);
    label.setText("JPF Classpath");
    setControl(label);
  }

  @Override
  public boolean finish() {
    return true;
  }

  @Override
  public IClasspathEntry getSelection() {
    return JavaCore.newContainerEntry(JPFClasspathPlugin.CONTAINER_PATH);
  }

  @Override
  public void setSelection(IClasspathEntry containerEntry) {
    // nothing to edit
  }
}
