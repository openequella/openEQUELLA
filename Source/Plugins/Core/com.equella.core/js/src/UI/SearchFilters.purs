module OEQ.UI.SearchFilters where 

import Prelude hiding (div)

import Dispatcher.React (propsRenderer)
import MaterialUI.Properties (className, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (subheading)
import MaterialUI.Typography (typography)
import React (ReactElement, childrenToArray, component, unsafeCreateElement)
import React.DOM (div, text)
import React.DOM.Props as P

filterSection :: {name::String, icon::ReactElement} -> Array ReactElement -> ReactElement
filterSection = unsafeCreateElement $ withStyles styles $ component "Filter" $ \this -> do
  let render {icon, name,classes,children:c} = 
        div [P.className classes.container] $ [
          typography [variant subheading, className classes.title] [text name ]
        ] <> childrenToArray c
  pure {render: propsRenderer render this}
  where 
  styles theme = {
    container: {
      padding: theme.spacing.unit * 2
    }, 
    title: {
      marginBottom: theme.spacing.unit * 2
    }
  }
