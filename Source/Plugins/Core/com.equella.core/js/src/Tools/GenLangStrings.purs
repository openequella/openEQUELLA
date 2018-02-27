module Tools.GenLangStrings where

import Prelude

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE, log)
import Control.Plus (empty)
import Data.Argonaut (encodeJson, stringify)
import Data.List (List, singleton)
import Data.Record (get)
import Data.StrMap (fromFoldable)
import Data.Symbol (class IsSymbol, SProxy(..), reflectSymbol)
import Data.Tuple (Tuple(..))
import SearchPage (rawStrings) as SearchPage
import Settings.UISettings (rawStrings) as UISettings
import SettingsPage (rawStrings) as SettingsPage
import Type.Row (class RowToList, Cons, Nil, RLProxy(..))

class ConvertToStrings a where
  genStrings :: String -> a -> List (Tuple String String)

instance nilStrings :: ConvertToStrings (Tuple (RLProxy Nil) (Record r)) where
  genStrings _ _ = empty

instance consStrings :: (IsSymbol hs, RowCons hs ht r' r,
    ConvertToStrings ht,
    ConvertToStrings (Tuple (RLProxy t) (Record r)))
  => ConvertToStrings (Tuple (RLProxy (Cons hs ht t)) (Record r)) where
  genStrings pfx (Tuple _ r) =
    genStrings (pfx <> ".") (Tuple (reflectSymbol (SProxy :: SProxy hs)) (get (SProxy :: SProxy hs) r))
    <> (genStrings pfx (Tuple (RLProxy :: RLProxy t) r))

instance record :: (RowToList a out, ConvertToStrings (Tuple (RLProxy out) (Record a)))
  => ConvertToStrings (Record a) where
  genStrings pfx r = genStrings pfx (Tuple (RLProxy :: RLProxy out) r)

instance stringString :: ConvertToStrings String where
  genStrings pfx a = singleton $ Tuple pfx a

instance prefixed :: ConvertToStrings a => ConvertToStrings (Tuple String a) where
  genStrings pfx (Tuple prefix r) = genStrings (pfx<>prefix) r

main :: forall eff. Eff ( console :: CONSOLE | eff) Unit
main = do
  log $ stringify $ encodeJson $ fromFoldable $
    genStrings "" SearchPage.rawStrings <>
    genStrings "" UISettings.rawStrings <>
    genStrings "" SettingsPage.rawStrings
