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

package com.tle.web.api.cloudprovider
import com.tle.core.cloudproviders.{
  CloudProviderDB,
  CloudProviderRegistration,
  CloudProviderRegistrationResponse
}
import com.tle.web.api.ApiHelper
import io.swagger.annotations.{Api, ApiOperation}
import javax.ws.rs.core.Response
import javax.ws.rs.{POST, Path, PathParam, QueryParam}

@Api("Cloud Providers")
@Path("cloudprovider")
class CloudProviderApi {

  @POST
  @Path("register/{regtoken}")
  @ApiOperation(value = "Register a cloud provider",
                response = classOf[CloudProviderRegistrationResponse])
  def register(@PathParam("regtoken") regtoken: String,
               registration: CloudProviderRegistration): Response = {
    ApiHelper.runAndBuild {
      CloudProviderDB.register(registration).map { validatedInstance =>
        ApiHelper.validationOrEntity(
          validatedInstance.map(inst => CloudProviderRegistrationResponse(inst, "http://")))
      }
    }
  }
}
