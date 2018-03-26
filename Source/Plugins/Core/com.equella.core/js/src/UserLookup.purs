module UserLookup where 

import Prelude

import Control.Monad.Aff (Aff, error, throwError)
import Data.Argonaut (class DecodeJson, class EncodeJson, decodeJson, encodeJson, jsonEmptyObject, (.?), (.??), (:=), (~>))
import Data.Either (either)
import Data.Lens (Lens')
import Data.Lens.Iso.Newtype (_Newtype)
import Data.Maybe (Maybe)
import Data.Monoid (class Monoid)
import Data.Newtype (class Newtype)
import EQUELLA.Environment (baseUrl)
import Network.HTTP.Affjax (AJAX, post)

type UserDetailsR = {id:: String, username:: String, firstName:: String,
                           lastName:: String, email :: Maybe String }
                           
newtype UserDetails = UserDetails UserDetailsR
derive instance ntUD :: Newtype UserDetails _
type IdName = {id:: String, name:: String }
newtype GroupDetails = GroupDetails IdName
derive instance ntGD :: Newtype GroupDetails _

newtype RoleDetails = RoleDetails IdName
derive instance ntRD :: Newtype RoleDetails _

newtype UserGroupRoles u g r = UserGroupRoles {users:: Array u, groups :: Array g, roles:: Array r}

instance semigroupUGR :: Semigroup (UserGroupRoles u g r) where 
  append (UserGroupRoles a) (UserGroupRoles b) = UserGroupRoles {users:a.users <> b.users, groups:a.groups <> b.groups, roles: a.roles <> b.roles }

instance monoidUGR :: Monoid (UserGroupRoles u g r) where 
  mempty = UserGroupRoles {users:[], groups:[], roles:[]}

instance encUserGroupRoles :: (EncodeJson u, EncodeJson g, EncodeJson r) => EncodeJson (UserGroupRoles u g r) where
  encodeJson (UserGroupRoles {users, groups, roles}) = 
    "users" := users ~>
    "groups" := groups ~> 
    "roles" := roles ~> jsonEmptyObject

instance decUserGroupRoles :: (DecodeJson u, DecodeJson g, DecodeJson r) => DecodeJson (UserGroupRoles u g r) where
  decodeJson v = do 
    o <- decodeJson v
    users <- o .? "users"
    groups <- o .? "groups"
    roles <- o .? "roles"
    pure $ UserGroupRoles {users,groups,roles}

instance decUserDetails :: DecodeJson UserDetails where 
  decodeJson v = do 
    o <- decodeJson v
    id <- o .? "id"
    username <- o .? "username"
    firstName <- o .? "firstName"
    lastName <- o .? "lastName"
    email <- o .?? "email"
    pure $ UserDetails {id,username,firstName,lastName, email}

instance decGroupDetails :: DecodeJson GroupDetails where 
  decodeJson v = do 
    o <- decodeJson v
    id <- o .? "id"
    name <- o .? "name"
    pure $ GroupDetails {id,name}
  
instance decRoleDetails :: DecodeJson RoleDetails where 
  decodeJson v = do 
    o <- decodeJson v
    id <- o .? "id"
    name <- o .? "name"
    pure $ RoleDetails {id,name}

lookupUsers :: forall e. UserGroupRoles String String String -> Aff (ajax::AJAX|e) (UserGroupRoles UserDetails GroupDetails RoleDetails)
lookupUsers r = do 
  resp <- post (baseUrl <> "api/userquery/lookup") (encodeJson r)
  either (throwError <<< error) pure $ decodeJson resp.response

