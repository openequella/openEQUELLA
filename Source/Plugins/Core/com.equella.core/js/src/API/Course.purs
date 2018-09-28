module OEQ.API.Course where 

import Prelude

import Data.Either (Either)
import Effect.Aff (Aff)
import Global.Unsafe (unsafeEncodeURIComponent)
import OEQ.API.Requests (getJson)
import OEQ.Data.Course (CourseEntity, decodeCourse)
import OEQ.Data.Error (ErrorResponse)

getCourseByCode :: String -> Aff (Either ErrorResponse CourseEntity)
getCourseByCode courseCode = getJson ("api/course/bycode/" <> unsafeEncodeURIComponent courseCode) decodeCourse