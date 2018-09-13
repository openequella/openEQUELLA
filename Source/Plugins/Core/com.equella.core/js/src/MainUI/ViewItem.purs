module OEQ.MainUI.ViewItem where 

import Prelude hiding (div)
import Data.Maybe (Maybe(..))
import Debug.Trace (traceM)
import Dispatcher (affAction)
import Dispatcher.React (renderer)
import OEQ.Data.Error (ErrorResponse)
import Effect.Uncurried (mkEffectFn1)
import OEQ.UI.ItemSummary.ViewItem (viewItem)
import MaterialUI.Styles (withStyles)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div)
import React.DOM.Props (className)
import OEQ.MainUI.Template (template', templateDefaults)
import OEQ.Utils.Interop (notNull)

data Command = Errored ErrorResponse

viewItemPage :: {uuid::String, version::Int} -> ReactElement
viewItemPage = unsafeCreateLeafElement $ withStyles styles $  component "ViewItemPage" \this -> do 
  let 
    d = eval >>> affAction this
    render {state:{}, props:{classes, uuid,version}} = 
      template' (templateDefaults "Resource summary") { fixedViewPort = notNull true} [ 
        div [className classes.main ] [
          viewItem {uuid,version, onError: mkEffectFn1 traceM, onSelect: Nothing} 
        ]
      ]
    eval = case _ of 
      Errored _ -> pure unit
  pure {render: renderer render this, state:{content:Nothing}}
  where 
    styles t = {
      main: {
        margin: t.spacing.unit * 2
      }
    }
