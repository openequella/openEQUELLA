/*
 * Copyright 2019 Apereo
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

package com.dytech.common.producers;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a basic warehouse facility. It is expected that this class will be
 * extended in order to use <code>addProduct</code>. Multiple products can be
 * added, and consumers can get them. If no products are available when a
 * consumer asks, they will be blocked until a product becomes available.
 * 
 * @author Nicholas Read
 */
public class Warehouse<T>
{
	private List<T> store;
	private boolean closed;

	/**
	 * Constructs a new Warehouse.
	 */
	public Warehouse()
	{
		store = new ArrayList<T>();
	}

	/**
	 * Adds a product to the warehouse.
	 * 
	 * @param product The product to add.
	 */
	protected synchronized void addProduct(T product)
	{
		if( product != null )
		{
			store.add(product);
			notifyAll();
		}
	}

	/**
	 * Removes all products from the warehouse.
	 */
	protected synchronized void destroyAllProducts()
	{
		store.clear();
		notifyAll();
	}

	/**
	 * Closes the warehouse. Attempting to get a product will return
	 * <code>null</code>.
	 * 
	 * @param b true if the warehouse should be closed.
	 */
	protected synchronized void setClosed(boolean b)
	{
		closed = b;
		notifyAll();
	}

	/**
	 * Retrieves a product from the warehouse. If one or more products are
	 * availabe then the first product is returned, else the consumer will be
	 * blocked until a product has become available. If the warehouse is closed,
	 * then the product will always, and immediately, be <code>null</code>.
	 * 
	 * @return An object representing a product, or <code>null</code> if the
	 *         warehouse is closed.
	 */
	public synchronized Object getProduct()
	{
		while( store.isEmpty() )
		{
			if( closed )
			{
				return null;
			}

			try
			{
				wait();
			}
			catch( InterruptedException e )
			{
				// We don't care.
			}
		}

		return store.remove(0);
	}

	/**
	 * Indicates if the warehouse contains any products.
	 * 
	 * @return true if the warehouse contains one or more products.
	 */
	public synchronized boolean hasProducts()
	{
		return !store.isEmpty();
	}

	/**
	 * Inidicates if the warehouse is closed.
	 * 
	 * @return true if the warehouse is closed.
	 */
	public synchronized boolean isClosed()
	{
		return closed;
	}
}