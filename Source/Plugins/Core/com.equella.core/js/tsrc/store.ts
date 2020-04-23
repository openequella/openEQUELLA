import { applyMiddleware, combineReducers, compose, createStore } from "redux";
import { createLogger } from "redux-logger";
import thunkMiddleware from "redux-thunk";
import { AclState } from "./acl/index";
import { CourseState } from "./course/index";
import { SchemaState } from "./schema/index";
import { courseService, schemaService, aclService } from "./services";

export interface StoreState {
  course: CourseState;
  schema: SchemaState;
  acl: AclState;
}

const loggerMiddleware = createLogger();

declare global {
  interface Window {
    __REDUX_DEVTOOLS_EXTENSION_COMPOSE__?: typeof compose;
  }
}

const composeEnhancers =
  window["__REDUX_DEVTOOLS_EXTENSION_COMPOSE__"] || compose;
const store = createStore<StoreState>(
  combineReducers({
    course: courseService.reducer,
    schema: schemaService.reducer,
    acl: aclService.reducer,
  }),
  composeEnhancers(applyMiddleware(thunkMiddleware, loggerMiddleware))
);

export default store;
