module Uploads.ProgressBar where

import Prelude hiding (div)

import Data.Maybe (Maybe(..))
import Dispatcher.React (propsRenderer, saveRef, withRef)
import Effect (Effect)
import Effect.Ref (new)
import Effect.Uncurried (runEffectFn1)
import React (ReactElement, ReactRef, component, getProps, unsafeCreateLeafElement)
import React.DOM (div)
import React.DOM.Props (className, ref)

foreign import runProgress :: Int -> ReactRef -> Effect Unit

progressBar :: {progress::Int} -> ReactElement
progressBar = unsafeCreateLeafElement $ component "ProgressBar" $ \this -> do 
  mainRef <- new Nothing
  let updateProgress = do
          {progress} <- getProps this
          withRef mainRef $ runProgress progress
      render {progress} = div [
        className "progress-bar",
        ref $ runEffectFn1 $ saveRef mainRef
      ] [ ]
  pure {render: propsRenderer render this, componentDidMount: updateProgress, componentDidUpdate: \_ _ _ -> updateProgress}
