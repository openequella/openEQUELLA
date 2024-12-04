/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.lti.consumers.api.interfaces;

import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.lti.consumers.service.LtiConsumerService;
import com.tle.web.api.baseentity.serializer.BaseEntitySerializer;
import com.tle.web.api.entity.resource.AbstractBaseEntityResource;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.lti.consumers.api.beans.LtiConsumerBean;
import com.tle.web.lti.consumers.api.serializer.LtiConsumerBeanSerializer;
import javax.inject.Inject;

@Bind(LtiConsumerResource.class)
public class LtiConsumerResourceImpl
    extends AbstractBaseEntityResource<LtiConsumer, BaseEntitySecurityBean, LtiConsumerBean>
    implements LtiConsumerResource {
  @Inject private LtiConsumerService ltiConsumerService;
  @Inject private LtiConsumerBeanSerializer ltiConsumerSerializer;

  @Override
  protected Node[] getAllNodes() {
    return new Node[] {Node.ALL_LTI_CONSUMERS};
  }

  @Override
  protected BaseEntitySecurityBean createAllSecurityBean() {
    return new BaseEntitySecurityBean();
  }

  @Override
  public AbstractEntityService<?, LtiConsumer> getEntityService() {
    return ltiConsumerService;
  }

  @Override
  protected BaseEntitySerializer<LtiConsumer, LtiConsumerBean> getSerializer() {
    return ltiConsumerSerializer;
  }

  @Override
  protected Class<?> getResourceClass() {
    return LtiConsumerResource.class;
  }
}
