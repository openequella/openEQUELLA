import * as React from "react";
import { makeStyles } from "@material-ui/styles";
import { IconButton, Popover } from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import JQueryDiv from "../legacycontent/JQueryDiv";
import { languageStrings } from "../util/langstrings";

const useStyles = makeStyles(t => ({
  screenOptions: {
    margin: 20
  }
}));

export default React.memo(function ScreenOptions(props: {
  optionsHtml: string;
  contentId: string;
}) {
  const [optionsAnchor, setOptionsAnchor] = React.useState<HTMLElement>();
  const { optionsHtml } = props;
  const classes = useStyles();
  return (
    <React.Fragment>
      <IconButton
        id="screenOptionsOpen"
        onClick={e => setOptionsAnchor(e.currentTarget)}
        aria-label={languageStrings.screenoptions.description}
      >
        <MoreVertIcon />
      </IconButton>
      <Popover
        open={Boolean(optionsAnchor)}
        marginThreshold={64}
        keepMounted
        container={document.getElementById("eqpageForm")}
        anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
        anchorEl={optionsAnchor}
        onClose={_ => setOptionsAnchor(undefined)}
      >
        <JQueryDiv
          id="screenOptions"
          className={classes.screenOptions}
          html={optionsHtml}
        />
      </Popover>
    </React.Fragment>
  );
});
