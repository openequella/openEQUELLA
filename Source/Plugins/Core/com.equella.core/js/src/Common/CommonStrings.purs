module Common.CommonStrings where 

foreign import commonString :: { 
  action :: { save :: String
            , cancel :: String
            , undo :: String
            , add :: String
            , ok :: String
            , discard :: String
            , select :: String
            , delete :: String
            }
  , users :: String
  , groups :: String
  , roles :: String
}

commonAction = commonString.action