/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import cats.data.{Kleisli, StateT}
import cats.effect.LiftIO
import cats.syntax.applicative._
import cats.~>
import fs2.Stream
import io.doolse.simpledba.WriteOp
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._

package object db {
  type DB[A] = Kleisli[JDBCIO, UserContext, A]

  val getContext: DB[UserContext] = Kleisli.ask

  val dbLiftIO = LiftIO[DB]

  def dbAttempt[A](db: DB[A]): DB[Either[Throwable, A]] = Kleisli { uc =>
    StateT { con =>
      db.run(uc).runA(con).attempt.map(e => (con, e))
    }
  }

  def withContext[A](f: UserContext => A): DB[A] =
    Kleisli(uc => f(uc).pure[JDBCIO])

  val flushDB: Stream[JDBCIO, WriteOp] => DB[Unit] =
    a => Kleisli.liftF(a.flush.compile.drain)

  val translateDB = new (JDBCIO ~> DB) {
    override def apply[A](fa: JDBCIO[A]): DB[A] = Kleisli.liftF(fa)
  }

  def dbStream[A](f: UserContext => Stream[JDBCIO, A]): Stream[DB, A] =
    Stream.eval[DB, UserContext](Kleisli.ask[JDBCIO, UserContext]).flatMap { uc =>
      f(uc).translate(translateDB)
    }

  def toJDBCStream[A](stream: Stream[DB, A]): DB[Stream[JDBCIO, A]] = getContext.map { ctx =>
    stream.translate(new (DB ~> JDBCIO) {
      override def apply[A](fa: DB[A]): JDBCIO[A] = fa.run(ctx)
    })
  }
}
