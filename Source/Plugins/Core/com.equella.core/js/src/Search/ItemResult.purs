module Search.ItemResult where 

import Prelude hiding (div)

import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (catMaybes, findMap, fromFoldable)
import Data.Maybe (Maybe(..), fromMaybe)
import Dispatcher.React (propsRenderer)
import EQUELLA.Environment (baseUrl, prepLangStrings)
import Effect (Effect)
import MaterialUI.Button (button)
import MaterialUI.DialogTitle (disableTypography)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItem as LI
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Properties (className, classes_, color, mkProp, onClick, style, variant)
import MaterialUI.Properties as MUI
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle as TS
import MaterialUI.Typography (textSecondary, typography)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', img, text)
import React.DOM.Props as DP
import TimeAgo (timeAgo)

newtype Attachment = Attachment {thumbnailHref::String}
newtype DisplayField = DisplayField {name :: String, html::String}

data SelectionType = Summary | AttachmentUUID String | Filepath String

type ItemSelection = {
    item :: Result,
    selected :: SelectionType
}

newtype Result = Result {
    name::String, 
    description:: Maybe String, 
    modifiedDate::String,
    displayFields :: Array DisplayField, 
    thumbnail::String, 
    uuid::String, 
    version::Int, 
    attachments::Array Attachment
}

instance attachDecode :: DecodeJson Attachment where
  decodeJson v = do
    o <- decodeJson v
    links <- o .? "links"
    thumbnailHref <- links .? "thumbnail"
    pure $ Attachment {thumbnailHref}


instance dfDecode :: DecodeJson DisplayField where
  decodeJson v = do
    o <- decodeJson v
    name <- o .? "name"
    html <- o .? "html"
    pure $ DisplayField {name, html}

instance rDecode :: DecodeJson Result where
  decodeJson v = do
    o <- decodeJson v
    nameO <- o .?? "name"
    description <- o .?? "description"
    uuid <- o .? "uuid"
    modifiedDate <- o .? "modifiedDate"
    thumbnail <- o .? "thumbnail"
    df <- o .?? "displayFields"
    version <- o .? "version"
    attachments <- o .? "attachments"
    pure $ Result {uuid,version,name:fromMaybe uuid nameO, description, thumbnail, modifiedDate, displayFields:fromMaybe [] df, attachments}

type ItemResultOptions = { 
    showDivider :: Boolean, 
    result :: Result, 
    onSelect :: Maybe (ItemSelection -> Effect Unit)
}

itemResultOptions :: Result -> ItemResultOptions
itemResultOptions result = {showDivider:false, result, onSelect:Nothing }

itemResult :: ItemResultOptions -> ReactElement
itemResult = unsafeCreateLeafElement $ withStyles styles $ component "ItemResult" $ \this -> do
  let 
    string = prepLangStrings rawStrings
    render {classes, showDivider, onSelect, 
        result:item@(Result {name,description,displayFields,uuid,version,attachments,modifiedDate})} =
        let descMarkup descText = typography [] [ text descText ]
            titleLink = typography [variant TS.subheading, style {textDecoration:"none", color:"blue"},
                            MUI.component "a", 
                            mkProp "href" $ baseUrl <> "items/" <> uuid <> "/" <> show version <> "/"] [ 
                                text name 
                            ]
            attachThumb (Attachment {thumbnailHref}) = Just $ img [
                    DP.aria {hidden:true}, 
                    DP.className classes.itemThumb, 
                    DP.src thumbnailHref
                ]
            firstThumb = fromFoldable $ findMap attachThumb attachments
            extraDeets = [
                listItem [classes_ {default: classes.displayNode}, disableGutters true] [
                    metaTitle string.modifiedDate,
                    metaContent [
                        timeAgo modifiedDate []
                    ]
                ]
            ]
            extraFields = (fieldDiv <$> displayFields) <> extraDeets
            itemContent = div [ DP.className classes.searchResultContent ] $ firstThumb <> [ 
                    div' $ fromFoldable (descMarkup <$> description) <> [ list [disablePadding true] extraFields ] 
                ]
        in listItem [LI.button true, LI.divider showDivider] $ catMaybes [
            Just $ listItemText [ disableTypography true, primary titleLink, secondary itemContent ], 
            (\ons -> listItemSecondaryAction_ [ button [onClick \_ -> ons {item, selected: Summary}] [ text "Select"] ]) <$> onSelect
        ]
        where
        metaTitle n = typography [variant TS.body1, className classes.metaLabel ] [ text n ]
        metaContent c = typography [MUI.component "div", color textSecondary] c 
        fieldDiv (DisplayField {name:n,html}) = listItem [classes_ {default: classes.displayNode}, disableGutters true] [
            metaTitle n,
            metaContent [ div [DP.dangerouslySetInnerHTML {__html: html}] [] ]
        ]
  pure {render: propsRenderer render this}
  where 
    styles theme = {
        metaLabel: {
            alignSelf: "flex-start", 
            marginRight: theme.spacing.unit
        }, 
        displayNode: {
            padding: 0
        }, 
        searchResultContent: {
            display: "flex",
            marginTop: "8px"
        },
        itemThumb: {
            maxWidth: "88px",
            maxHeight: "66px",
            marginRight: "12px"
        }
    }

rawStrings :: { prefix :: String
, strings :: { modifiedDate :: String
             }
}
rawStrings = {prefix: "searchpage", 
  strings: {
    modifiedDate: "Modified"
  }
}
