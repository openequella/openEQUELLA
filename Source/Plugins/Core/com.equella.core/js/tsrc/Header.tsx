import * as React from 'react';
import { AppBar } from 'material-ui';

class Header extends React.Component<object, object> {
    render() {
        return <AppBar position="sticky" color="primary">
            Hello world
        </AppBar>
    }
}

export default Header;