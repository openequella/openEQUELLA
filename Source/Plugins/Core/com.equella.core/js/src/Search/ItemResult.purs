module OEQ.Search.ItemResult where 

import Prelude hiding (div)

import Common.Strings (languageStrings)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), (.??))
import Data.Array (catMaybes, findMap, fromFoldable)
import Data.Maybe (Maybe(..), fromMaybe)
import Dispatcher.React (propsRenderer)
import Effect (Effect)
import ExtUI.TimeAgo (timeAgo)
import MaterialUI.Button (button)
import MaterialUI.Enums as TS
import MaterialUI.List (list)
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText')
import MaterialUI.Styles (withStyles)
import MaterialUI.Typography (typography)
import OEQ.MainUI.TSRoutes (LocationDescription, link)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', img, text)
import React.DOM.Props as DP

newtype Attachment = Attachment {thumbnailHref::String}
newtype DisplayField = DisplayField {name :: String, html::String}

data SelectionType = Summary | AttachmentUUID String | Filepath String
derive instance stEQ :: Eq SelectionType

type ItemSelection = {
    item :: {
        uuid::String,
        version::Int, 
        name :: String,
        description :: Maybe String
    },
    thumbnail :: String,
    name :: String,
    description :: String,
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
    attachments:: Maybe (Array Attachment)
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
    attachments <- o .?? "attachments"
    pure $ Result {uuid,version,name:fromMaybe uuid nameO, description, thumbnail, modifiedDate, displayFields:fromMaybe [] df, attachments}

type ItemResultOptions = { 
    showDivider :: Boolean, 
    result :: Result, 
    onSelect :: Maybe (ItemSelection -> Effect Unit),
    clickable :: LocationDescription
}

itemResultOptions :: LocationDescription -> Result -> ItemResultOptions
itemResultOptions clickable result = {showDivider:false, result, onSelect:Nothing, clickable }

itemResult :: ItemResultOptions -> ReactElement
itemResult = unsafeCreateLeafElement $ withStyles styles $ component "ItemResult" $ \this -> do
  let 
    string = languageStrings.searchpage
    render p@{classes, showDivider, onSelect, 
        result:item@(Result {name,description,displayFields,thumbnail,uuid,version,attachments,modifiedDate})} =
        let descMarkup descText = typography {} [ text descText ]
            titleLink = typography {variant: TS.subtitle1, className: classes.titleLink} [ 
                                link {to: p.clickable} [ text name ]
                            ]
            attachThumb (Attachment {thumbnailHref}) = Just $ img [
                    DP.aria {hidden:true}, 
                    DP.className classes.itemThumb, 
                    DP.src thumbnailHref
                ]
            firstThumb = fromFoldable $ findMap attachThumb =<< attachments
            extraDeets = [
                listItem {classes: {default: classes.displayNode}, disableGutters: true} [
                    metaTitle string.modifiedDate,
                    metaContent [
                        timeAgo {datetime:modifiedDate}
                    ]
                ]
            ]
            extraFields = (fieldDiv <$> displayFields) <> extraDeets
            itemContent = div [ DP.className classes.searchResultContent ] $ firstThumb <> [ 
                    div' $ fromFoldable (descMarkup <$> description) <> [ list {disablePadding: true} extraFields ] 
                ]
        in listItem {button: true, divider: showDivider } $ catMaybes [
            Just $ listItemText' {disableTypography: true, primary: titleLink, secondary: itemContent }, 
            (\ons -> listItemSecondaryAction_  [ button {
                    onClick: ons {item: {uuid, version, description, name}, 
                    thumbnail, 
                    description: name, name, selected: Summary} 
                } [ text "Select"] ] ) <$> onSelect
        ]
        where
        metaTitle n = typography {variant: TS.body1, className: classes.metaLabel } [ text n ]
        metaContent c = typography {component: "div", color: TS.textSecondary} c 
        fieldDiv (DisplayField {name:n,html}) = listItem {classes: {default: classes.displayNode}, disableGutters: true} [
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
        }, 
        titleLink: {
            color: theme.palette.primary.dark
        }
    }
