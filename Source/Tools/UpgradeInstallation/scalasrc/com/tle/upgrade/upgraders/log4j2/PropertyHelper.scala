package com.tle.upgrade.upgraders.log4j2

import java.util.Properties
import scala.util.{Failure, Success, Try}

object PropertyHelper {

  /**
    * Get a String value from the property configurations by the supplied key.
    *
    * @param key Key of a property.
    * @param props The property configuration to be read.
    * @return An Option of String.
    */
  def readProperty(key: String, props: Properties): Option[String] =
    Option(props.getProperty(key))
      .filter(_.nonEmpty)

  /**
    * Get a boolean value from the property configurations by the supplied key.
    *
    * @param key Key of a property.
    * @param props The property configuration to be read.
    * @return An Option of Boolean.
    */
  def readBooleanProperty(key: String, props: Properties): Option[Boolean] =
    readProperty(key, props).map(s =>
      Try {
        s.toBoolean
      } match {
        case Success(value) => value
        case Failure(_)     => false
    })

  /**
    * Get an Int value from the property configurations by the supplied key.
    *
    * @param key Key of a property.
    * @param props The property configuration to be read.
    * @return An Option of Int.
    */
  def readIntProperty(key: String, props: Properties): Option[Int] =
    readProperty(key, props)
      .map(s =>
        Try {
          s.toInt
        } match {
          case Success(value) => value
          case Failure(_)     => 0
      })
}
