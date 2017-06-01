/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.plugins;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.google.common.base.Throwables;

public class SerialisedValue<T> implements Externalizable
{
	private static final long serialVersionUID = 1;
	private byte[] data;
	private transient T object;
	private transient boolean objectRead;

	public SerialisedValue()
	{
		// for serialization
	}

	public SerialisedValue(T object)
	{
		setObject(object);
	}

	public final synchronized void setObject(T object)
	{
		this.object = object;
		objectRead = true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(getData());
	}

	@Override
	public synchronized void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		data = (byte[]) in.readObject();
	}

	public synchronized T getObject()
	{
		if( !objectRead )
		{
			PluginAwareObjectInputStream paois;
			try
			{
				paois = new PluginAwareObjectInputStream(new ByteArrayInputStream(data));
				object = (T) paois.readObject();
			}
			catch( Exception e )
			{
				Throwables.propagate(e);
			}
			objectRead = true;
		}
		return object;
	}

	public synchronized byte[] getData()
	{
		if( data == null )
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try( ObjectOutputStream oos = new PluginAwareObjectOutputStream(baos) )
			{
				oos.writeObject(object);
				oos.flush();
				data = baos.toByteArray();
			}
			catch( IOException e )
			{
				Throwables.propagate(e);
			}
		}
		return data;
	}

	public static <T> SerialisedValue<T> fromData(byte[] data)
	{
		SerialisedValue<T> sv = new SerialisedValue<T>();
		sv.data = data;
		return sv;
	}
}
