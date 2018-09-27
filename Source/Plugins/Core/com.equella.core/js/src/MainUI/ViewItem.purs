module OEQ.MainUI.ViewItem where 

import Prelude hiding (div)

import Data.Maybe (Maybe(..))
import Data.Nullable (toNullable)
import Dispatcher (affAction)
import Dispatcher.React (modifyState, renderer)
import Effect.Uncurried (mkEffectFn1)
import MaterialUI.Styles (withStyles)
import OEQ.Data.Error (ErrorResponse)
import OEQ.MainUI.Template (template', templateDefaults)
import OEQ.UI.ItemSummary.ViewItem (viewItem)
import OEQ.Utils.Interop (notNull)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div)
import React.DOM.Props (className)

data Command = Errored ErrorResponse

type State = {
  error :: Maybe ErrorResponse
}

viewItemPage :: {uuid::String, version::Int} -> ReactElement
viewItemPage = unsafeCreateLeafElement $ withStyles styles $  component "ViewItemPage" \this -> do 
  let 
    d = eval >>> affAction this
    render {state:{error}, props:{classes, uuid,version}} = 
      template' (templateDefaults "Resource summary") { fixedViewPort = notNull true, errorResponse = toNullable error} [ 
        div [className classes.main ] [
          viewItem {uuid,version, onError: mkEffectFn1 $ d <<< Errored, onSelect: Nothing} 
        ]
      ]
    eval = case _ of 
      Errored e -> modifyState _ {error = Just e}
  pure {render: renderer render this, state:{error:Nothing}}
  where 
    styles t = {
      main: {
        margin: t.spacing.unit * 2
      }
    }
