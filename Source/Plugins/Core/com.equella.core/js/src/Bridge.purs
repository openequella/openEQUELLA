module Bridge where 


import Prelude

import Effect (Effect)
import OEQ.MainUI.SearchPage (searchPageClass)
import OEQ.MainUI.SettingsPage (settingsPageClass)
import OEQ.UI.Security.ACLEditor (aclEditorClass)
import React (ReactClass)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {
    "AclEditor" :: forall p. ReactClass p,
    "SettingsPage" :: forall p. ReactClass p,
    "SearchPage" :: forall p. ReactClass p
}

tsBridge :: Bridge 
tsBridge = {
    "AclEditor" : unsafeCoerce aclEditorClass,
    "SettingsPage" : unsafeCoerce settingsPageClass,
    "SearchPage" : unsafeCoerce searchPageClass
} 

foreign import setupBridge :: Bridge -> Effect Unit 

main :: Effect Unit
main = setupBridge tsBridge