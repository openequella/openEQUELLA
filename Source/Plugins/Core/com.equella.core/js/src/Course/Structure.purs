module Course.Structure where 

import Prelude

import Control.Alt ((<|>))
import Control.MonadZero (guard)
import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Either (Either)
import Data.Map (Map, lookup)
import Data.Maybe (fromMaybe, maybe)
import Data.String (joinWith)
import Data.Traversable (traverse)
import Dispatcher.React (propsRenderer)
import Effect (Effect)
import Foreign.Object (Object)
import MaterialUI.Icon (icon, icon_)
import MaterialUI.List (list)
import MaterialUI.ListItem (button, listItem)
import MaterialUI.ListItemIcon (listItemIcon, listItemIcon_)
import MaterialUI.ListItemText (listItemText, primary)
import MaterialUI.Properties (className, onClick)
import MaterialUI.Styles (withStyles)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (text)
import Search.ItemResult (ItemSelection, Result(..))

type BasicNode r = (
    id::String, 
    name::String, 
    targetable::Boolean, 
    folders::Array CourseNode
    | r
)
newtype CourseNode = CourseNode (Record (BasicNode (defaultFolder::Boolean)))
newtype CourseStructure = CourseStructure (Record (BasicNode ()))

type CourseStructureProps = {
    structure :: CourseStructure, 
    selectedFolder :: String, 
    selections :: Map String (Array ItemSelection), 
    onSelectFolder :: String -> Effect Unit
}

courseStructure :: CourseStructureProps -> ReactElement
courseStructure = unsafeCreateLeafElement $ withStyles styles $ component "CourseStructure" $ \this -> do 
  let
    render {classes, selections, selectedFolder, onSelectFolder, structure: CourseStructure cs} = let 
      selectionsFor {item:Result {name}, selected} = listItem [] [listItemText [ primary name ]]
      nodeList :: forall r. {|BasicNode r} -> Array ReactElement
      nodeList {name,id,folders} = [ 
          listItem [
            button true, 
            className $ joinWith " " $ guard (selectedFolder == id) *> [classes.selected],
            onClick $ \_ -> onSelectFolder id
          ] $ [
              listItemIcon_ [ icon_ [ text "folder" ] ],
              listItemText [ primary name ]
          ]
      ] <> (maybe [] (map selectionsFor) $ lookup id selections) <> (bind folders \(CourseNode cn) -> nodeList cn)
      in list [] $ nodeList cs
  pure {render: propsRenderer render this}
  where
    styles theme = {
        selected: {
            backgroundColor: theme.palette.action.selected
        }
    }

decodeCourseNode :: Json -> Either String CourseNode
decodeCourseNode v = do 
    o <- decodeJson v
    defaultFolder <- fromMaybe false <$> (o .?? "defaultFolder")
    {id,name,targetable,folders} <- decodeBasicNode o
    pure $ CourseNode {id,name,targetable,folders,defaultFolder}


decodeBasicNode :: Object Json -> Either String {|BasicNode ()}
decodeBasicNode o = do 
    id <- ((show :: Number -> String) <$> o .? "id") <|> o .? "id"
    name <- o .? "name"
    targetable <- fromMaybe false <$> o .?? "targetable"
    folders <- o .? "folders" >>= traverse decodeCourseNode  
    pure {id,name,targetable,folders}

decodeStructure :: Json -> Either String CourseStructure
decodeStructure v = do 
    o <- decodeJson v
    {id,name,targetable,folders} <- decodeBasicNode o
    pure $ CourseStructure {id,name,targetable,folders}