package io.github.openequella

import zio._

package object ziohelpers {
  def zioToEither[T](z: ZIO[Any, Throwable, T]): Either[Throwable, T] = zio.Unsafe.unsafe {
    implicit unsafe =>
      Runtime.default.unsafe.run(z).toEither
  }
}
