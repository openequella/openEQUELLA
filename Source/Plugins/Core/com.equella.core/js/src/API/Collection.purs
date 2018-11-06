module OEQ.API.Collection where 

import Prelude

import Data.Either (Either)
import Effect.Aff (Aff)
import OEQ.API.Requests (getJson)
import OEQ.Data.BaseEntity (BaseEntityLabel, decodeBaseEntityLabel)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.SearchResults (SearchResults, decodeSearchResults)

listSearchCollections :: Aff (Either ErrorResponse (SearchResults BaseEntityLabel))
listSearchCollections = getJson "api/collection?privilege=SEARCH_COLLECTION" $ decodeSearchResults decodeBaseEntityLabel