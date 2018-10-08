module OEQ.Data.Searches where 

import Prelude

import Data.Argonaut (Json, decodeJson, (.?))
import Data.Either (Either(..))
import Data.Traversable (traverse)
import Foreign.Object (Object)

type SearchConfig = {
    index :: String, 
    sections :: Object (Array SearchControlConfig)
}

data SearchControlConfig = Sort 
  | Owner
  | ModifiedWithin Number

decodeSort :: Object Json -> Either String SearchControlConfig
decodeSort o = pure Sort

decodeControlConfig :: Json -> Either String SearchControlConfig
decodeControlConfig v = do 
  o <- decodeJson v 
  o .? "type" >>= case _ of 
    "sort" -> decodeSort o
    "owner" -> pure Owner
    "modifiedWithin" -> do 
      d <- o .? "default"
      pure $ ModifiedWithin d  
    c -> Left $ "Unknown control type: " <> c

decodeSearchConfig :: Json -> Either String SearchConfig 
decodeSearchConfig v = do 
  o <- decodeJson v 
  index <- o .? "index"
  sectionsM <- o .? "sections"
  sections <- traverse (traverse decodeControlConfig) sectionsM
  pure {index,sections}