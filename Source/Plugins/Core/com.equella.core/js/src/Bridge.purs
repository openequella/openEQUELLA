module Bridge where 

import Prelude

import Data.Nullable (Nullable, toNullable)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Foreign (Foreign)
import OEQ.MainUI.Routes (Route(..), forcePushRoute, logoutRoute, matchRoute, pushRoute, routeHref, routeURI, setPreventNav, userPrefsRoute)
import OEQ.UI.Security.ACLEditor (aclEditorClass)
import React (ReactClass)
import React.SyntheticEvent (SyntheticMouseEvent)
import Unsafe.Coerce (unsafeCoerce)

type Bridge = {
    routes :: Foreign,
    router :: Route -> {href::String, onClick :: EffectFn1 SyntheticMouseEvent Unit},
    routeURI :: Route -> String, 
    pushRoute ::EffectFn1 Route Unit,
    forcePushRoute :: EffectFn1 Route Unit,
    setPreventNav :: EffectFn1 (EffectFn1 Route Boolean) Unit,
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
    routeURI: routeURI,
    pushRoute: mkEffectFn1 pushRoute,
    matchRoute: toNullable <<< matchRoute,
    setPreventNav: mkEffectFn1 setPreventNav,
    forcePushRoute: mkEffectFn1 forcePushRoute,
    "AclEditor" : unsafeCoerce aclEditorClass
} 
