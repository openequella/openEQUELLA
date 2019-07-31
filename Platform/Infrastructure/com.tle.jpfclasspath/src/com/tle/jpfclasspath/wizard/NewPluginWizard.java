package com.tle.jpfclasspath.wizard;

import com.tle.jpfclasspath.JPFClasspathPlugin;
import com.tle.jpfclasspath.JPFProjectNature;
import com.tle.jpfclasspath.model.JPFProject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

@SuppressWarnings("restriction")
public class NewPluginWizard extends NewElementWizard {
  private NewJPFPluginWizardPageOne fFirstPage;
  private NewJavaProjectWizardPageTwo fSecondPage;
  private XMLOutputter xmlOut;

  public NewPluginWizard() {
    fFirstPage = new NewJPFPluginWizardPageOne();
    fSecondPage = new NewJavaProjectWizardPageTwo(fFirstPage);
    Format format = Format.getPrettyFormat();
    format.setIndent("\t");
    format.setOmitEncoding(true);
    format.setOmitDeclaration(true);
    format.setLineSeparator("\n");
    format.setEncoding("UTF-8");
    xmlOut = new XMLOutputter(format);
  }

  @Override
  public void addPages() {
    addPage(fFirstPage);
    addPage(fSecondPage);

    fFirstPage.init(getSelection(), getActivePart());
  }

  private IWorkbenchPart getActivePart() {
    IWorkbenchWindow activeWindow = getWorkbench().getActiveWorkbenchWindow();
    if (activeWindow != null) {
      IWorkbenchPage activePage = activeWindow.getActivePage();
      if (activePage != null) {
        return activePage.getActivePart();
      }
    }
    return null;
  }

  @Override
  protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
    fSecondPage.performFinish(monitor);
    IProject project = fSecondPage.getJavaProject().getProject();
    JPFProject.getPreferences(project)
        .put(JPFClasspathPlugin.PREF_REGISTRY_NAME, fFirstPage.getSelectedRegistry().getName());
    IProjectDescription description = project.getDescription();
    writePluginJpf(monitor, project);
    JPFProjectNature.addNature(description);
    project.setDescription(description, SubMonitor.convert(monitor, "Add JPF Project Nature", 1));
  }

  @SuppressWarnings("nls")
  private void writePluginJpf(IProgressMonitor monitor, IProject project) throws CoreException {
    Document doc = new Document();
    doc.setDocType(
        new DocType(
            "plugin",
            "-//JPF//Java Plug-in Manifest 1.0",
            "http://jpf.sourceforge.net/plugin_1_0.dtd"));
    Element rootElem = new Element("plugin");
    doc.setRootElement(rootElem);
    rootElem.setAttribute("id", project.getName());
    rootElem.setAttribute("version", "1");
    Element requires = new Element("requires");
    rootElem.addContent(requires);
    Element runtime = new Element("runtime");
    Element srcLib = new Element("library");
    srcLib.setAttribute("type", "code");
    srcLib.setAttribute("path", "classes/");
    srcLib.setAttribute("id", "classes");
    Element export = new Element("export");
    export.setAttribute("prefix", "*");
    srcLib.addContent(export);
    runtime.addContent(srcLib);
    rootElem.addContent(runtime);
    fFirstPage.customizeManifest(rootElem, project, monitor);
    if (requires.getContentSize() == 0) {
      rootElem.removeContent(requires);
    }

    IFile manifest = JPFProject.getManifest(project);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      xmlOut.output(doc, baos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    manifest.create(new ByteArrayInputStream(baos.toByteArray()), true, monitor);
  }

  @Override
  public boolean performFinish() {
    boolean res = super.performFinish();
    if (res) {
      final IJavaElement newElement = getCreatedElement();

      IWorkingSet[] workingSets = fFirstPage.getWorkingSets();
      if (workingSets.length > 0) {
        PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(newElement, workingSets);
      }
      BasicNewResourceWizard.selectAndReveal(
          fSecondPage.getJavaProject().getProject(), getWorkbench().getActiveWorkbenchWindow());
    }
    return res;
  }

  @Override
  public IJavaElement getCreatedElement() {
    return fSecondPage.getJavaProject();
  }
}
