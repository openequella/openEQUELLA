module OEQ.UI.ItemSummary.ViewItem where 

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Array (catMaybes, groupBy)
import Data.Array as Array
import Data.Either (either)
import Data.Maybe (Maybe(..), maybe)
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, runEffectFn1)
import MaterialUI.Button (button)
import MaterialUI.Enums as E
import MaterialUI.List (list)
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText)
import MaterialUI.Styles (withStyles)
import MaterialUI.Typography (typography, typography_)
import OEQ.API.Item (loadItemSummary)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.Item (AttachmentNode(..), ItemSummary, ItemSummarySection(..), MetaType(..))
import OEQ.UI.ItemSummary.CopyrightSummary (copyrightSummary)
import OEQ.UI.ItemSummary.ItemComments (itemComments)
import OEQ.UI.ItemSummary.MetadataList (metaEntry)
import OEQ.UI.ItemSummary.MetadataList as ME
import OEQ.Utils.Dates (luxonFormat, luxonFormats, parseIsoToLuxon)
import React (ReactElement, component, statelessComponent, unsafeCreateLeafElement)
import React.DOM (a, div, div', img, text)
import React.DOM.Props (dangerouslySetInnerHTML, src)
import React.DOM.Props as D
import React.DOM.Props as DP
import OEQ.Search.ItemResult (ItemSelection, SelectionType(..))

type ViewItemP = (
  uuid :: String, 
  version :: Int,
  onError :: EffectFn1 ErrorResponse Unit, 
  onSelect :: Maybe (ItemSelection -> Effect Unit), 
  courseCode :: Maybe String
)

type ViewItemProps = {
  | ViewItemP
}

data Command = LoadItemSummary

type ViewItemSummaryProps = {
  content :: ItemSummary 
  | ViewItemP
}

viewItem :: ViewItemProps -> ReactElement
viewItem = unsafeCreateLeafElement $ component "ViewItem" \this -> do 
  let 
    d = eval >>> affAction this
    render {state:{content: Just content}, props:{uuid,version,onError,onSelect,courseCode}} = 
      viewItemSummary {uuid,version,content, onError, onSelect, courseCode}
    render _ = div' []
    eval = case _ of 
      LoadItemSummary -> do 
        {uuid,version,onError} <- getProps
        er <- lift $ loadItemSummary uuid version
        either (liftEffect <<< runEffectFn1 onError) (\c -> modifyState _ {content = Just c }) er
  pure {render: renderer render this, state:{content:Nothing}, componentDidMount: d LoadItemSummary}

viewItemSummary :: ViewItemSummaryProps -> ReactElement
viewItemSummary = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
  where
  render p@{classes, courseCode, onError, onSelect, content:{title:itemName,copyright,sections}} = 
    div' $ (renderSection <$> sections)
      <> (Array.fromFoldable $ renderCopyright <$> copyright)
    where
    renderCopyright c = div [DP.className classes.section ] [ 
      titleText "Copyright",
      copyrightSummary {onError, copyright:c, courseCode}
    ]
    titleText t = typography { 
      className: classes.sectionTitle, 
      variant: E.title, 
      color: E.secondary
      } [text t]
    renderSection section = div [DP.className classes.section ] $ case section of 
      BasicDetails {description} -> [ typography {variant: E.display1} [ text itemName ] ] <> 
        maybe [] (\desc -> [ titleText "Description", typography {variant: E.subheading} [text desc] ]) description
      DisplayNodes dn -> 
        let nodes near = div [DP.className classes.metaRow] $ node <$> Array.fromFoldable near
            node {value:t,title,metaType} = 
              let secondary = case metaType of 
                    HTML -> div [dangerouslySetInnerHTML {__html:t} ] []
                    Date -> text $ luxonFormat (parseIsoToLuxon t) (luxonFormats."DATE_FULL")
                    Text -> text t
                    URL -> a [DP.href t] [text t]
              in listItem {component: "div", disableGutters: true} [ 
                listItemText {primary: title, secondaryTypographyProps:{component:"div"}, secondary} [] 
              ]
            nonFullWidth a b = not a.fullWidth && not b.fullWidth
        in [
          titleText dn.sectionTitle, 
          list {component:"div", disablePadding: true} $ nodes <$> groupBy nonFullWidth dn.meta
        ]
      HtmlSummarySection hs -> (guard hs.showTitle $> titleText hs.sectionTitle) <> [
        typography_ [ div [ dangerouslySetInnerHTML {__html:hs.html}] [] ]
      ]
      CommentsSummarySection {sectionTitle,canAdd,canDelete,anonymousOnly,allowAnonymous} -> 
        let {uuid,version,onError} = p
        in [
          titleText sectionTitle,
          itemComments {uuid,version,onError,canAdd,canDelete,anonymousOnly,allowAnonymous}
        ]
      Attachments att -> 
        let 
        attachView (Attachment {title,href,thumbnailHref,details,uuid} _) = let 
          toMeta {title:t,value} = {key:t, title:t, value : ME.HTML value}
          extraDeets = list {disablePadding: true} $ toMeta >>> metaEntry <$> details 
          in listItem {disableGutters: true} [ 
            listItemIcon_ $ img [src thumbnailHref],
            listItemText {disableTypography: true,  
              primary: typography {variant: E.body1} [a [D.href href] [text title]], 
              secondary: extraDeets 
            } [],
            listItemSecondaryAction_ $ catMaybes [
              (\os -> button {onClick: os {
                item:{
                  uuid:p.uuid, 
                  version:p.version, 
                  name: itemName, 
                  description: Nothing
                }, 
                selected: AttachmentUUID uuid, 
                name:title, 
                description:title, 
                thumbnail:thumbnailHref
              }} [ text "Select"]) <$> onSelect
            ]
          ] 
        in [
          titleText att.sectionTitle, 
          list {disablePadding: true} $ attachView <$> att.attachments
        ]
  styles t = {
    section: {
      marginTop: t.spacing.unit * 2
    },
    detailList: {
      -- display: "flex"
    },
    sectionTitle: {
      marginTop: t.spacing.unit,
      marginBottom: t.spacing.unit
    }, 
    metaRow: {
      display: "flex"
    }
  }
