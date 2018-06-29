# Developer Guides

openEQUELLA began life a long time ago in 2002 and as such has had fair share of code re-strucures over the years. As of version 6.6 the code mostly fits into two categories; the "new" architecture or the "legacy" architecture.

## Legacy architecture

* Written in Java (some Java 8 features)
* In house web framework called Sections for server side rendering with some facicility for doing AJAX updates
* XML based extension declarations (JPF)
* Freemarker, JQuery, Boostrap
* Hibernate (3.6)
* JAX-RS based REST APIs (not used by web site)

## New architecure

* Java and Scala can be used
* Separate backend/frontend
  * REST APIs on backend
  * ReactJS for frontend
* Material UI React library 
* Typescript/Purescript for frontend code
* Typesafe SQL tables/queries instead of hibernate magic
* Put extensions in typesafe code instead of XML

---

95% of the openEQUELLA is still written with the legacy architecture but the idea is to leave that untouched until something major needs to be done to a particular area and only that area will be refactored.

All the guides will concentrate on the new architecture.

## Guides

* REST endpoint creation - TODO
* React based web pages - TODO
* Using security - TODO

