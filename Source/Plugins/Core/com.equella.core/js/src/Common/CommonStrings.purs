module Common.CommonStrings where 

import Common.Strings (languageStrings)

commonString :: { action :: { save :: String
            , cancel :: String
            , undo :: String
            , add :: String
            , ok :: String
            , discard :: String
            , select :: String
            , delete :: String
            , search :: String
            , clear :: String
            , close :: String
            , dismiss :: String
            , browse :: String
            , apply :: String
            , resettodefault :: String
            , revertchanges :: String
            , register :: String
            }
, users :: String
, groups :: String
, roles :: String
}
commonString = languageStrings.common 

commonAction :: { save :: String
, cancel :: String
, undo :: String
, add :: String
, ok :: String
, discard :: String
, select :: String
, delete :: String
, search :: String
, clear :: String
, close :: String
, dismiss :: String
, browse :: String
, apply :: String
, resettodefault :: String
, revertchanges :: String
, register :: String
}
commonAction = commonString.action