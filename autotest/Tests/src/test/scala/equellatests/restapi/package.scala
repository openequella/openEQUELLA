package equellatests

import cats.free.Free

package object restapi {
  type ERest[A] = Free[ERestA, A]

}
