module OEQ.Data.Selection where 

import Prelude

import Control.Alt ((<|>))
import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Array (head)
import Data.Either (Either)
import Data.Maybe (Maybe, fromMaybe)
import Data.Newtype (class Newtype, unwrap)
import Data.Traversable (find, traverse)
import Foreign.Object (Object)

newtype CourseNode = CourseNode {
    id::String, 
    name::String, 
    targetable::Boolean, 
    folders::Array CourseNode,
    defaultFolder :: Boolean
}
derive instance ntCN :: Newtype CourseNode _ 

type CourseStructure = {name::String, folders :: Array CourseNode}

type SelectionData = {
  courseData :: CourseData,
  integration :: Maybe IntegrationData
} 

decodeCourseData :: Object Json -> Either String CourseData
decodeCourseData o = do 
  structure <- o .?? "structure" >>= traverse decodeStructure
  pure {structure}

decodeSelection :: Json -> Maybe Json -> Either String SelectionData 
decodeSelection s i = do 
  os <- decodeJson s 
  courseData <- decodeCourseData os
  integration <- traverse decodeIntegration i
  pure {courseData, integration}

type CourseData = {
  structure :: Maybe CourseStructure
}

type IntegrationData = {
  courseInfoCode :: Maybe String
}

decodeIntegration :: Json -> Either String IntegrationData 
decodeIntegration v = do 
  o <- decodeJson v 
  courseInfoCode <- o .? "courseInfoCode" >>= decodeJson
  pure $ {courseInfoCode}

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
    name <- fromMaybe "<untitled>" <$> o .? "name"
    pure $ {name,folders}
