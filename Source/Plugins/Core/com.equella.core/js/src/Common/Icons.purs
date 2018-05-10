module Common.Icons where 

import MaterialUI.Icon (icon_)
import React (ReactElement)
import React.DOM (text)

userIconName :: String
userIconName = "person"

groupIconName :: String
groupIconName = "people"
    
roleIconName :: String
roleIconName = "people"

userIcon :: ReactElement
userIcon = icon_ [text userIconName]

groupIcon :: ReactElement
groupIcon = icon_ [text groupIconName]

roleIcon :: ReactElement
roleIcon = icon_ [text roleIconName]

expandMoreIcon :: ReactElement
expandMoreIcon = icon_ [text "expand_more"]

searchIcon :: ReactElement
searchIcon = icon_ [ text "search"]