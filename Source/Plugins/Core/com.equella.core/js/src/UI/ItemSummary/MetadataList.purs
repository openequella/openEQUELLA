module OEQ.UI.ItemSummary.MetadataList where 


import Prelude

import MaterialUI.Styles (withStyles)
import OEQ.Utils.Interop (nullAny)
import React (ReactElement, statelessComponent, unsafeCreateLeafElement)
import React.DOM (text)
import MaterialUI.Enums as E
import MaterialUI.ListItem (listItem)
import MaterialUI.Typography (typography)
import Record (merge)
import Unsafe.Coerce (unsafeCoerce)

data MetaValue = HTML String | React ReactElement | Text String
type MetaEntry = {key :: String, title::String, value :: MetaValue}

metaEntry :: MetaEntry -> ReactElement
metaEntry = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
 where 
 valueProps = {key:"value", component: "span", color: E.textSecondary}
 render {title,value,classes} = listItem {classes: {default: classes.attachmentMeta}} [ 
    typography {key:"name", component: "span", className: classes.metaTitle} [text title],
    case value of 
      HTML h -> typography (merge valueProps {dangerouslySetInnerHTML: {__html:h}} ) (unsafeCoerce nullAny)
      Text t -> typography valueProps [ text t ]
      React r -> typography valueProps [ r ]
 ] 
 styles t = {
    attachmentMeta: {
      padding: 0
    }, 
    metaTitle: {
      paddingRight: t.spacing.unit
    }
 }
