package com.tle.jpfclasspath;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.ClasspathFixProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;
import org.eclipse.swt.graphics.Image;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.tle.jpfclasspath.model.IPluginModel;
import com.tle.jpfclasspath.model.JPFProject;
import com.tle.jpfclasspath.model.JPFPluginModelManager;

@SuppressWarnings("nls")
public class JPFClasspathFixProcessor extends ClasspathFixProcessor
{
	private SAXBuilder sax;
	private XMLOutputter xmlOut;

	public JPFClasspathFixProcessor()
	{
		super();
		sax = new SAXBuilder();
		sax.setValidation(false);
		sax.setReuseParser(true);
		sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Format format = Format.getRawFormat();
		format.setOmitEncoding(true);
		format.setOmitDeclaration(true);
		format.setLineSeparator("\n");
		format.setEncoding("UTF-8");

		xmlOut = new XMLOutputter(format);
	}

	@Override
	public ClasspathFixProposal[] getFixImportProposals(final IJavaProject project, String name) throws CoreException
	{
		IProject requestedProject = project.getProject();
		if( !requestedProject.hasNature(JPFProjectNature.NATURE_ID) )
		{
			return null;
		}
		ArrayList<ClasspathFixProposal> props = new ArrayList<ClasspathFixProposal>();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		int idx = name.lastIndexOf('.');
		char[] packageName = idx != -1 ? name.substring(0, idx).toCharArray() : null;
		char[] typeName = name.substring(idx + 1).toCharArray();

		if( typeName.length == 1 && typeName[0] == '*' )
		{
			typeName = null;
		}

		ArrayList<TypeNameMatch> res = new ArrayList<TypeNameMatch>();
		TypeNameMatchCollector requestor = new TypeNameMatchCollector(res);

		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		int matchMode = SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE;
		new SearchEngine().searchAllTypeNames(packageName, matchMode, typeName, matchMode, IJavaSearchConstants.TYPE,
			scope, requestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);

		if( res.isEmpty() )
		{
			return null;
		}
		JPFPluginModelManager service = JPFPluginModelManager.instance();
		for( TypeNameMatch curr : res )
		{
			IType type = curr.getType();
			if( type != null )
			{
				IPackageFragmentRoot root = (IPackageFragmentRoot) type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
				IPluginModel model = null;
				if( root.isArchive() )
				{
					model = service.findModel((IFile) root.getResource());
				}
				else if( !root.isExternal() )
				{
					model = service.findModel(root.getResource().getProject());
				}
				if( model != null )
				{
					System.err.println("Found in " + model.getParsedManifest().getId());
					props.add(new JPFClasspathFixProposal(project, JPFProject.getManifest(requestedProject), model));
				}
			}
		}
		return props.toArray(new ClasspathFixProposal[props.size()]);
	}

	private Element getOrAdd(Element rootElem, String elem, String after)
	{
		Element element = rootElem.getChild(elem);
		if( element == null )
		{
			int index = 0;
			Element attributes = rootElem.getChild(after);
			if( attributes != null )
			{
				index = rootElem.indexOf(attributes) + 1;
			}
			element = new Element(elem);
			element.addContent("\n\t");
			rootElem.addContent(index, Arrays.asList(new Text("\n\t"), element));
		}
		return element;
	}

	private final class JPFClasspathFixProposal extends ClasspathFixProposal
	{
		private final IJavaProject project;
		private final IResource pluginJpf;
		private final IPluginModel model;

		private JPFClasspathFixProposal(IJavaProject project, IResource pluginJpf, IPluginModel model)
		{
			this.project = project;
			this.pluginJpf = pluginJpf;
			this.model = model;
		}

		@Override
		public int getRelevance()
		{
			return 9;
		}

		@Override
		public Image getImage()
		{
			return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
		}

		@Override
		public String getDisplayString()
		{
			return "Add plugin " + model.getParsedManifest().getId() + " to the plugin-jpf.xml";
		}

		@Override
		public String getAdditionalProposalInfo()
		{
			return "Will add an entry to this projects plugin-jpf.xml";
		}

		@Override
		public Change createChange(IProgressMonitor monitor) throws CoreException
		{
			return new ResourceChange()
			{

				@Override
				public Change perform(IProgressMonitor pm) throws CoreException
				{
					pm.beginTask("Channging plugin-jpf", 1);
					try
					{

						File file = pluginJpf.getRawLocation().toFile();
						Document build = sax.build(file);
						Element rootElement = build.getRootElement();
						Element requires = getOrAdd(rootElement, "requires", "attributes");
						Element importEntry = new Element("import");
						requires.addContent("\t");
						requires.addContent(importEntry);
						importEntry.setAttribute("plugin-id", model.getParsedManifest().getId());
						requires.addContent("\n\t");
						FileOutputStream fileOutputStream = new FileOutputStream(file);
						xmlOut.output(build, fileOutputStream);
						fileOutputStream.close();
						pluginJpf.touch(new SubProgressMonitor(pm, 1));

					}
					catch( Exception e )
					{
						throw new CoreException(new Status(IStatus.ERROR, "com.tle.jpfclasspath", e.getMessage(), e));
					}
					finally
					{
						pm.done();
					}
					return new NullChange();
				}

				@Override
				public String getName()
				{
					return "Channging plugin-jpf";
				}

				@Override
				protected IResource getModifiedResource()
				{
					return project.getResource();
				}

				@Override
				public Object getModifiedElement()
				{
					return pluginJpf;
				}
			};

		}
	}
}
