module Routes where 

import Prelude

import Control.Alt ((<|>))
import Routing.Match (Match)
import Routing.Match.Class (lit, str)

data Route = SearchPage | SettingsPage | CoursesPage | CourseEdit String

homeSlash :: Match Unit
homeSlash = lit ""

routeMatch :: Match Route
routeMatch = 
    SearchPage <$ (lit "search") <|>
    SettingsPage <$ (lit "settings") <|>
    CourseEdit <$> (lit "course" *> str <* lit "edit") <|>
    CoursesPage <$ (lit "courses")

routeHref :: Route -> String 
routeHref = append "page.do" <<< routeHash 

routeHash :: Route -> String
routeHash r = "#" <> ( case r of 
    SearchPage -> "search"
    SettingsPage -> "settings"
    CoursesPage -> "courses"
    CourseEdit cid -> "course/" <> cid <> "/edit"
  )
