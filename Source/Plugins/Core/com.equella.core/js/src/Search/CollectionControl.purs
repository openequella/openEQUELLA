module OEQ.Search.CollectionControl where 

import Prelude

import Data.Lens (set)
import Data.Lens.At (at)
import Data.Maybe (Maybe(..))
import Data.String (joinWith)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Select (select)
import OEQ.Search.SearchControl (Placement, SearchControl)
import OEQ.Search.SearchQuery (Query, ParamDataLens, _params, singleParam)
import React.DOM (text)

_collections :: ParamDataLens
_collections = at "collections"


filterByCollections :: Array String -> Query -> Query 
filterByCollections cols = let v = joinWith "," cols in set (_params <<< _collections) $ Just $ singleParam v "collections" v


-- collectionControl :: Placement -> SearchControl
-- collectionControl placement = let 
-- --   orderItem o = menuItem {value: orderValue o} [ text $ orderName o ]
--   render {props:{classes,updateQuery,query}, state:{}} = 
--     -- order = preview (_order <<< _Just <<< _data <<< _String) query.params
--     -- updateOrder v = updateQuery $ setSortStr v
--     textfield { 
--           className: classes.ordering, 
--       } []
--   collectionSelect = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
--   renderer {updateQuery, query} = pure { 
--     render:[Tuple placement $ orderSelect {query, updateQuery}], 
--     chips:[]
--   }
--   in renderer
--   where 
--   styles theme = {
--     ordering: {
--       width: "10em"
--     }
--   }
