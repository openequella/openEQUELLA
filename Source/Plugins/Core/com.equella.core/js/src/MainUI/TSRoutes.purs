module OEQ.MainUI.TSRoutes where 

import Prelude

import Data.Function.Uncurried (Fn2)
import Dispatcher.React (ReactReaderT, getProps)
import Effect.Class (class MonadEffect, liftEffect)
import Effect.Uncurried (EffectFn1, runEffectFn1)
import OEQ.MainUI.Template (TemplateProps)
import React (Children, ReactClass, ReactElement, unsafeCreateElement)
import Unsafe.Coerce (unsafeCoerce)

type LinkPropsR r = (to :: LocationDescription|r)
type LinkProps = {|LinkPropsR ()}

type TemplateUpdate = ({|TemplateProps} -> {|TemplateProps})
type TemplateUpdateCB = EffectFn1 TemplateUpdate Unit

foreign import linkClass :: ReactClass {|LinkPropsR (children::Children)}

foreign import data LocationDescription :: Type

foreign import routes :: {
    "ViewItem" :: {
        to:: Fn2 String Int LocationDescription
    },
    "ThemeConfig" :: {
      path:: LocationDescription
    }
}

link :: LinkProps -> Array ReactElement -> ReactElement
link = unsafeCreateElement linkClass

toLocation :: String -> LocationDescription
toLocation = unsafeCoerce

runTemplateUpdate :: forall m r s. MonadEffect m => TemplateUpdate -> ReactReaderT {updateTemplate::TemplateUpdateCB|r} s m Unit
runTemplateUpdate f = do 
    {updateTemplate} <- getProps
    liftEffect $ runEffectFn1 updateTemplate f