module OEQ.Data.Course where 

import Prelude

import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Either (Either)
import Data.Nullable (Nullable, toNullable)

type CourseEntity = {
    name :: String,
    uuid :: String, 
    from :: Nullable String, 
    until :: Nullable String, 
    citation :: Nullable String,
    code :: String
}

decodeCourse :: Json -> Either String CourseEntity
decodeCourse v = do 
  o <- decodeJson v
  name <- o .? "name"
  uuid <- o .? "uuid"
  from <- toNullable <$> o .?? "from"
  until <- toNullable <$> o .?? "until"
  citation <- toNullable <$> o .?? "citation"
  code <- o .? "code"
  pure  {name,code,uuid,from,until,citation}
