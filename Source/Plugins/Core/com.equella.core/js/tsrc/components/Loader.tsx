import CircularProgress from '@material-ui/core/CircularProgress';
import * as React from 'react';

export default class Loader extends React.Component<{}> {
    render() {
        //TODO: css
        return <div style={ { width:"100%", height: "100%", position: "relative" }}>
                    <div style={ { width: 100, height: 100, margin: "auto", position: "absolute", left: 0, right: 0, top: 0, bottom: 0 } }>
                        <CircularProgress size={100} thickness={5} color="secondary" />
                    </div>
                </div>
    }
}
