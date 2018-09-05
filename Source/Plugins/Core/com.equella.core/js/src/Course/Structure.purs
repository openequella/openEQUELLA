module Course.Structure where 

import Prelude hiding (div)

import Control.Alt ((<|>))
import Control.MonadZero (guard)
import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Array (head, mapWithIndex)
import Data.Either (Either)
import Data.Map (Map, lookup)
import Data.Maybe (Maybe, fromMaybe, maybe)
import Data.Newtype (class Newtype, unwrap)
import Data.String (joinWith)
import Data.Traversable (find, traverse)
import Debug.Trace (spy)
import Dispatcher.React (propsRenderer)
import Effect (Effect)
import Foreign.Object (Object)
import MaterialUI.Icon (icon, icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (button, disableGutters, listItem)
import MaterialUI.ListItemIcon (listItemIcon, listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction, listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary)
import MaterialUI.Properties (className, mkProp, onClick, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (title)
import MaterialUI.Typography (typography)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, text)
import React.DOM.Dynamic (div')
import React.DOM.Props (key)
import Search.ItemResult (ItemSelection, Result(..))

newtype CourseNode = CourseNode {
    id::String, 
    name::String, 
    targetable::Boolean, 
    folders::Array CourseNode,
    defaultFolder :: Boolean
}
derive instance ntCN :: Newtype CourseNode _ 

type CourseStructure = {name::String, folders :: Array CourseNode}

type CourseStructureProps = {
  structure :: CourseStructure, 
  selectedFolder :: String, 
  selections :: Map String (Array ItemSelection), 
  onSelectFolder :: String -> Effect Unit,
  onRemove :: String -> ItemSelection -> Effect Unit
}

courseStructure :: CourseStructureProps -> ReactElement
courseStructure = unsafeCreateLeafElement $ withStyles styles $ component "CourseStructure" $ \this -> do 
  let
    render {classes, selections, selectedFolder, onRemove, onSelectFolder, structure: cs} = let 
      selectionsFor folderId i is@{description, selected} = listItem [mkProp "key" $ folderId <> "_" <> show i] [
          listItemText [ primary description ], 
          listItemSecondaryAction_ [ 
            iconButton [onClick \_ -> onRemove folderId is] 
              [ icon_ [ text "delete" ] ] 
          ]
      ]
      nodeList :: CourseNode -> Array ReactElement
      nodeList (CourseNode {name,id,folders}) = [ 
          listItem [
            button true, 
            className $ joinWith " " $ guard (selectedFolder == id) *> [classes.selected],
            onClick $ \_ -> onSelectFolder id
          ] $ [
              listItemIcon_ [ icon_ [ text "folder" ] ],
              listItemText [ primary name ]
          ]
      ] <> (maybe [] (mapWithIndex $ selectionsFor id) $ lookup id selections) <> (folders >>= nodeList)
      in div [key "courses"] [ 
          typography [variant title]  [text cs.name],
          list [disablePadding true] $ cs.folders >>= nodeList
      ]
  pure {render: propsRenderer render this}
  where
    styles theme = {
        selected: {
            backgroundColor: theme.palette.action.selected
        }
    }



findDefaultFolder :: CourseStructure -> Maybe String
findDefaultFolder {folders} = (unwrap >>> _.id) <$> (find isDefault folders <|> head folders)
  where 
  isDefault (CourseNode {defaultFolder}) = defaultFolder

decodeCourseNode :: Json -> Either String CourseNode
decodeCourseNode v = do 
    o <- decodeJson v
    defaultFolder <- fromMaybe false <$> (o .?? "defaultFolder")
    id <- ((show :: Number -> String) <$> o .? "id") <|> o .? "id"
    name <- o .? "name"
    targetable <- fromMaybe false <$> o .?? "targetable"
    folders <- o .? "folders" >>= traverse decodeCourseNode  
    pure $ CourseNode {id,name,targetable,folders,defaultFolder}

decodeStructure :: Json -> Either String CourseStructure
decodeStructure v = do 
    o <- decodeJson v
    folders <- o .? "folders" >>= traverse decodeCourseNode  
    name <- o .? "name"
    pure $ {name,folders}