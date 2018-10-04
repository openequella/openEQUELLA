# Important classes

## Hibernate
* `com.tle.core.hibernate.HibernateFactoryServiceImpl`
* extension `com.tle.core.hibernate:domainObjects`

## Freetext search
* `com.tle.core.freetext.service.impl.FreeTextServiceImpl`
* `com.tle.core.freetext.indexer.IndexingExtension`
* extension - `com.tle.core.freetext:indexingExtension`

## Embedded tomcat
* `com.tle.tomcat.service.impl.TomcatServiceImpl`
* `com.tle.web.dispatcher.WebFilter`

## I18N
* `com.tle.common.i18n.LangUtils`
* `com.tle.beans.entity.LanguageBundle`
* `com.tle.common.i18n.CurrentLocale`

## Item editing

* `com.tle.core.item.service.impl.ItemServiceImpl`
* `com.tle.core.item.operations.WorkflowOperation`  

## Sections

* `com.tle.web.sections.registry.SectionsServlet`
* `com.tle.web.sections.registry.SectionsControllerImpl`
* extensions `com.tle.web.sections`
  * `sectionTree`
  * `section`

## User management

* `com.tle.core.services.user.impl.UserServiceImpl`
* `com.tle.common.usermanagement.user.UserState`
* `com.tle.plugins.ump.UserDirectory`
