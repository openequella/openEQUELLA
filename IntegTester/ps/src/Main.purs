module Main where

import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE, log)
import DOM (DOM)
import DOM.HTML (window)
import DOM.HTML.Location (pathname)
import DOM.HTML.Window (location)
import Data.Array (head, mapMaybe)
import Data.StrMap (StrMap, toUnfoldable)
import Data.Tuple (Tuple(..))
import Dispatcher.React (renderWithSelector)
import IntegTester (integ)
import Prelude hiding (div)

foreign import postValues :: StrMap (Array String)

postFirst :: Array (Tuple String String)
postFirst = mapMaybe (\(Tuple n vs) -> Tuple n <$> head vs) $ toUnfoldable postValues

main :: forall t40.
  Eff ( dom :: DOM
    , console :: CONSOLE
               | t40
               )
               Unit
main = void $ do
    path <- window >>= location >>= pathname
    log path
    renderWithSelector "#app" $ case path of
        _ ->  (integ postFirst)
