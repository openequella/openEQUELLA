package com.tle.jpfclasspath.wizard;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.jdom.Element;

import com.tle.jpfclasspath.JPFClasspathPlugin;
import com.tle.jpfclasspath.model.JPFProject;
import com.tle.jpfclasspath.properties.JarProjectContentProvider;

@SuppressWarnings({ "nls", "restriction" })
public class NewJPFPluginWizardPageOne extends NewJavaProjectWizardPageOne
{
	private EQUELLAGroup equella;
	private RegistryGroup registry;

	public NewJPFPluginWizardPageOne()
	{
		setTitle("Create new JPF Plugin project");
		equella = new EQUELLAGroup();
		registry = new RegistryGroup();
	}

	@Override
	public void createControl(Composite parent)
	{
		initializeDialogUnits(parent);

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		Control nameControl = createNameControl(composite);
		nameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control locationControl = createLocationControl(composite);
		locationControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));


		Control registryControl = registry.createContent(composite);
		registryControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control equellaControl = equella.createContent(composite);
		equellaControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control workingSetControl = createWorkingSetControl(composite);
		workingSetControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control infoControl = createInfoControl(composite);
		infoControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(composite);
	}

	private GridLayout initGridLayout(GridLayout layout, boolean margins)
	{
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		if( margins )
		{
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		}
		else
		{
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}

	@Override
	public IClasspathEntry[] getDefaultClasspathEntries()
	{
		List<IClasspathEntry> entries = new ArrayList<>();
		entries.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
		entries.add(JavaCore.newContainerEntry(JPFClasspathPlugin.CONTAINER_PATH));
		return entries.toArray(new IClasspathEntry[entries.size()]);
	}

	private final class RegistryGroup implements IListAdapter<IProject>
	{
		private Group fGroup;
		private final ListDialogField<IProject> fProject = new ListDialogField<>(this, null,
			WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
		private IProject selected;

		public Control createContent(Composite composite)
		{
			fGroup = new Group(composite, SWT.NONE);
			fGroup.setFont(composite.getFont());
			fGroup.setLayout(initGridLayout(new GridLayout(3, false), true));
			fGroup.setText("Registry Project");

			List<IProject> projects = new ArrayList<>();
			Object[] children = new JarProjectContentProvider(null).getChildren(ResourcesPlugin.getWorkspace());
			for( Object object : children )
			{
				projects.add((IProject) object);
			}
			fProject.setElements(projects);
			fProject.selectFirstElement();
			fProject.doFillIntoGrid(fGroup, 3);
			return fGroup;
		}

		public IProject getSelectedRegistry()
		{
			return selected;
		}

		@Override
		public void customButtonPressed(ListDialogField<IProject> field, int index)
		{
			// nothing
		}

		@Override
		public void selectionChanged(ListDialogField<IProject> field)
		{
			List<IProject> elements = fProject.getSelectedElements();
			if( elements.isEmpty() )
			{
				selected = null;
			}
			else
			{
				selected = elements.get(0);
			}
		}

		@Override
		public void doubleClicked(ListDialogField<IProject> field)
		{
			// nothing
		}
	}

	private final class EQUELLAGroup
	{
		private final SelectionButtonDialogField fLanguage = new SelectionButtonDialogField(SWT.CHECK);
		private final SelectionButtonDialogField fGuice = new SelectionButtonDialogField(SWT.CHECK);

		private Group fGroup;

		public Control createContent(Composite composite)
		{
			fGroup = new Group(composite, SWT.NONE);
			fGroup.setFont(composite.getFont());
			fGroup.setLayout(initGridLayout(new GridLayout(3, false), true));
			fGroup.setText("Options");

			fGuice.setLabelText("Guice module");
			fGuice.doFillIntoGrid(fGroup, 1);

			fLanguage.setLabelText("Language strings");
			fLanguage.doFillIntoGrid(fGroup, 1);
			return fGroup;
		}

		public boolean isGuiceModule()
		{
			return fGuice.isSelected();
		}

		public boolean isLanguageStrings()
		{
			return fLanguage.isSelected();
		}
	}

	public IProject getSelectedRegistry()
	{
		return registry.getSelectedRegistry();
	}

	public void customizeManifest(Element rootElem, IProject project, IProgressMonitor monitor) throws CoreException
	{
		if( equella.isLanguageStrings() )
		{
			Element runtime = rootElem.getChild("runtime");
			addLibrary(runtime, "resources", "resources/", "resources");
			JPFProject.getResourcesFolder(project).create(true, true, monitor);
		}
		Element requires = rootElem.getChild("requires");
		if( equella.isGuiceModule() )
		{
			addImport(requires, "com.tle.core.guice");
			addExtension(rootElem, "com.tle.core.guice", "module", "guiceModules");
		}
		if( equella.isLanguageStrings() )
		{
			addImport(requires, "com.tle.common.i18n");
			Element ext = addExtension(rootElem, "com.tle.common.i18n", "bundle", "strings");
			addParameter(ext, "file", "lang/i18n.properties");
			IFolder langFolder = JPFProject.getResourcesFolder(project).getFolder("lang");
			langFolder.create(true, true, monitor);
			langFolder.getFile("i18n.properties").create(new ByteArrayInputStream("# add your strings".getBytes()),
				true, monitor);
		}

	}

	private void addLibrary(Element runtime, String type, String path, String id)
	{
		Element lib = new Element("library");
		lib.setAttribute("type", type);
		lib.setAttribute("path", path);
		lib.setAttribute("id", id);
		runtime.addContent(lib);
	}

	private void addParameter(Element ext, String name, String value)
	{
		Element param = new Element("parameter");
		param.setAttribute("id", name);
		param.setAttribute("value", value);
		ext.addContent(param);
	}

	private Element addExtension(Element rootElem, String pluginId, String pointId, String id)
	{
		Element extension = new Element("extension");
		extension.setAttribute("plugin-id", pluginId);
		extension.setAttribute("point-id", pointId);
		extension.setAttribute("id", id);
		rootElem.addContent(extension);
		return extension;
	}

	private void addImport(Element requires, String pluginId)
	{
		Element importTag = new Element("import");
		importTag.setAttribute("plugin-id", pluginId);
		requires.addContent(importTag);
	}

}
