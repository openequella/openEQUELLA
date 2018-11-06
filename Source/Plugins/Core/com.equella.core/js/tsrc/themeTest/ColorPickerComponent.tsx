'use strict';

import * as React from 'react';
import {SketchPicker} from 'react-color';
import createStyles from "@material-ui/core/es/styles/createStyles";
import withStyles, {WithStyles} from "@material-ui/core/styles/withStyles";

const styles = createStyles({
    color: {
      width: '36px',
        height: '14px',
        borderRadius: '2px',
         // background: `rgb(${ state.color.r }, ${ this.state.color.g }, ${ this.state.color.b })`,
    },
    swatch: {
      padding: '5px',
        background: '#fff',
        borderRadius: '1px',
        boxShadow: '0 0 0 1px rgba(0,0,0,.1)',
        display: 'inline-block',
        cursor: 'pointer',
    },
    popover: {
      position: "absolute",
        zIndex: 2,
    },
    cover: {
      position: 'fixed',
        top: '0px',
        right: '0px',
        bottom: '0px',
        left: '0px',
    },
  },
);

class ColorPickerComponent extends React.Component<WithStyles<typeof styles>> {
    state = {
      displayColorPicker: false,
      color: {
        r: 241,
        g: 112,
        b: 19,
      },
    };
    handleClick = () => {
      this.setState({displayColorPicker: !this.state.displayColorPicker})
    };

    handleClose = () => {
      this.setState({displayColorPicker: false})
    };

    handleChange = (color: any) => {
      this.setState({color: color.rgb})
    };

    render() {
      return (
        <div>
          <div style={styles.swatch} onClick={this.handleClick}>
            <div style={styles.color}/>
          </div>
          {this.state.displayColorPicker ? <div style={styles.popover}>
            <div style={styles.cover} onClick={this.handleClose}/>
            <SketchPicker disableAlpha={true} color={this.state.color} onChange={this.handleChange}/>
          </div> : null}

        </div>
      )
    }
  }


export default withStyles(styles)(ColorPickerComponent);
