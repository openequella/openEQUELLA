package com.tle.web.remoting.graphql.provider

case class ProviderError(message: String, cause: Option[Throwable] = None)
