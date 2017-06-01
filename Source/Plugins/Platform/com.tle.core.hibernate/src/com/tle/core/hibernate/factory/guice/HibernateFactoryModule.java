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

package com.tle.core.hibernate.factory.guice;

import com.tle.core.config.guice.PropertiesModule;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.type.ImmutableHibernateXStreamType;

@SuppressWarnings("nls")
public class HibernateFactoryModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/hibernate.properties";
	}

	@Override
	protected void configure()
	{
		requestStaticInjection(ImmutableHibernateXStreamType.class);
		bindProp("hibernate.connection.driver_class");
		bindNewInstance("hibernate.dialect", ExtendedDialect.class);
		bindProp("hibernate.connection.username");
		bindProp("hibernate.connection.password");
		bindProp("hibernate.connection.url");
	}
}
