package com.tle.ant;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Ant.TargetElement;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.taskdefs.SubAnt;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.registry.PluginFragment;
import org.java.plugin.registry.PluginRegistry;
import org.java.plugin.registry.UniqueIdentity;
import org.java.plugin.util.IoUtil;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.collect.Sets;

public class ParallelBuildTask extends ExtendedBaseJpfTask
{
	private int threadCount;

	private String targets;

	private DefaultDirectedGraph<UniqueIdentity, DefaultEdge> graph;

	private boolean threadPool = true;

	private Object graphLock = new Object();
	private Object workLock = new Object();

	private Set<UniqueIdentity> visited = Collections.synchronizedSet(new HashSet<UniqueIdentity>());

	private String excluderefid;
	private Set<File> ignoredManifests = Sets.newHashSet();

	@Override
	public void execute() throws BuildException
	{
		setupExcludes();

		graph = new DefaultDirectedGraph<UniqueIdentity, DefaultEdge>(DefaultEdge.class);
		initRegistry(true);
		PluginRegistry jpfReg = getRegistry();

		Collection<PluginDescriptor> descs = jpfReg.getPluginDescriptors();
		for( PluginDescriptor desc : descs )
		{
			URL manifestUrl = desc.getLocation();
			File manifestFile = IoUtil.url2file(manifestUrl);
			if( manifestFile != null )
			{
				graph.addVertex(desc);
			}
		}

		Collection<PluginFragment> fragments = getRegistry().getPluginFragments();
		for( PluginFragment pluginFragment : fragments )
		{
			if( !graph.containsVertex(pluginFragment) )
			{
				URL manifestUrl = pluginFragment.getLocation();
				File manifestFile = IoUtil.url2file(manifestUrl);
				if( manifestFile != null )
				{
					graph.addVertex(pluginFragment);
				}
			}
		}

		for( PluginDescriptor desc : descs )
		{
			URL manifestUrl = desc.getLocation();
			File manifestFile = IoUtil.url2file(manifestUrl);
			if( manifestFile == null )
			{
				continue;
			}
			Collection<PluginDescriptor> dependingPlugins = jpfReg.getDependingPlugins(desc);
			for( PluginDescriptor dependingPlugin : dependingPlugins )
			{
				manifestUrl = dependingPlugin.getLocation();
				manifestFile = IoUtil.url2file(manifestUrl);
				if( manifestFile == null )
				{
					continue;
				}
				graph.addEdge(desc, dependingPlugin);

			}
		}

		for( PluginFragment pluginFragment : fragments )
		{
			URL manifestUrl = pluginFragment.getLocation();
			File manifestFile = IoUtil.url2file(manifestUrl);
			if( manifestFile == null )
			{
				continue;
			}
			PluginDescriptor pluginDescriptor = jpfReg.getPluginDescriptor(pluginFragment.getPluginId());
			manifestUrl = pluginDescriptor.getLocation();
			manifestFile = IoUtil.url2file(manifestUrl);
			if( manifestFile == null )
			{
				continue;
			}
			graph.addEdge(pluginFragment, pluginDescriptor);

		}

		if( threadCount == 0 )
		{
			threadCount = Runtime.getRuntime().availableProcessors();
		}
		if( threadPool )
		{
			log("Building with a threadpool " + threadCount + " threads.");
			threadPool();
		}
		else
		{
			log("Building with a parallel task with " + threadCount + " threads.");
			parallelTask();
		}

	}

	private void setupExcludes()
	{
		if( excluderefid != null )
		{
			ResourceCollection reference = (ResourceCollection) getProject().getReference(excluderefid);
			Iterator<Resource> iter = reference.iterator();
			while( iter.hasNext() )
			{
				Resource resource = iter.next();
				FileResource file = resource.as(FileResource.class);
				if( file != null )
				{
					ignoredManifests.add(file.getFile());
				}
			}
		}
	}

	protected void parallelTask()
	{
		while( !graph.vertexSet().isEmpty() )
		{
			Collection<UniqueIdentity> noDeps = new HashSet<UniqueIdentity>();
			Set<UniqueIdentity> vertexSet = graph.vertexSet();
			for( UniqueIdentity pluginDescriptor : vertexSet )
			{
				if( graph.inDegreeOf(pluginDescriptor) == 0 )
				{
					noDeps.add(pluginDescriptor);
				}
			}

			Parallel parallel = (Parallel) getProject().createTask("parallel");
			parallel.setThreadCount(threadCount);
			parallel.setFailOnAny(true);
			for( UniqueIdentity pluginDescriptor : noDeps )
			{
				URL manifestUrl;
				if( pluginDescriptor instanceof PluginFragment )
				{
					manifestUrl = ((PluginFragment) pluginDescriptor).getLocation();
				}
				else
				{
					manifestUrl = ((PluginDescriptor) pluginDescriptor).getLocation();
				}

				File manifestFile = IoUtil.url2file(manifestUrl);
				if( !ignoredManifests.contains(manifestFile) )
				{
					Path path = new Path(getProject());
					path.setLocation(manifestFile.getParentFile());
					Collection<String> collectTargets = collectTargets();
					SubAnt subant = (SubAnt) getProject().createTask("subant");
					subant.setBuildpath(path);
					for( String target : collectTargets )
					{
						TargetElement targetElement = new Ant.TargetElement();
						targetElement.setName(target);
						subant.addConfiguredTarget(targetElement);
					}
					subant.setFailonerror(true);
					parallel.addTask(subant);
				}
			}
			parallel.init();
			parallel.execute();
			graph.removeAllVertices(noDeps);
		}
	}

	protected void threadPool()
	{
		ExecutorService executor = Executors.newCachedThreadPool();
		Collection<Future<Exception>> futures = new ArrayList<Future<Exception>>();
		for( int i = 0; i < threadCount; i++ )
		{
			SubAntRunnable e = new SubAntRunnable();
			futures.add(executor.submit(e));
		}

		boolean running = true;
		while( running )
		{
			running = false;
			for( Future<Exception> future : futures )
			{
				boolean done = future.isDone();
				running |= !done;
				if( done )
				{
					try
					{
						Exception exception = future.get();
						if( exception != null )
						{
							throw new BuildException(exception);
						}
					}
					catch( Exception e )
					{
						throw new BuildException(e);
					}

				}
			}
			try
			{
				Thread.sleep(100);
			}
			catch( InterruptedException e )
			{
				// nothing
			}
		}

		executor.shutdown();
	}

	protected UniqueIdentity nextQueue()
	{
		synchronized( graphLock )
		{
			Set<UniqueIdentity> vertexSet = graph.vertexSet();
			for( UniqueIdentity pluginDescriptor : vertexSet )
			{
				if( graph.inDegreeOf(pluginDescriptor) == 0 )
				{
					if( !visited.contains(pluginDescriptor) )
					{
						visited.add(pluginDescriptor);
						return pluginDescriptor;
					}
				}
			}
		}
		return null;
	}

	protected class SubAntRunnable implements Callable<Exception>
	{
		@Override
		public Exception call()
		{
			UniqueIdentity pluginDescriptor = null;
			try
			{
				while( anythingLeft() )
				{
					pluginDescriptor = getNext();
					if( pluginDescriptor == null )
					{
						try
						{

							Thread.sleep(250);
						}
						catch( InterruptedException e )
						{
							// Nothing
						}
					}
					else
					{
						runAnt(pluginDescriptor);
						removeVert(pluginDescriptor);
					}
				}
			}
			catch( Exception e )
			{
				log(pluginDescriptor + " broken " + e);
				return e;
			}
			return null;
		}

		private void runAnt(UniqueIdentity pluginDescriptor)
		{
			URL manifestUrl;
			if( pluginDescriptor instanceof PluginFragment )
			{
				manifestUrl = ((PluginFragment) pluginDescriptor).getLocation();
			}
			else
			{
				manifestUrl = ((PluginDescriptor) pluginDescriptor).getLocation();
			}
			File manifestFile = IoUtil.url2file(manifestUrl);
			if( !ignoredManifests.contains(manifestFile) )
			{
				Path path = new Path(getProject());
				path.setLocation(manifestFile.getParentFile());
				Collection<String> collectTargets = collectTargets();
				SubAnt subant = (SubAnt) getProject().createTask("subant");
				subant.setBuildpath(path);
				for( String target : collectTargets )
				{
					TargetElement targetElement = new Ant.TargetElement();
					targetElement.setName(target);
					subant.addConfiguredTarget(targetElement);
				}
				subant.setFailonerror(true);
				subant.init();
				subant.execute();
			}
		}

		private UniqueIdentity getNext()
		{
			synchronized( workLock )
			{
				return nextQueue();
			}
		}
	}

	private void removeVert(UniqueIdentity pd)
	{
		synchronized( graphLock )
		{
			graph.removeVertex(pd);
		}
	}

	private boolean anythingLeft()
	{
		synchronized( graphLock )
		{
			return !graph.vertexSet().isEmpty();
		}
	}

	private Collection<String> collectTargets()
	{
		List<String> result = new ArrayList<String>();
		for( StringTokenizer st = new StringTokenizer(targets, ",", false); //$NON-NLS-1$
		st.hasMoreTokens(); )
		{
			result.add(st.nextToken());
		}
		return result;
	}

	public String getTargets()
	{
		return targets;
	}

	public void setTargets(String targets)
	{
		this.targets = targets;
	}

	public boolean isThreadPool()
	{
		return threadPool;
	}

	public void setThreadPool(boolean threadPool)
	{
		this.threadPool = threadPool;
	}

	public int getThreadCount()
	{
		return threadCount;
	}

	public void setThreadCount(int threadCount)
	{
		this.threadCount = threadCount;
	}

	public String getExcluderefid()
	{
		return excluderefid;
	}

	public void setExcluderefid(String excluderefid)
	{
		this.excluderefid = excluderefid;
	}

}
