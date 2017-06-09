package com.tle.webtests.framework.ant;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.LogLevel;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.tools.ant.BaseJpfTask;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@SuppressWarnings("nls")
public class PluginReport extends BaseJpfTask
{
	private ExecutionDataStore executionDataStore;
	private SessionInfoStore sessionInfoStore;
	private File sourcesDir;
	private File groupings;
	private File reportDir;
	private String coverageHost = "localhost";
	private boolean ignoreothers;
	private int coveragePort = 6300;
	private final Map<String, PluginCoverage> coveredPlugins = Maps.newHashMap();

	private String[] excludedClassPrefixes = new String[0];
	private ArrayList<String> excludedClasses = new ArrayList<String>();

	public void setExcludesfile(File excludesFile)
	{
		try
		{
			List<String> lines = Files.readLines(excludesFile, Charset.forName("UTF-8"));
			setExcludes(Joiner.on(",").join(lines));
		}
		catch( IOException e )
		{
			throw new BuildException("Cannot read file", e);
		}
	}

	public void setExcludes(String excludes)
	{
		ArrayList<String> exPre = new ArrayList<String>(excludedClasses);
		for( String exclude : excludes.split(",") )
		{
			exclude = exclude.trim();
			if( !Strings.isNullOrEmpty(exclude) && !exclude.startsWith("#") )
			{
				if( exclude.endsWith(".*") )
				{
					exPre.add(exclude.replace(".*", "").trim());
				}
				else
				{
					excludedClasses.add((exclude.trim() + ".class"));
				}
			}
		}
		excludedClassPrefixes = exPre.toArray(new String[exPre.size()]);
	}

	public void setPluginsDir(File file)
	{
		setBaseDir(file);
	}

	@Override
	public void execute() throws BuildException
	{
		if( reportDir == null )
		{
			throw new BuildException("You must specify the report output dir in 'reportDir'");
		}
		if( groupings == null )
		{
			throw new BuildException("You must specify the groupings file in 'groupings'");
		}
		initRegistry(true);
		try
		{
			loadExecutionData();

			final HTMLFormatter htmlFormatter = new HTMLFormatter();
			final IReportVisitor visitor = htmlFormatter.createVisitor(new FileMultiReportOutput(reportDir));

			visitor.visitInfo(sessionInfoStore.getInfos(), executionDataStore.getContents());

			IReportGroupVisitor allPluginsGroup = visitor.visitGroup("All Plugins");

			Gson gson = new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(PluginNode.class, new PluginNodeDeserializer()).create();

			PluginNode rootNode = gson.fromJson(Files.newReader(groupings, Charsets.UTF_8), PluginNode.class);
			visitNode(allPluginsGroup, rootNode);
			if( !ignoreothers )
			{
				Collection<PluginDescriptor> descs = getRegistry().getPluginDescriptors();
				List<String> otherPlugins = Lists.newArrayList();
				for( PluginDescriptor desc : descs )
				{
					String pluginId = desc.getId();
					if( !coveredPlugins.containsKey(pluginId) )
					{
						otherPlugins.add(pluginId);
					}
				}
				if( !otherPlugins.isEmpty() )
				{
					PluginNode othersNode = new PluginNode();
					othersNode.setPlugins(otherPlugins);
					othersNode.setName("Other Plugins");
					List<PluginNode> emptyChildren = Collections.emptyList();
					othersNode.setChildren(emptyChildren);
					visitNode(allPluginsGroup, othersNode);
				}
			}
			visitor.visitEnd();
		}
		catch( IOException e )
		{
			throw new BuildException(e);
		}
		catch( URISyntaxException e )
		{
			throw new BuildException(e);
		}
	}

	public void visitNode(IReportGroupVisitor parentGroup, PluginNode node) throws IOException, URISyntaxException
	{
		if( node.getName() != null )
		{
			parentGroup = parentGroup.visitGroup(node.getName());
		}
		List<PluginNode> children = node.getChildren();
		for( PluginNode pluginNode : children )
		{
			visitNode(parentGroup, pluginNode);
		}
		List<String> plugins = node.getPlugins();
		if( plugins != null )
		{
			for( String pluginId : plugins )
			{
				PluginCoverage coverage = analysePlugin(pluginId);
				if( coverage != null )
				{
					log("Visiting bundle:" + pluginId);
					parentGroup.visitBundle(coverage.getBundleCoverage(),
						new DirectorySourceFileLocator(coverage.getSourceDir(), "utf-8", 4));
				}
			}
		}
	}

	private PluginCoverage analysePlugin(String pluginId) throws IOException, URISyntaxException
	{
		if( coveredPlugins.containsKey(pluginId) )
		{
			return coveredPlugins.get(pluginId);
		}
		PluginCoverage coverage = null;
		if( getRegistry().isPluginDescriptorAvailable(pluginId) )
		{
			PluginDescriptor descriptor = getRegistry().getPluginDescriptor(pluginId);
			final CoverageBuilder coverageBuilder = new CoverageBuilder();
			final Analyzer analyzer = new Analyzer(executionDataStore, coverageBuilder);
			File classesFile = new File(getPathResolver().resolvePath(descriptor, "classes").toURI());
			if( classesFile.exists() )
			{
				analyzeFile(analyzer, classesFile);
			}
			File srcFile = new File(getPathResolver().resolvePath(descriptor, "src").toURI());
			if( !srcFile.exists() )
			{
				srcFile = sourcesDir;
			}
			coverage = new PluginCoverage(srcFile, coverageBuilder.getBundle(pluginId));
		}
		else
		{
			log("Plugin '" + pluginId + "' does not exist", LogLevel.WARN.getLevel());
		}
		coveredPlugins.put(pluginId, coverage);
		return coverage;
	}

	private void analyzeFile(Analyzer analyzer, File file) throws IOException
	{
		if( file.isDirectory() )
		{
			for( final File f : file.listFiles(new ClassFileFilter()) )
			{
				analyzeFile(analyzer, f);
			}
		}
		else
		{
			analyzer.analyzeAll(file);
		}
	}

	public class ClassFileFilter implements FileFilter
	{
		private final static String CLS = "/classes/";

		@Override
		public boolean accept(File file)
		{
			String path = file.getPath();
			String fullClassName = path.substring(path.indexOf(CLS) + CLS.length()).replaceAll("/", ".");

			return !excludedClasses.contains(fullClassName)
				&& !StringUtils.startsWithAny(fullClassName, excludedClassPrefixes);
		}
	}

	public static class PluginCoverage
	{
		private final IBundleCoverage bundleCoverage;
		private final File sourceDir;

		public PluginCoverage(File sourceDir, IBundleCoverage bundleCoverage)
		{
			this.bundleCoverage = bundleCoverage;
			this.sourceDir = sourceDir;
		}

		public IBundleCoverage getBundleCoverage()
		{
			return bundleCoverage;
		}

		public File getSourceDir()
		{
			return sourceDir;
		}
	}

	private void loadExecutionData() throws IOException
	{
		// Open a socket to the coverage agent:
		final Socket socket = new Socket(InetAddress.getByName(coverageHost), coveragePort);
		final RemoteControlWriter writer = new RemoteControlWriter(socket.getOutputStream());
		final RemoteControlReader reader = new RemoteControlReader(socket.getInputStream());
		sessionInfoStore = new SessionInfoStore();
		executionDataStore = new ExecutionDataStore();
		reader.setExecutionDataVisitor(executionDataStore);
		reader.setSessionInfoVisitor(sessionInfoStore);

		// Send a dump command and read the response:
		writer.visitDumpCommand(true, false);
		reader.read();

		socket.close();

		// fix me
		File testngCoverage = new File(reportDir.getParentFile(), "testcoverage.exec");
		if( !testngCoverage.exists() )
		{
			testngCoverage = new File(reportDir.getParentFile().getParentFile(), "coverage/testcoverage.exec");
		}

		if( testngCoverage.exists() )
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(testngCoverage);
				final ExecutionDataReader executionDataReader = new ExecutionDataReader(fis);

				executionDataReader.setExecutionDataVisitor(executionDataStore);
				executionDataReader.setSessionInfoVisitor(sessionInfoStore);

				while( executionDataReader.read() )
				{

				}
			}
			finally
			{
				IOUtils.closeQuietly(fis);
			}
		}
	}

	public File getSourcesDir()
	{
		return sourcesDir;
	}

	public void setSourcesDir(File sourcesDir)
	{
		this.sourcesDir = sourcesDir;
	}

	public static void main(String[] args) throws IOException
	{
		PluginReport report = new PluginReport();
		Project project = new Project();
		report.setProject(project);
		report.setBaseDir(new File("/home/jolz/gitrepos/trunk/Source/Plugins")); //$NON-NLS-1$
		report.setIncludes("**/plugin-jpf.xml"); //$NON-NLS-1$
		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		project.addBuildListener(consoleLogger);
		report.execute();
	}

	public File getGroupings()
	{
		return groupings;
	}

	public void setGroupings(File groupings)
	{
		this.groupings = groupings;
	}

	public File getReportDir()
	{
		return reportDir;
	}

	public void setReportDir(File reportDir)
	{
		this.reportDir = reportDir;
	}

	public String getCoverageHost()
	{
		return coverageHost;
	}

	public void setCoverageHost(String coverageHost)
	{
		this.coverageHost = coverageHost;
	}

	public int getCoveragePort()
	{
		return coveragePort;
	}

	public void setCoveragePort(int coveragePort)
	{
		this.coveragePort = coveragePort;
	}

	public boolean isIgnoreothers()
	{
		return ignoreothers;
	}

	public void setIgnoreothers(boolean ignoreothers)
	{
		this.ignoreothers = ignoreothers;
	}
}
