package com.tle.core.remoting;

import com.tle.beans.cloudproviders.CloudControlDefinition;
import java.util.List;

public interface CloudProviderAdminService {

  List<CloudControlDefinition> listControls();
}
