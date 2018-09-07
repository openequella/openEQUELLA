module Utils.Interop where

import Data.Maybe (Maybe(..))
import Data.Nullable (Nullable, toNullable)
import Unsafe.Coerce (unsafeCoerce)

nullAny :: forall a. Nullable a
nullAny = toNullable Nothing

notNull :: forall a. a -> Nullable a 
notNull = unsafeCoerce