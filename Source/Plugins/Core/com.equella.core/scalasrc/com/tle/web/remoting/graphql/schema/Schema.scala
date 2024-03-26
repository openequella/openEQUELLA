package com.tle.web.remoting.graphql.schema

import caliban.GraphQL
import com.tle.core.guice.Bind

import javax.inject.{Inject, Singleton}

@Bind
@Singleton
class Schema {
  @Inject private var tleUserSchema: TLEUserSchema = _

  def getFullApi: GraphQL[Any] = {
    // NOTE: The idea here is to combine the APIs from all the different schemas using the
    // |+| operator. This is a placeholder for now.
    // See more: https://ghostdogpr.github.io/caliban/faq/#i-have-more-than-22-fields-in-my-query-i-can-t-create-a-case-class-for-it
    // Maybe we should have all API provider classes extend a common trait and then use that trait
    // to combine the APIs - by finding them all with introspection. But then the dependency injection
    // wont work. :thinking:
    tleUserSchema.getApi
  }
}
