import { createStyles, IconButton, ListItem, ListItemSecondaryAction, ListItemText, Theme, WithStyles, withStyles } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import DeleteIcon from '@material-ui/icons/Delete';
import * as React from 'react';
import { TargetListEntry as TargetListEntryBean } from '../api/acleditor';

const styles = (theme:Theme) => createStyles(
{
    targetListEntryContent: {
        marginTop: theme.spacing.unit
    },
    itemThumb: {
        maxWidth: "88px",
        maxHeight: "66px",
        marginRight: "12px"
    },
    displayNode: {
        padding: 0
    },
    details: {
        marginTop: theme.spacing.unit
    }
});

export interface TargetListEntryProps {
    href?: string;
    onClick?: (e: React.MouseEvent<HTMLAnchorElement>) => void;
    onDelete?: () => void;
    entry: TargetListEntryBean;
    //extraDetails?: TargetListEntryExtraDetail[];
    //indicators?: string[];
}

type PropsWithStyles = TargetListEntryProps & WithStyles<"targetListEntryContent" | "itemThumb" | "displayNode" | "details">

class TargetListEntry extends React.Component<PropsWithStyles> {
    render() {
		const { onClick, onDelete, href, entry } = this.props
		const clickable = !!(href || onClick);
        const link = <Typography color={(clickable ? "primary" : "default")} variant="subheading" component={(p) => 
            { return (clickable ? 
                <a {...p} href={href} onClick={onClick}>{entry.privilege}</a> :
                <span {...p}>{entry.privilege}</span>); }} />
        /*
        var details: JSX.Element | undefined;
        if (extraDetails){
            details = <List className={ classes.details } disablePadding>
                    { extraDetails.map((detail: TargetListEntryExtraDetail, index: number) => 
                    <ListItem key={index} className={classes.displayNode } disableGutters>
                        <Typography variant="body1">{detail.label}</Typography>
                        { (detail.value ? 
                            <Typography component="div" color="textSecondary"> - {detail.value}</Typography> 
                            : null) }
                    </ListItem>) }
                </List>;
        }
        var indic: JSX.Element | undefined;
        if (indicators){
            indic = <div>
                    { indicators.map((ind, index: number) => <Chip key={index} label={ind} />) }
                </div>;
        }*/

        const content = <Typography variant="body1" className={this.props.classes.targetListEntryContent}>{entry.who}</Typography>;

        return <ListItem button={clickable} onClick={onClick} divider>
                <ListItemText disableTypography primary={link} secondary={content} />
                <ListItemSecondaryAction>
                { onDelete && <IconButton onClick={onDelete}><DeleteIcon/></IconButton> }
                </ListItemSecondaryAction>
        </ListItem>;
    }
}

export default withStyles(styles)(TargetListEntry);