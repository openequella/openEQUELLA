module EQUELLA.Environment where

foreign import baseUrl :: String

foreign import prepLangStrings :: forall r. {prefix::String, strings :: (Record r)} -> Record r
