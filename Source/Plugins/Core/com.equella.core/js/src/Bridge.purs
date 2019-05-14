module Bridge where 

import Prelude

import Data.Nullable (Nullable, toNullable)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Foreign (Foreign)
import OEQ.MainUI.Routes (Route(..), forcePushRoute, logoutRoute, matchRoute, routeHref, userPrefsRoute)
import OEQ.MainUI.Template (templateClass)
import OEQ.UI.Security.ACLEditor (aclEditorClass)
import React (ReactClass)
import React.SyntheticEvent (SyntheticMouseEvent)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {
    routes :: Foreign,
    router :: Route -> {href::String, onClick :: EffectFn1 SyntheticMouseEvent Unit},
    forcePushRoute :: EffectFn1 Route Unit, 
    matchRoute :: String -> Nullable Route,
    "AclEditor" :: forall p. ReactClass p
}

tsBridge :: Bridge 
tsBridge = {
    routes : unsafeCoerce $ {
        "CoursesPage": CoursesPage, 
        "CourseEdit": CourseEdit, 
        "NewCourse": NewCourse,
        "SettingsPage": SettingsPage,
        "CloudProviderListPage" : CloudProviderListPage,
        "Logout": logoutRoute,
        "UserPrefs": userPrefsRoute
        },
    router : routeHref,
    matchRoute: toNullable <<< matchRoute,
    forcePushRoute: mkEffectFn1 forcePushRoute,
    "AclEditor" : unsafeCoerce aclEditorClass
} 
