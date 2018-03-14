import * as React from 'react';
import * as ReactDOM from 'react-dom';
// import App from './App';
import registerServiceWorker from './registerServiceWorker';

import { Provider } from 'react-redux';
import CourseStore from './course/CourseStore';

import SearchCourse from './course/SearchCourse';
import EditCourse from './course/EditCourse';
import Header from './Header';

import history from './history';
import { Route } from 'react-router';
import { ConnectedRouter } from 'react-router-redux';

import './index.css';

//const logo = require('./logo.svg');
//<img src={logo} className="App-logo" alt="logo" />

//

ReactDOM.render(
  <div>
    <Provider store={CourseStore}>
      <ConnectedRouter history={history}>
        <div>
          <Route path="*" component={Header} />
          <Route exact path="/" component={SearchCourse} />
          <Route exact path="/edit" component={EditCourse} />
          <Route path="/edit/:uuid" component={EditCourse} />
        </div>
      </ConnectedRouter>
    </Provider>
  </div>,
  document.getElementById('root') as HTMLElement
);
registerServiceWorker();
