/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core

import cats.data.Kleisli
import io.doolse.simpledba.jdbc._
import fs2.Stream
import cats.~>

package object db {
  type DB[A] = Kleisli[JDBCIO, UserContext, A]

  val translate = new (JDBCIO ~> DB) {
    override def apply[A](fa: JDBCIO[A]): DB[A] = Kleisli.liftF(fa)
  }

  def dbStream[A](f: UserContext => Stream[JDBCIO, A]): Stream[DB, A] = Stream.eval[DB, UserContext](Kleisli.ask[JDBCIO, UserContext]).flatMap {
    uc => f(uc).translate[DB](translate)
  }
}
