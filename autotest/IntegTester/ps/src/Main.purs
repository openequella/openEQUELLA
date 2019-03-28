module Main where

import Prelude hiding (div)

import Data.Array (head, mapMaybe)
import Data.Maybe (fromJust)
import Data.Tuple (Tuple(..))
import Effect (Effect)
import Foreign.Object (Object)
import Foreign.Object as Object
import IntegTester (integ)
import Partial.Unsafe (unsafePartial)
import ReactDOM (render)
import Web.DOM.Document (toNonElementParentNode)
import Web.DOM.NonElementParentNode (getElementById)
import Web.HTML (window)
import Web.HTML.HTMLDocument (toDocument)
import Web.HTML.Location (pathname)
import Web.HTML.Window (document, location)

foreign import postValues :: Object (Array String)

postFirst :: Array (Tuple String String)
postFirst = mapMaybe (\(Tuple n vs) -> Tuple n <$> head vs) $ Object.toUnfoldable postValues

main :: Effect Unit
main = unsafePartial $ void $ do
    w <- window
    doc <- document w
    path <- location w >>= pathname
    rootElem <- fromJust <$> getElementById "app" (toNonElementParentNode $ toDocument doc)
    flip render rootElem $ case path of
        _ ->  (integ postFirst)
