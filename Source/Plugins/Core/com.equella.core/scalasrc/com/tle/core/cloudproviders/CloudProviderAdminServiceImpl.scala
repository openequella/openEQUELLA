package com.tle.core.cloudproviders

import java.util

import com.tle.beans.cloudproviders.CloudControlDefinition
import com.tle.core.guice.Bind
import com.tle.core.remoting.CloudProviderAdminService

import scala.collection.JavaConverters._

@Bind(classOf[CloudProviderAdminService])
class CloudProviderAdminServiceImpl extends CloudProviderAdminService {
  override def listControls: util.List[CloudControlDefinition] =
    List(CloudControlDefinition("My control", "/icons/control.gif"): CloudControlDefinition).asJava
}
