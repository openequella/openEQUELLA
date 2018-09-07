module StdViewItem where 

import Prelude
import Prelude hiding (div)

import AjaxRequests (ErrorResponse)
import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import Debug.Trace (traceM)
import Dispatcher (affAction)
import Dispatcher.React (renderer)
import Effect.Uncurried (mkEffectFn1)
import ItemSummary.ViewItem (viewItem)
import MaterialUI.Paper (paper, paper_)
import MaterialUI.Properties (style)
import MaterialUI.Styles (withStyles)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div)
import React.DOM.Props (className)
import Template (template, template', templateDefaults)
import Utils.Interop (notNull)

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
