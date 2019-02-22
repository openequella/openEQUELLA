# Developer Guides

openEQUELLA began life a long time ago in 2002 and as such has had fair share of code re-strucures over the years. As of version 6.6 the code mostly fits into two categories; the "new" architecture or the "legacy" architecture.

## Legacy architecture

- Written in Java (some Java 8 features)
- In house web framework called Sections for server side rendering with
  some facility for doing AJAX DOM updates somewhat transparently
- XML based extension declarations (JPF)
- Freemarker, JQuery, Boostrap
- Hibernate (3.6)
- JAX-RS based REST APIs (not used by web site)

## New architecure

- Java and Scala can be used
- Separate backend/frontend
  - REST APIs on backend
  - ReactJS for frontend
- Material UI React library
- Typescript/Purescript for frontend code
- Typesafe SQL tables/queries instead of hibernate magic
- Put extensions in compiled code instead of XML

---

95% of the openEQUELLA is still written with the legacy architecture but the idea is to leave that untouched until something major needs to be done to a particular area and only that area will be refactored.

All the guides will concentrate on the new architecture.

## Code layout

Originally EQUELLA was built out of several hundred "plugins" which each had their own classpath and dependencies.
However for a few reasons (lack of typesafety for core functionality, slowness of build system, complexity) the number of plugins was cut back to a minimum. Fore "core" functionality, rather than creating a completely new plugin and filling out the various XML extension points,
you should simply add your classes to an existing (or new) package inside one of a few select plugins:

Inside `Source/Plugins/Core`:

- `com.equella.core` - Anything related to the web site, frontend or backend.
- `com.equella.serverbase` - Classes that don't have an affiliation with the web, e.g. DB access, core services.
- `com.equella.base` - Classes shared between admin console and web site.
- `com.equella.admin` - Admin console.
- `com.equella.reporting` - Classes related to reporting with BIRT.

Usually `com.equella.core` is the place where new architecture classes will be placed.

## Guides

- [REST endpoint creation](restendpoint.md)
- [Using security](security.md)
- [React based web pages](reactjs.md)
- [Scala server side code](scaladb.md)
- [Unexpected errors and notifications](clienterrors.md)
- [Guice integration](guice.md)
