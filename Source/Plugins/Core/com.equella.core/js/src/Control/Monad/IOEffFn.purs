module Control.Monad.IOEffFn where

import Control.Monad.Aff (Fiber)
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Uncurried (EffFn1, EffFn2, mkEffFn1, runEffFn1, runEffFn2)
import Prelude (($))
import Unsafe.Coerce (unsafeCoerce)

newtype IOFiber a = IOFiber (forall e. Fiber e a)
newtype IOFn2 a b c = IOFn2 (forall e. EffFn2 e a b c)
newtype IOFn1 a b = IOFn1 (forall e. EffFn1 e a b)

mkIOFiber :: forall e a. (Fiber e a) -> IOFiber a
mkIOFiber = unsafeCoerce

mkIOFn1 :: forall e a b. (a -> Eff e b) -> IOFn1 a b
mkIOFn1 f = unsafeCoerce $ mkEffFn1 f

runIOFn1 :: forall e a b. IOFn1 a b -> a -> Eff e b
runIOFn1 (IOFn1 effFn1) = runEffFn1 effFn1

runIOFn2 :: forall e a b c. IOFn2 a b c -> a -> b -> Eff e c
runIOFn2 (IOFn2 effFn2) = runEffFn2 effFn2

runIOFiber :: forall e a. IOFiber a -> Fiber e a
runIOFiber (IOFiber f) = f
