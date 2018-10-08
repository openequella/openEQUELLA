module OEQ.Search.Controls where 

import Prelude

import Effect (Effect)
import OEQ.Data.Searches (SearchControlConfig(..))
import OEQ.Search.OrderControl (orderControl)
import OEQ.Search.OwnerControl (ownerControl)
import OEQ.Search.SearchControl (SearchControl)
import OEQ.Search.WithinLastControl (withinLastControl)

controlFromConfig :: SearchControlConfig -> Effect SearchControl
controlFromConfig = case _ of 
  Sort -> pure orderControl
  Owner -> ownerControl
  ModifiedWithin wi -> pure $ withinLastControl wi