package com.tle.admin.service

import io.github.openequella.graphql.api.ApiError

class ClientRequestException(message: String, apiErrors: List[ApiError])
    extends RuntimeException(message) {
  def getApiErrors: List[ApiError] = apiErrors
}
