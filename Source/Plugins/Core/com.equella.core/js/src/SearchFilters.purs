module SearchFilters where

import Prelude

import Dispatcher.React (ReactChildren(..), ReactProps(..), createComponent)
import MaterialUI.Properties (variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (subheading)
import MaterialUI.Typography (typography)
import React (ReactElement, createElement)
import React.DOM as D
import React.DOM.Props as DP

filterSection :: {name::String} -> Array ReactElement -> ReactElement
filterSection = createElement (withStyles styles $ createComponent {} render unit)
  where
  styles theme = {
    container: {
      padding: theme.spacing.unit
    }
  }
  render _ (ReactProps {name,classes}) (ReactChildren c) = D.div [DP.className classes.container] $
      (pure $ typography [variant subheading] [ D.text name ]) <> c
