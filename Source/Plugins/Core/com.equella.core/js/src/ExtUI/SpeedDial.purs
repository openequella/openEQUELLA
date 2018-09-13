module ExtUI.SpeedDial where 

import React (ReactClass, ReactElement, unsafeCreateElement, unsafeCreateLeafElement)

foreign import speedDialClass :: forall p. ReactClass p
foreign import speedDialIconClass :: forall p. ReactClass p
foreign import speedDialActionClass :: forall p. ReactClass p

speedDialU :: forall p. {|p} -> Array ReactElement -> ReactElement
speedDialU = unsafeCreateElement speedDialClass

speedDialIconU :: forall p. {|p} -> Array ReactElement -> ReactElement
speedDialIconU = unsafeCreateElement speedDialIconClass

speedDialActionU :: forall p. {|p} -> ReactElement
speedDialActionU = unsafeCreateLeafElement speedDialActionClass