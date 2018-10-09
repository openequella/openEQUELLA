module OEQ.Search.Controls where 

import Prelude

import Control.MonadZero (guard)
import Data.Maybe (Maybe(..))
import Effect (Effect)
import OEQ.Data.Searches (SearchControlConfig(..))
import OEQ.Search.CollectionControl (filterByCollections)
import OEQ.Search.FacetControl (facetControl)
import OEQ.Search.OrderControl (orderControl, setSort)
import OEQ.Search.OwnerControl (ownerControl)
import OEQ.Search.SearchControl (Placement, SearchControl)
import OEQ.Search.SearchQuery (Query)
import OEQ.Search.WithinLastControl (setModifiedWithin, withinLastControl)

controlFromConfig :: Placement -> SearchControlConfig -> Maybe (Effect SearchControl)
controlFromConfig p = case _ of 
  Sort {editable} -> guard editable $> (pure $ orderControl p)
  Owner {editable} -> guard editable $> ownerControl p
  ModifiedWithin {editable} -> guard editable $> (pure $ withinLastControl p)
  Facet fs -> Just $ pure $ facetControl fs p 
  Collections _ -> Nothing

queryFromConfig :: SearchControlConfig -> Query -> Query 
queryFromConfig = case _ of 
  ModifiedWithin {default} -> setModifiedWithin default
  Sort {default} -> setSort default
  Collections {collections: Just c} -> filterByCollections c
  _ -> identity