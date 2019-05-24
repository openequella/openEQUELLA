module Bridge where 


import Prelude

import Data.Either (fromRight)
import Effect (Effect)
import OEQ.Data.LegacyContent (LegacyURI, legacyRoute)
import OEQ.MainUI.SearchPage (searchPageClass)
import OEQ.MainUI.SettingsPage (settingsPageClass)
import OEQ.UI.LegacyContent (legacyContentClass)
import OEQ.UI.Security.ACLEditor (aclEditorClass)
import Partial.Unsafe (unsafePartial)
import React (ReactClass)
import Routing (match)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {
    "AclEditor" :: forall p. ReactClass p,
    "LegacyContent" :: forall p. ReactClass p,
    "SettingsPage" :: forall p. ReactClass p,
    "SearchPage" :: forall p. ReactClass p,
    legacyUri :: String -> LegacyURI
}

tsBridge :: Bridge 
tsBridge = {
    "AclEditor" : unsafeCoerce aclEditorClass,
    "LegacyContent" : unsafeCoerce legacyContentClass,
    "SettingsPage" : unsafeCoerce settingsPageClass,
    "SearchPage" : unsafeCoerce searchPageClass,
    legacyUri: \uri -> unsafePartial $ fromRight $ match legacyRoute uri
} 

foreign import setupBridge :: Bridge -> Effect Unit 

main :: Effect Unit
main = setupBridge tsBridge