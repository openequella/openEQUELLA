module SearchFilters where 

import Prelude hiding (div)

import Common.Icons (expandMoreIcon)
import Dispatcher.React (ReactChildren(..), ReactProps(..), createComponent)
import MaterialUI.ExpansionPanel (expansionPanel)
import MaterialUI.ExpansionPanelDetails (expansionPanelDetails, expansionPanelDetails_)
import MaterialUI.ExpansionPanelSummary (expandIcon, expansionPanelSummary, expansionPanelSummary_)
import MaterialUI.Properties (className, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (subheading)
import MaterialUI.Typography (typography)
import React (ReactElement, createElement)
import React.DOM (div, div', text)
import React.DOM as D
import React.DOM.Props as P

filterSection :: {name::String, icon::ReactElement} -> Array ReactElement -> ReactElement
filterSection = createElement (withStyles styles $ createComponent {} render unit)
  where 
  styles theme = {
    container: {
      padding: theme.spacing.unit * 2
    }, 
    title: {
      marginBottom: theme.spacing.unit * 2
    }
  }
  render _ (ReactProps {icon, name,classes}) (ReactChildren c) = 
    div [P.className classes.container] $ [
      typography [variant subheading, className classes.title] [text name ]
    ] <> c
    -- expansionPanel [] [
    --   expansionPanelSummary [expandIcon expandMoreIcon] [ 
    --     icon,
    --     typography [className classes.title] [text name ]
    --   ],
    --   expansionPanelDetails_ c
    -- ]
