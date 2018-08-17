module Utils.Interop where

import Data.Maybe (Maybe(..))
import Data.Nullable (Nullable, toNullable)

nullAny :: forall a. Nullable a
nullAny = toNullable Nothing
