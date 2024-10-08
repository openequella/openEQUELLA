package com.tle.common.util

import java.security.SecureRandom

object StringUtils {

  private val secureRandom = SecureRandom.getInstanceStrong

  /**
    * Generates a string of random bytes represented as hexadecimal values.
    *
    * @param length number of random bytes
    * @return a string which is twice the length of `length` with each two characters representing
    *         one byte
    */
  def generateRandomHexString(length: Int): String =
    Range(0, length).map(_ => "%02x".format(secureRandom.nextInt(255))).mkString
}
