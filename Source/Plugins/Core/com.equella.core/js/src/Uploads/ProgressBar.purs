module Uploads.ProgressBar where

import Prelude hiding (div)

import Control.Monad.Eff (Eff)
import Data.Nullable (Nullable, toNullable)
import React (ReactElement, ReactProps, ReactRefs, ReactThis, ReadOnly, Ref, createClass, createFactory, getProps, readRef, spec, writeRef)
import React.DOM (div)
import React.DOM.Props (className, withRef)

foreign import runProgress :: forall e. Nullable Ref -> Int -> Eff e Unit

progressBar :: {progress::Int} -> ReactElement
progressBar = createFactory $ createClass $ (spec {} (\this -> render this <$> getProps this)) {
  componentDidMount = mounted,
  componentDidUpdate = updated}
  where
  updateProgress :: forall eff. ReactThis {progress::Int} {} -> Eff
    ( props :: ReactProps
    , refs :: ReactRefs ReadOnly
    | eff
    ) Unit
  updateProgress this = do
    {progress} <- getProps this
    el <- readRef this "main"
    runProgress (toNullable el) progress
  mounted this = updateProgress this
  updated this prevProps state = updateProgress this
  render this {progress} = div [
    className "progress-bar",
    withRef (\ref -> writeRef this "main" ref)
  ] [ ]
