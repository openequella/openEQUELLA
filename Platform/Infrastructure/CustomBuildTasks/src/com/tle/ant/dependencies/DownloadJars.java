package com.tle.ant.dependencies;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.providers.http.HttpWagon;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.layout.RepositoryLayout;
import org.eclipse.aether.spi.connector.layout.RepositoryLayoutFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.NoRepositoryLayoutException;
import org.eclipse.aether.transport.wagon.WagonProvider;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.visitor.PostorderNodeListGenerator;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.sun.org.apache.xml.internal.serialize.LineSeparator;

@SuppressWarnings("nls")
public class DownloadJars extends Task
{
	private final Map<String, Dep> depsMap = Maps.newConcurrentMap();

	private static String templateJpf;
	private static String templateJpfFragment;
	private static XMLOutputter xmlOut;

	private SAXBuilder sax = DownloadJars.createSAXBuilder();
	private File destDir;
	private File sourcesDir;
	private File dependencies;
	private boolean createClasspath;
	private boolean createJpf;
	private Gson gson;

	private Vector<FileSet> filesets = new Vector<FileSet>();
	private List<Repository> repos = Lists.newArrayList();

	public DownloadJars() throws IOException
	{
		templateJpf = Resources.toString(DownloadJars.class.getResource("template-plugin-jpf.xml"), Charsets.UTF_8);
		templateJpfFragment = Resources.toString(DownloadJars.class.getResource("plugin-fragment-jpf.xml"),
			Charsets.UTF_8);
		Format format = Format.getRawFormat();
		format.setOmitEncoding(true);
		format.setOmitDeclaration(true);
		format.setLineSeparator(LineSeparator.Unix);
		xmlOut = new XMLOutputter(format);
		gson = new Gson();
	}

	public Repository createRepository()
	{
		Repository repo = new Repository();
		repos.add(repo);
		return repo;
	}

	@Override
	public void execute() throws BuildException
	{
		try
		{
			Set<Exclusion> globalExcludes = Sets.newHashSet();
			final Set<String> globalIgnores = Sets.newHashSet();
			List<Dep> depsList = Lists.newArrayList();
			List<File> fileList = Lists.newArrayList();
			if( dependencies != null )
			{
				fileList.add(dependencies);
			}

			for( FileSet fileset : filesets )
			{
				Iterator<Resource> iterator = fileset.iterator();
				while( iterator.hasNext() )
				{
					Resource res = iterator.next();
					if( res instanceof FileResource )
					{
						File file = ((FileResource) res).getFile();
						fileList.add(file);
					}
				}
			}
			checkParams(fileList);

			Map<String, String> versionMap = Maps.newHashMap();
			for( File dependency : fileList )
			{
				String jsonTxt = Files.toString(dependency, Charsets.UTF_8);

				Deps dependencies = gson.fromJson(jsonTxt, Deps.class);

				List<String> exclusions = dependencies.getExclusions();
				if( exclusions != null )
				{
					for( String exclusion : exclusions )
					{
						globalExcludes.add(convertExclusion(exclusion));
					}
				}
				List<String> ignoreArtifacts = dependencies.getIgnoredArtifacts();
				if( ignoreArtifacts != null )
				{
					globalIgnores.addAll(ignoreArtifacts);
				}

				Map<String, String> versions = dependencies.getVersions();
				if( versions != null )
				{
					versionMap.putAll(versions);
				}
				depsList.addAll(dependencies.getDependencies());
			}

			List<Dependency> deps = Lists.newArrayList();
			for( Dep dep : depsList )
			{
				String key = dep.getGroupId() + ":" + dep.getArtifactId();

				if( depsMap.containsKey(key) )
				{
					log(key + " is already in the list!", Project.MSG_WARN);
					continue;
				}
				if( dep.getVersion() == null )
				{
					depsMap.put(key, dep);
					continue;
				}
				resolveVersion(versionMap, dep);
				depsMap.put(key, dep);

				DefaultArtifact artifact = new DefaultArtifact(dep.getGroupId(), dep.getArtifactId(),
					dep.getClassifier(), dep.getExtension(), dep.getVersion());

				List<String> excludes = dep.getExcludes();
				Collection<Exclusion> exCol = Sets.newHashSet();
				if( excludes != null && !excludes.isEmpty() )
				{
					for( String ex : excludes )
					{
						exCol.add(convertExclusion(ex));
					}
				}
				deps.add(new Dependency(artifact, "compile", false, exCol));

				if( !dep.isNoSource() && sourcesDir != null && !"sources".equals(dep.getClassifier()) )
				{
					String classifier = dep.getClassifier();
					if( "".equals(classifier) )
					{
						classifier = "sources";
					}
					else
					{
						classifier += "-sources";
					}
					DefaultArtifact source = new DefaultArtifact(dep.getGroupId(), dep.getArtifactId(), classifier,
						dep.getExtension(), dep.getVersion());
					deps.add(new Dependency(source, "compile", true, exCol));
				}
			}

			final RepositorySystem repoSystem = newRepositorySystem();
			final RepositorySystemSession session = newSession(repoSystem, globalExcludes);

			CollectRequest collectRequest = new CollectRequest();
			collectRequest.setDependencies(deps);

			// Our list of repositories - this should probably match what we
			// have in ivysettings.xml or we're doing something wrong.
			final List<RemoteRepository> repositories = Lists.newArrayList();

			for( Repository repo : repos )
			{
				Builder builder = new RemoteRepository.Builder(repo.getId(), repo.getType(), repo.getUrl());
				String username = repo.getUsername();
				String password = repo.getPassword();
				if( username != null )
				{
					AuthenticationBuilder auth = new AuthenticationBuilder();
					auth.addUsername(username);
					auth.addPassword(password);
					builder.setAuthentication(auth.build());
				}
				repositories.add(builder.build());
			}

			for( RemoteRepository repository : repositories )
			{
				collectRequest.addRepository(repository);
			}

			DependencyNode node = repoSystem.collectDependencies(session, collectRequest).getRoot();
			// node.accept(new ConsoleDependencyGraphDumper());
			final OptionalDependancyFilter filter = new OptionalDependancyFilter();
			DependencyRequest dependencyRequest = new DependencyRequest(node, filter);
			DependencyNode root = repoSystem.resolveDependencies(session, dependencyRequest).getRoot();
			root.accept(new ConsoleDependencyGraphDumper());

			PostorderNodeListGenerator polg = new PostorderNodeListGenerator();
			root.accept(polg);

			destDir.mkdirs();
			if( sourcesDir != null )
			{
				sourcesDir.mkdirs();
			}
			Document classpathDoc = sax.getFactory().document(new Element("classpath"));
			final Element classpathRoot = classpathDoc.getRootElement();

			ExecutorService executor = Executors.newFixedThreadPool(8);
			List<DependencyNode> nodes = polg.getNodes();
			final BuildException[] throwable = new BuildException[1];
			for( final DependencyNode n : nodes )
			{
				if( throwable[0] != null )
				{
					break;
				}
				Runnable runnable = new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							processArtifact(repoSystem, session, repositories, filter, classpathRoot, n, globalIgnores);
						}
						catch( Exception e )
						{
							throwable[0] = new BuildException("error on: " + n.getArtifact().getFile(), e);
						}
					}
				};
				executor.execute(runnable);
			}
			executor.shutdown();
			executor.awaitTermination(30, TimeUnit.MINUTES);

			if( throwable[0] != null )
			{
				throw throwable[0];
			}

			if( createClasspath )
			{
				FileOutputStream fileOutputStream = new FileOutputStream(new File(destDir, ".classpath"));
				xmlOut.output(classpathDoc, fileOutputStream);
				fileOutputStream.close();
			}
		}
		catch( Exception e )
		{
			throw new BuildException(e);
		}
	}

	private void processArtifact(RepositorySystem repoSystem, RepositorySystemSession session,
		List<RemoteRepository> repositories, OptionalDependancyFilter filter, Element classpathRoot, DependencyNode n,
		Set<String> globalIgnores) throws IOException, Exception
	{
		Dependency dep = n.getDependency();

		Artifact artifact = dep.getArtifact();
		String fullId = artifact.getGroupId() + ':' + artifact.getArtifactId();
		if( globalIgnores.contains(fullId) )
		{
			return;
		}
		String classifier = dep.getArtifact().getClassifier();
		Dep localDep = depsMap.get(fullId);
		if( (localDep == null || !localDep.getClassifier().equals("sources")) && classifier != null
			&& classifier.endsWith("sources") )
		{
			File file = artifact.getFile();
			if( file != null )
			{
				Files.copy(file, new File(sourcesDir, file.getName()));
			}
		}
		else
		{
			if( createClasspath )
			{
				String name = artifact.getFile().getName();
				synchronized( classpathRoot )
				{
					classpathRoot.addContent(new Element("classpathentry").setAttribute("kind", "lib")
						.setAttribute("path", name).setAttribute("exported", "true")
						.setAttribute("sourcepath", "lib-src/" + name.replace(".jar", "-sources.jar")));
				}
			}
			if( createJpf )
			{
				createJpf(dep, artifact, repositories, repoSystem, session, filter);
			}
			else
			{
				File file = artifact.getFile();
				Files.copy(file, new File(destDir, file.getName()));
			}
		}
	}

	private void resolveVersion(Map<String, String> versionMap, Dep dep)
	{
		String version = dep.getVersion();
		if( version.startsWith("$") )
		{
			dep.setVersion(versionMap.get(version.substring(1)));
		}
	}

	private Exclusion convertExclusion(String exclusion)
	{
		String[] parts = exclusion.split(":");
		Exclusion exclude = new Exclusion(parts[0], parts.length > 1 ? parts[1] : "*",
			parts.length > 2 ? parts[2] : "*", parts.length > 3 ? parts[3] : "*");
		return exclude;
	}

	private void checkParams(List<File> fileList)
	{
		if( fileList.isEmpty() )
		{
			throw new BuildException("Must supply a dependencies file");
		}

		if( destDir == null )
		{
			throw new BuildException("Must supply a destination dir");
		}
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

	public static void main(String[] args) throws Exception
	{
		DownloadJars downloadJars = new DownloadJars();
		Project project = new Project();
		downloadJars.setProject(project);
		downloadJars.setDependencies(new File(args[0]));
		downloadJars.setDestDir(new File(args[1]));
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);
		downloadJars.execute();
	}

	public static SAXBuilder createSAXBuilder()
	{
		SAXBuilder builder = new SAXBuilder();
		builder.setValidation(false);
		builder.setReuseParser(true);
		builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		return builder;
	}

	public static void copy(byte[] buffer, InputStream input, OutputStream output) throws IOException
	{
		int bytesRead;
		while( (bytesRead = input.read(buffer)) != -1 )
		{
			output.write(buffer, 0, bytesRead);
		}
	}

	private void createJpf(Dependency dep, Artifact artifact, List<RemoteRepository> repositories,
		RepositorySystem repoSystem, RepositorySystemSession session, OptionalDependancyFilter filter) throws Exception
	{
		String artifactId = artifact.getGroupId() + ":" + artifact.getArtifactId();
		String jpfId = artifactId;
		String fragmentHost = null;

		String pluginXml = templateJpf;
		Dep localDep = depsMap.get(artifactId);
		if( localDep != null )
		{
			if( localDep.getRename() != null )
			{
				jpfId = localDep.getRename();
			}
			fragmentHost = localDep.getJpfFragment();
			if( fragmentHost != null )
			{
				pluginXml = templateJpfFragment;
				jpfId += "-fragment";
			}
		}

		CollectRequest collectRequest = new CollectRequest(dep, repositories);
		DependencyNode depNode = repoSystem.collectDependencies(session, collectRequest).getRoot();
		List<DependencyNode> children = depNode.getChildren();

		String xmlString = createXmlString(filter, artifactId, jpfId, fragmentHost, pluginXml, localDep, children);

		addToZip(artifact.getFile(), xmlString);
	}

	private synchronized String createXmlString(OptionalDependancyFilter filter, String artifactId, String jpfId,
		String fragmentHost, String pluginXml, Dep localDep, List<DependencyNode> children)
			throws JDOMException, IOException
	{
		System.out.println("Creating jpf plugin " + artifactId);
		Document jpfXml = sax.build(new StringReader(pluginXml));
		Element rootElement = jpfXml.getRootElement();
		rootElement.setAttribute("id", jpfId);
		if( fragmentHost != null )
		{
			rootElement.setAttribute("plugin-id", fragmentHost);
		}
		Element libNode = rootElement.getChild("runtime").getChild("library");
		libNode.setAttribute("id", artifactId);

		if( !children.isEmpty() )
		{
			for( DependencyNode child : children )
			{
				if( filter.accept(child, null) )
				{
					Artifact artifact2 = child.getDependency().getArtifact();
					String depId = artifact2.getGroupId() + ":" + artifact2.getArtifactId();
					if( !depId.equals(artifactId) && !depId.equals(fragmentHost) )
					{
						Element requires = getOrAdd(rootElement, "requires", "attributes");
						Element importEntry = new Element("import");
						requires.addContent("\t");
						requires.addContent(importEntry);
						importEntry.setAttribute("plugin-id", depId);

						if( localDep != null && localDep.getJpfExports().contains(depId) )
						{
							importEntry.setAttribute("exported", "true");
						}
						requires.addContent("\n\t");
					}
				}
			}
		}

		if( localDep != null )
		{
			List<String> addDeps = localDep.getJpfIncludes();
			for( String depId : addDeps )
			{
				Element requires = getOrAdd(rootElement, "requires", "attributes");
				Element importEntry = new Element("import");
				requires.addContent("\t");
				requires.addContent(importEntry);
				importEntry.setAttribute("plugin-id", depId);
				List<String> jpfExports = localDep.getJpfExports();
				if( jpfExports.contains(depId) )
				{
					importEntry.setAttribute("exported", "true");
				}
				requires.addContent("\n\t");
			}
		}

		String xmlString = xmlOut.outputString(jpfXml);
		return xmlString;
	}

	public void addToZip(File zip, String jpf) throws Exception
	{
		byte[] buffer = new byte[4096 * 1024];
		ZipFile jar = new ZipFile(zip);
		ZipOutputStream append = new ZipOutputStream(new FileOutputStream(new File(destDir, zip.getName())));

		HashSet<String> names = new HashSet<>();
		Enumeration<? extends ZipEntry> entries = jar.entries();
		while( entries.hasMoreElements() )
		{
			ZipEntry e = entries.nextElement();
			if( names.add(e.getName()) )
			{
				if( !e.isDirectory() )
				{
					e.setCompressedSize(-1);
					append.putNextEntry(e);
					copy(buffer, jar.getInputStream(e), append);
					append.closeEntry();
				}
				else
				{
					append.putNextEntry(e);
					append.closeEntry();
				}
			}
		}
		ZipEntry e = new ZipEntry("plugin-jpf.xml");
		append.putNextEntry(e);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(jpf.getBytes(Charsets.UTF_8));
		copy(buffer, inputStream, append);
		append.closeEntry();
		inputStream.close();
		jar.close();
		append.close();
	}

	private static RepositorySystemSession newSession(RepositorySystem system, Set<Exclusion> globalExcludes)
	{
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		// session.setDependencyGraphTransformer(new
		// NoopDependencyGraphTransformer());
		DependencySelector depFilter = new AndDependencySelector(new ScopeDependencySelector("test", "provided"),
			new OptionalDependencySelector(), new ExclusionDependencySelector(globalExcludes));
		session.setDependencySelector(depFilter);
		LocalRepository localRepo = new LocalRepository(System.getProperty("user.home") + "/.local-repo");

		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		return session;
	}

	private static RepositorySystem newRepositorySystem()
	{
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

		locator.setServices(WagonProvider.class, new ManualWagonProvider());
		locator.setService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, WagonTransporterFactory.class);
		locator.addService(RepositoryLayoutFactory.class, SpringSourceLayoutFactory.class);
		return locator.getService(RepositorySystem.class);
	}

	public static class SpringSourceLayoutFactory implements RepositoryLayoutFactory
	{

		@Override
		public float getPriority()
		{
			return 0;
		}

		@Override
		public RepositoryLayout newInstance(RepositorySystemSession repoSystem, RemoteRepository repo)
			throws NoRepositoryLayoutException
		{
			if( "springsource".equals(repo.getContentType()) )
			{
				return new RepositoryLayout()
				{

					private URI toUri(String path)
					{
						try
						{
							return new URI(null, null, path, null);
						}
						catch( URISyntaxException e )
						{
							throw new IllegalStateException(e);
						}
					}

					@Override
					public List<Checksum> getChecksums(Artifact artifact, boolean upload, URI location)
					{
						return getChecksums(location);
					}

					@Override
					public List<Checksum> getChecksums(Metadata metadata, boolean upload, URI location)
					{
						return getChecksums(location);
					}

					private List<Checksum> getChecksums(URI location)
					{
						return Arrays.asList(Checksum.forLocation(location, "SHA-1"),
							Checksum.forLocation(location, "MD5"));
					}

					@Override
					public URI getLocation(Artifact artifact, boolean arg1)
					{
						StringBuilder path = new StringBuilder(128);

						path.append(artifact.getGroupId()).append('/');

						path.append(artifact.getArtifactId()).append('/');

						path.append(artifact.getBaseVersion()).append('/');

						path.append(artifact.getArtifactId()).append('-').append(artifact.getVersion());

						if( artifact.getClassifier().length() > 0 )
						{
							path.append('-').append(artifact.getClassifier());
						}

						if( artifact.getExtension().length() > 0 )
						{
							path.append('.').append(artifact.getExtension());
						}

						return toUri(path.toString());
					}

					@Override
					public URI getLocation(Metadata metadata, boolean arg1)
					{
						StringBuilder path = new StringBuilder(128);

						if( metadata.getGroupId().length() > 0 )
						{
							path.append(metadata.getGroupId()).append('/');

							if( metadata.getArtifactId().length() > 0 )
							{
								path.append(metadata.getArtifactId()).append('/');

								if( metadata.getVersion().length() > 0 )
								{
									path.append(metadata.getVersion()).append('/');
								}
							}
						}

						path.append(metadata.getType());

						return toUri(path.toString());
					}

				};
			}
			throw new NoRepositoryLayoutException(repo);
		}

	}

	public static class ManualWagonProvider implements WagonProvider
	{
		@Override
		public Wagon lookup(String roleHint) throws Exception
		{
			if( "file".equals(roleHint) )
			{
				return new FileWagon();
			}
			else if( roleHint != null && roleHint.startsWith("http") )
			{ // http and https
				return new HttpWagon();
			}
			return null;
		}

		@Override
		public void release(Wagon wagon)
		{
			// no-op
		}
	}

	public class ConsoleDependencyGraphDumper implements DependencyVisitor
	{
		private PrintStream out;
		private String currentIndent = "";

		public ConsoleDependencyGraphDumper()
		{
			this(null);
		}

		public ConsoleDependencyGraphDumper(PrintStream out)
		{
			this.out = (out != null) ? out : System.out;
		}

		@Override
		public boolean visitEnter(DependencyNode node)
		{
			out.println(currentIndent + node);
			if( currentIndent.length() <= 0 )
			{
				currentIndent = "+- ";
			}
			else
			{
				currentIndent = "|  " + currentIndent;
			}
			return true;
		}

		@Override
		public boolean visitLeave(DependencyNode node)
		{
			currentIndent = currentIndent.substring(3, currentIndent.length());
			return true;
		}

	}

	public class OptionalDependancyFilter implements DependencyFilter
	{
		@Override
		public boolean accept(DependencyNode node, List<DependencyNode> parents)
		{
			Dependency dependency = node.getDependency();

			if( dependency != null && !"sources".equals(dependency.getArtifact().getClassifier())
				&& dependency.isOptional() )
			{
				return false;
			}
			return true;
		}
	}

	public File getDependencies()
	{
		return dependencies;
	}

	public void setDependencies(File dependencies)
	{
		this.dependencies = dependencies;
	}

	public File getDestDir()
	{
		return destDir;
	}

	public void setDestDir(File destDir)
	{
		this.destDir = destDir;
	}

	public File getSourcesDir()
	{
		return sourcesDir;
	}

	public void setSourcesDir(File sourcesDir)
	{
		this.sourcesDir = sourcesDir;
	}

	public boolean isCreateClasspath()
	{
		return createClasspath;
	}

	public void setCreateClasspath(boolean createClasspath)
	{
		this.createClasspath = createClasspath;
	}

	public boolean isCreateJpf()
	{
		return createJpf;
	}

	public void setCreateJpf(boolean createJpf)
	{
		this.createJpf = createJpf;
	}

	public void addFileset(FileSet fileset)
	{
		filesets.add(fileset);
	}
}
