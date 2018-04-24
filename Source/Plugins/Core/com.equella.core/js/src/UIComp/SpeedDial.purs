module UIComp.SpeedDial where 

import React (ReactClass, ReactElement, createElement, createFactory)

foreign import speedDialClass :: forall p. ReactClass p
foreign import speedDialIconClass :: forall p. ReactClass p
foreign import speedDialActionClass :: forall p. ReactClass p

speedDialU :: forall p. p -> Array ReactElement -> ReactElement
speedDialU = createElement speedDialClass

speedDialIconU :: forall p. p -> Array ReactElement -> ReactElement
speedDialIconU = createElement speedDialIconClass

speedDialActionU :: forall p. p -> ReactElement
speedDialActionU = createFactory speedDialActionClass