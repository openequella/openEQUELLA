package com.tle.ant;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.tools.ant.BaseJpfTask;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

public class SoapInterfaceScanner extends BaseJpfTask
{
	private File dest;
	private File fallbackLocation;
	private final Set<ExcludeInterface> excludeInterfaces = new HashSet<ExcludeInterface>();
	private boolean show;
	private Map<File, CompilationUnit> parsedFiles = new HashMap<File, CompilationUnit>();

	public boolean isShow()
	{
		return show;
	}

	public void setShow(boolean show)
	{
		this.show = show;
	}

	public File getDest()
	{
		return dest;
	}

	public void setDest(File dest)
	{
		this.dest = dest;
	}

	public File getFallbackLocation()
	{
		return fallbackLocation;
	}

	public void setFallbackLocation(File fallbackLocation)
	{
		this.fallbackLocation = fallbackLocation;
	}

	public void addExcludeInterface(ExcludeInterface excludeInterface)
	{
		excludeInterfaces.add(excludeInterface);
	}

	public static class ExcludeInterface
	{
		private String className;

		public String getClassName()
		{
			return className;
		}

		public void setClassName(String className)
		{
			this.className = className;
		}
	}

	@Override
	public void execute() throws BuildException
	{
		try
		{
			initRegistry(true);
			scanEndpoints();
			scanInterfaces();
		}
		catch( Exception e )
		{
			throw new BuildException(e);
		}

	}

	@SuppressWarnings("nls")
	private void scanEndpoints() throws Exception
	{
		ExtensionPoint point = getRegistry().getExtensionPoint("com.tle.web.remoting.soap", "endpoint");
		Collection<Extension> extensions = point.getConnectedExtensions();
		Set<String> excluded = new HashSet<String>();
		for( ExcludeInterface exclude : excludeInterfaces )
		{
			excluded.add(exclude.getClassName());
		}
		Map<String, FileSet> filesets = new HashMap<String, FileSet>();
		for( Extension extension : extensions )
		{
			Parameter interfaceParam = extension.getParameter("serviceInterface");
			if( interfaceParam != null )
			{
				String className = interfaceParam.valueAsString();
				if( excluded.contains(className) )
				{
					continue;
				}
				if( show )
				{
					log("Found SOAP interface:" + className);
				}
				String pluginId = extension.getDeclaringPluginDescriptor().getId();
				FileSet fileset = filesets.get(pluginId);
				if( fileset == null )
				{
					fileset = new FileSet();
					File baseDir = new File(new File(getPathResolver().getRegisteredContext(pluginId).toURI()), "src");
					fileset.setDir(baseDir);
					filesets.put(pluginId, fileset);
				}
				String sourceName = className.replace('.', '/') + ".java";
				FilenameSelector selector = new FilenameSelector();
				selector.setName(sourceName);
				fileset.addFilename(selector);
			}
		}
		Copy copyTask = new Copy();
		copyTask.bindToOwner(this);
		for( FileSet fileset : filesets.values() )
		{
			copyTask.addFileset(fileset);
		}
		copyTask.setVerbose(true);
		copyTask.setTodir(dest);
		copyTask.init();
		copyTask.execute();
	}

	@SuppressWarnings("nls")
	private void scanInterfaces() throws Exception
	{
		Map<String, CompilationUnit> javaFiles = scanCompilationUnits();

		ExtensionPoint point = getRegistry().getExtensionPoint("com.tle.web.remoting.soap", "endpoint-interface");
		Collection<Extension> extensions = point.getConnectedExtensions();
		for( Extension extension : extensions )
		{
			Parameter interfaceParam = extension.getParameter("serviceInterface");
			if( interfaceParam != null )
			{
				String className = interfaceParam.valueAsString();
				String pluginId = extension.getDeclaringPluginDescriptor().getId();
				File baseDir = new File(new File(getPathResolver().getRegisteredContext(pluginId).toURI()), "src");
				String sourceName = className.replace('.', '/') + ".java";
				File sourceFile = new File(baseDir, sourceName);

				Collection<Parameter> paths = extension.getParameters("path");
				for( Parameter pathParam : paths )
				{
					String path = pathParam.valueAsString();
					CompilationUnit outFile = javaFiles.get(path);
					addMethods(sourceFile, outFile);
				}
			}
		}
		for( CompilationUnit compilationUnit : javaFiles.values() )
		{
			String outPath = compilationUnit.getPackage().getName().getName().replace('.', '/');
			File pareDir = new File(dest, outPath);
			pareDir.mkdirs();
			File destJavaFile = new File(pareDir, compilationUnit.getTypes().get(0).getName() + ".java");
			CharStreams.copy(CharStreams.newReaderSupplier(compilationUnit.toString()),
				CharStreams.newWriterSupplier(Files.newOutputStreamSupplier(destJavaFile), Charsets.UTF_8));
		}
	}

	@SuppressWarnings("nls")
	private void addMethods(File sourceFile, CompilationUnit outFile) throws ParseException, IOException
	{
		CompilationUnit parsed = getParsedFile(sourceFile);
		List<ImportDeclaration> imports = outFile.getImports();
		if( imports == null )
		{
			imports = new ArrayList<ImportDeclaration>();
			outFile.setImports(imports);
		}
		List<ImportDeclaration> newImports = parsed.getImports();
		if( newImports != null )
		{
			imports.addAll(newImports);
		}
		TypeDeclaration interfaceDec = outFile.getTypes().get(0);

		List<TypeDeclaration> types = parsed.getTypes();
		for( TypeDeclaration typeDeclaration : types )
		{
			ClassOrInterfaceDeclaration clazz = (ClassOrInterfaceDeclaration) typeDeclaration;
			List<ClassOrInterfaceType> extendsList = clazz.getExtends();
			if( extendsList != null )
			{
				for( ClassOrInterfaceType extClazz : extendsList )
				{
					String extendsName = extClazz.getName();
					File extendedFile = new File(sourceFile.getParentFile(), extendsName + ".java");
					if( extendedFile.exists() )
					{
						addMethods(extendedFile, outFile);
					}
				}
			}
			List<BodyDeclaration> methods = typeDeclaration.getMembers();
			for( BodyDeclaration bodyDeclaration : methods )
			{
				ASTHelper.addMember(interfaceDec, bodyDeclaration);
			}
		}
	}

	private CompilationUnit getParsedFile(File sourceFile) throws ParseException, IOException
	{
		CompilationUnit compilationUnit = parsedFiles.get(sourceFile);
		if( compilationUnit == null )
		{
			compilationUnit = JavaParser.parse(sourceFile);
			parsedFiles.put(sourceFile, compilationUnit);
		}
		return compilationUnit;
	}

	@SuppressWarnings("nls")
	private Map<String, CompilationUnit> scanCompilationUnits() throws Exception
	{
		ExtensionPoint point = getRegistry().getExtensionPoint("com.tle.web.remoting.soap", "endpoint");
		Collection<Extension> extensions = point.getConnectedExtensions();
		Map<String, CompilationUnit> javaFiles = new HashMap<String, CompilationUnit>();
		for( Extension extension : extensions )
		{
			Parameter interfaceParam = extension.getParameter("serviceInterface");
			if( interfaceParam == null )
			{
				String path = extension.getParameter("path").valueAsString();
				String namespace = extension.getParameter("serviceNamespace").valueAsString();
				String name = extension.getParameter("serviceName").valueAsString();
				String packageName = getPackageFromNamespace(namespace);

				CompilationUnit cu = new CompilationUnit();
				cu.setPackage(new PackageDeclaration(new NameExpr(packageName)));
				ASTHelper.addTypeDeclaration(cu, new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, true, name));
				javaFiles.put(path, cu);
			}
		}
		return javaFiles;

	}

	private String getPackageFromNamespace(String namespace) throws MalformedURLException
	{
		URL url = new URL(namespace);
		String[] host = url.getHost().split("\\."); //$NON-NLS-1$
		List<String> packages = new ArrayList<String>(Arrays.asList(host));
		Collections.reverse(packages);
		StringBuilder sbuf = new StringBuilder();
		boolean first = true;
		for( String pakage : packages )
		{
			if( !first )
			{
				sbuf.append('.');
			}
			sbuf.append(pakage);
			first = false;
		}
		return sbuf.toString();
	}
}
