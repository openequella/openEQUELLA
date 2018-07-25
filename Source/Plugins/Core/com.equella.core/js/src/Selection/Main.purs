module Selection.Main where

import Prelude

import Effect (Effect)
import Polyfills (polyfill)

main :: Effect Unit
main = do
  polyfill