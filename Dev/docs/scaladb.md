# Scala DB monad

```scala
import io.doolse.simpledba.jdbc._

case class UserContext(inst: Institution, user: UserState, ds: DataSource)

type DB[A] = Kleisli[JDBCIO, UserContext, A]
```

TODO - Explain
