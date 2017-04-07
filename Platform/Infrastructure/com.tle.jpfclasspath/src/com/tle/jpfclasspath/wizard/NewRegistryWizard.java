package com.tle.jpfclasspath.wizard;

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

import com.tle.jpfclasspath.JPFJarNature;

@SuppressWarnings("restriction")
public class NewRegistryWizard extends NewElementWizard
{
	private NewJPFRegistryWizardPageOne fFirstPage;
	private NewJavaProjectWizardPageTwo fSecondPage;

	public NewRegistryWizard()
	{
		fFirstPage = new NewJPFRegistryWizardPageOne();
		fSecondPage = new NewJavaProjectWizardPageTwo(fFirstPage);
	}

	@Override
	public void addPages()
	{
		addPage(fFirstPage);
		addPage(fSecondPage);

		fFirstPage.init(getSelection(), getActivePart());
	}

	private IWorkbenchPart getActivePart()
	{
		IWorkbenchWindow activeWindow = getWorkbench().getActiveWorkbenchWindow();
		if( activeWindow != null )
		{
			IWorkbenchPage activePage = activeWindow.getActivePage();
			if( activePage != null )
			{
				return activePage.getActivePart();
			}
		}
		return null;
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
	{
		fSecondPage.performFinish(monitor);
		IProject project = fSecondPage.getJavaProject().getProject();
		IProjectDescription description = project.getDescription();
		JPFJarNature.addNature(description);
		project.setDescription(description, SubMonitor.convert(monitor, "Add JPF Registry Nature", 1));
	}

	@Override
	public boolean performFinish()
	{
		boolean res = super.performFinish();
		if( res )
		{
			final IJavaElement newElement = getCreatedElement();

			IWorkingSet[] workingSets = fFirstPage.getWorkingSets();
			if( workingSets.length > 0 )
			{
				PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(newElement, workingSets);
			}
			BasicNewResourceWizard.selectAndReveal(fSecondPage.getJavaProject().getProject(), getWorkbench()
				.getActiveWorkbenchWindow());
		}
		return res;
	}

	@Override
	public IJavaElement getCreatedElement()
	{
		return fSecondPage.getJavaProject();
	}
}
