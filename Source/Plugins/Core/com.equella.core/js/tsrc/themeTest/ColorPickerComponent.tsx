import * as React from 'react';
import {SketchPicker} from 'react-color';
import createStyles from "@material-ui/core/styles/createStyles";
import withStyles,{WithStyles} from "@material-ui/core/styles/withStyles";


const styles = createStyles({
    color: {
      width: '36px',
      height: '14px',
      borderRadius: '2px',
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
interface ColorProps{
  color?: string;
  changeColor(color:string): void;
}
class ColorPickerComponent extends React.Component<ColorProps & WithStyles<typeof styles>>{
  constructor(props: ColorProps & WithStyles<'color' | 'swatch' | 'popover' | 'cover'>) {
    super(props);
  };

  state = {
    displayColorPicker: false,
    color: this.props.color,
  };

  handleClick = () => {
    this.setState({displayColorPicker: !this.state.displayColorPicker})
  };

  handleClose = () => {
    this.setState({displayColorPicker: false})
  };

  handleChange = (color: any) => {
    this.setState({color: color.hex});
     //this.props.changeColor(color.hex);
  };
  handleComplete = (color:any) => {
    this.props.changeColor(color.hex);
  }
  render() {
    const {classes} = this.props;

    return (
      <div>
        <div className={classes.swatch} onClick={this.handleClick}>
          <div style={{background:this.state.color}} className={classes.color}/>
        </div>
        {this.state.displayColorPicker ? <div className={classes.popover}>
          <div className={classes.cover} onClick={this.handleClose}/>
          <SketchPicker disableAlpha={true} color={this.state.color} onChange={this.handleChange} onChangeComplete={this.handleComplete}/>
        </div>:null}

      </div>
    )
  }
}


export default withStyles(styles)(ColorPickerComponent);
