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

package com.dytech.gui.workers;

import java.awt.Component;

import javax.swing.RootPaneContainer;

public final class GlassPaneUtils
{
	private GlassPaneUtils()
	{
		throw new Error();
	}

	public synchronized static <T extends Component> T mount(Component startComponent, boolean stopClosing,
		boolean alwaysRecreateGlasspane, Class<T> paneClass, GlassPaneCallback<T> callback)
	{
		RootPaneContainer rootpane = getRootContainer(startComponent);
		if( rootpane != null )
		{
			Component gp = rootpane.getGlassPane();
			if( !alwaysRecreateGlasspane )
			{
				if( paneClass.isAssignableFrom(gp.getClass()) )
				{
					if( !gp.isVisible() )
					{
						T ogp = paneClass.cast(gp);
						callback.processExisting(ogp);
						return ogp;
					}
					else
					{
						return null;
					}
				}
			}

			T ngp = callback.construct();
			rootpane.setGlassPane(ngp);
			return ngp;
		}
		return null;
	}

	private static RootPaneContainer getRootContainer(Component startComponent)
	{
		Component aComponent = startComponent;

		// Climb the component hierarchy until a RootPaneContainer is found or
		// until the very top
		while( (aComponent.getParent() != null) && !(aComponent instanceof RootPaneContainer) )
		{
			aComponent = aComponent.getParent();
		}

		// Guard against error conditions if climb search wasn't successful
		if( aComponent instanceof RootPaneContainer )
		{
			return (RootPaneContainer) aComponent;
		}

		return null;
	}

	public interface GlassPaneCallback<T>
	{
		T construct();

		void processExisting(T gp);
	}
}
