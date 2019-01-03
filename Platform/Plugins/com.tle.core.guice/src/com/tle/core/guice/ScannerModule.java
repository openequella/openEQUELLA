/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.guice;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.java.plugin.PluginClassLoader;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.tle.core.plugins.PluginService;

@SuppressWarnings("nls")
public class ScannerModule extends AbstractModule
{
	private static final String BIND_ANNOTATION = fullName(Bind.class);
	private static final String BINDINGS_ANNOTATION = fullName(Bindings.class);
	private static final String BINDFACTORY_ANNOTATION = fullName(BindFactory.class);

	private static Set<String> ALL_ANNOTATIONS = new HashSet<String>(Arrays.asList(BIND_ANNOTATION,
		BINDINGS_ANNOTATION, BINDFACTORY_ANNOTATION));

	private final ClassLoader classLoader;
	private final PluginService pluginService;
	private final List<BeanChecker> beanCheckers;

	private Map<Class<?>, Set<Class<?>>> interfaceMap = new HashMap<Class<?>, Set<Class<?>>>();
	private List<String> bindingClasses = new ArrayList<String>();

	private static String fullName(Class<? extends Annotation> annot)
	{
		return 'L' + annot.getName().replace('.', '/') + ';';
	}

	public ScannerModule(PluginService pluginService, ClassLoader classLoader, Iterable<URL> localClassPath,
		List<BeanChecker> beanCheckers)
	{
		this.pluginService = pluginService;
		this.classLoader = classLoader;
		this.beanCheckers = beanCheckers;

		try
		{
			for( URL url : localClassPath )
			{
				if( url.getProtocol().equals("file") )
				{
					File file = new File(url.toURI());
					visitFile(file);

				}
				else if( url.getProtocol().equals("jar") )
				{
					visitJar(url);
				}
			}
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
		catch( URISyntaxException e )
		{
			throw new RuntimeException(e);
		}
	}

	private void visitJar(URL url) throws IOException, URISyntaxException
	{
		String pathName = url.getPath();
		int pathInd = pathName.lastIndexOf('!');
		String path = pathName.substring(pathInd + 2);
		ZipFile zipFile = null;
		try
		{
			zipFile = new ZipFile(new File(new URL(pathName.substring(0, pathInd)).toURI()));
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while( entries.hasMoreElements() )
			{
				ZipEntry entry = entries.nextElement();
				String filename = entry.getName();
				if( filename.startsWith(path) && filename.endsWith(".class") )
				{
					InputStream inputStream = zipFile.getInputStream(entry);
					readAndClose(inputStream);
				}
			}
		}
		finally
		{
			if( zipFile != null )
			{
				zipFile.close();
			}
		}
	}

	private void readAndClose(InputStream inputStream)
	{
		try( BufferedInputStream bufInp = new BufferedInputStream(inputStream) )
		{
			ClassReader reader = new ClassReader(bufInp);
			reader.accept(new Visitor(), ClassReader.SKIP_CODE);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	private void visitFile(File file)
	{
		if( file.isDirectory() )
		{
			File[] files = file.listFiles();
			for( File file2 : files )
			{
				visitFile(file2);
			}
		}
		else if( file.getName().endsWith(".class") )
		{
			try
			{
				FileInputStream finp = new FileInputStream(file);
				readAndClose(finp);
			}
			catch( FileNotFoundException e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	protected void configure()
	{
		bind(PluginService.class).toProvider(new Provider<PluginService>()
		{
			@Override
			public PluginService get()
			{
				return pluginService;
			}
		});
		bind(PluginClassLoader.class).toInstance((PluginClassLoader) classLoader);
		doConfigure();
	}

	@SuppressWarnings("unchecked")
	private <T> void doConfigure()
	{
		for( String binding : bindingClasses )
		{
			try
			{
				Class<T> actualClass = (Class<T>) classLoader.loadClass(binding);

				BindFactory bindFactory = actualClass.getAnnotation(BindFactory.class);
				if( bindFactory != null )
				{
					install(new FactoryModuleBuilder().build(actualClass));
				}
				else
				{
					Bindings bindings = actualClass.getAnnotation(Bindings.class);
					if( bindings != null )
					{
						for( Bind bind : bindings.value() )
						{
							bindOne(actualClass, bind);
						}
					}
					else
					{
						Bind bind = actualClass.getAnnotation(Bind.class);
						if( bind != null )
						{
							bindOne(actualClass, bind);
						}
						else
						{
							bind(Key.get(Object.class, Names.named(actualClass.getName()))).to(actualClass);
							bind(actualClass);
						}
					}
				}

				// Bean checkers to do extra validation, manipulation,
				// whatever...
				if( !beanCheckers.isEmpty() )
				{

					Set<Class<?>> interfaces = getAllInterfaces(actualClass);

					for( BeanChecker bc : beanCheckers )
					{
						bc.check(pluginService, actualClass, interfaces);
					}
				}
			}
			catch( ClassNotFoundException e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void bindOne(Class<T> actualClass, Bind bind)
	{
		Class<T> clazz = (Class<T>) bind.value();
		if( clazz == Bind.class || (clazz == actualClass && bind.types().length == 0) )
		{
			bind(actualClass);
			return;
		}
		com.tle.core.guice.Type[] types = bind.types();
		if( types.length == 0 )
		{
			bind(clazz).to(actualClass);
		}
		else
		{
			Type[] actualTypes = new Type[types.length];
			int i = 0;
			for( com.tle.core.guice.Type type : types )
			{
				Type paramType = type.value();
				if( type.unknown() )
				{
					paramType = Types.subtypeOf(paramType);
				}
				actualTypes[i++] = paramType;
			}
			TypeLiteral<T> typeLiteral = (TypeLiteral<T>) TypeLiteral.get(Types
				.newParameterizedType(clazz, actualTypes));
			bind(typeLiteral).to(actualClass);
		}
	}

	private Set<Class<?>> getAllInterfaces(Class<?> clazz)
	{
		Set<Class<?>> interfaces = interfaceMap.get(clazz);
		if( interfaces != null )
		{
			return interfaces;
		}
		interfaces = new HashSet<Class<?>>();
		Class<?>[] declaredInterfaces = clazz.getInterfaces();
		for( Class<?> decInt : declaredInterfaces )
		{
			if( !isIgnored(decInt) )
			{
				interfaces.add(decInt);
				interfaces.addAll(getAllInterfaces(decInt));
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		if( superclass != null )
		{
			interfaces.addAll(getAllInterfaces(superclass));
		}
		interfaceMap.put(clazz, interfaces);
		return interfaces;
	}

	private boolean isIgnored(Class<?> decInt)
	{
		return decInt.getPackage().getName().startsWith("java.");
	}

	class Visitor extends ClassVisitor
	{
		public Visitor()
		{
			super(Opcodes.ASM4);
		}

		String name;
		boolean bind;

		@Override
		public void visit(int classVersion, int accessFlags, String name, String signature, String parentClass,
			String[] interfaces)
		{
			this.name = fixClassName(name);
		}

		private String fixClassName(String name)
		{
			return name.replace('/', '.');
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible)
		{
			if( ALL_ANNOTATIONS.contains(desc) )
			{
				bind = true;
			}
			return null;
		}

		@Override
		public void visitEnd()
		{
			if( bind )
			{
				bindingClasses.add(name);
			}
		}

		@Override
		public void visitAttribute(Attribute arg0)
		{
			// nothing
		}

		@Override
		public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4)
		{
			return null;
		}

		@Override
		public void visitInnerClass(String arg0, String arg1, String arg2, int arg3)
		{
			// nothing
		}

		@Override
		public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3, String[] arg4)
		{
			return null;
		}

		@Override
		public void visitOuterClass(String arg0, String arg1, String arg2)
		{
			// nothing
		}

		@Override
		public void visitSource(String arg0, String arg1)
		{
			// nothing
		}
	}

}
