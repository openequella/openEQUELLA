package com.tle.web.api.staging.interfaces;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Common response documentation for endpoints that create a new staging area. */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
  @ApiResponse(
      code = 201,
      message = "Staging area created successfully",
      responseHeaders = {
        @ResponseHeader(
            name = "x-eps-stagingid",
            description = "The UUID of the newly created staging area",
            response = String.class),
        @ResponseHeader(
            name = "Location",
            description = "The URI of the newly created staging area",
            response = String.class)
      })
})
public @interface StagingAreaCreatedResponse {}
