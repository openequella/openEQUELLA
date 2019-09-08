import * as React from "react";
import { SketchPicker, ColorResult } from "react-color";
import createStyles from "@material-ui/core/styles/createStyles";
import withStyles, { WithStyles } from "@material-ui/core/styles/withStyles";

const styles = createStyles({
  color: {
    width: "36px",
    height: "14px",
    borderRadius: "2px"
  },
  swatch: {
    padding: "5px",
    background: "#fff",
    borderRadius: "1px",
    boxShadow: "0 0 0 1px rgba(0,0,0,.1)",
    display: "inline-block",
    cursor: "pointer"
  },
  popover: {
    position: "absolute",
    zIndex: 2
  },
  cover: {
    position: "fixed",
    top: "0px",
    right: "0px",
    bottom: "0px",
    left: "0px"
  }
});

interface ColorProps {
  color?: string;
  changeColor(color: string): void;
}

class ColorPickerComponent extends React.Component<
  ColorProps & WithStyles<typeof styles>
> {
  constructor(
    props: ColorProps & WithStyles<"color" | "swatch" | "popover" | "cover">
  ) {
    super(props);
  }

  state = {
    displayColorPicker: false,
    colorState: this.props.color
  };

  componentWillReceiveProps = (
    nextProps: ColorProps & WithStyles<typeof styles>
  ) => {
    if (nextProps.color !== this.props.color) {
      this.setState({ colorState: nextProps.color });
    }
  };

  handleClick = () => {
    this.setState({ displayColorPicker: true });
  };

  handleClose = () => {
    this.setState({ displayColorPicker: false });
  };

  handleChange = (color: ColorResult) => {
    this.setState({ colorState: color.hex });
  };

  handleComplete = (color: ColorResult) => {
    this.props.changeColor(color.hex);
  };

  render() {
    const { classes } = this.props;
    return (
      <div>
        <div className={classes.swatch} onClick={this.handleClick}>
          <div
            style={{ background: this.state.colorState }}
            className={classes.color}
          />
        </div>
        {this.state.displayColorPicker ? (
          <div className={classes.popover}>
            <div className={classes.cover} onClick={this.handleClose} />
            <SketchPicker
              disableAlpha={true}
              color={this.state.colorState}
              onChange={this.handleChange}
              onChangeComplete={this.handleComplete}
            />
          </div>
        ) : null}
      </div>
    );
  }
}

export default withStyles(styles)(ColorPickerComponent);
