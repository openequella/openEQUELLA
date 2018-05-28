import { IconButton, ListItem, ListItemSecondaryAction, ListItemText, Theme, WithStyles, withStyles } from '@material-ui/core';
import Typography from '@material-ui/core/Typography';
import DeleteIcon from '@material-ui/icons/Delete';
import * as React from 'react';

const styles = withStyles((theme: Theme) => (
{
    searchResultContent: {
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
}));

export interface SearchResultExtraDetail {
    label: string;
    value: string;
}

export interface SearchResultProps {
    href: string;
    onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
    onDelete?: () => void;
    primaryText: string;
    secondaryText?: string;
    //extraDetails?: SearchResultExtraDetail[];
    //indicators?: string[];
}

type PropsWithStyles = SearchResultProps & WithStyles<"searchResultContent" | "itemThumb" | "displayNode" | "details">

class SearchResult extends React.Component<PropsWithStyles> {
    render() {
        const { onDelete } = this.props
        const link: any = <Typography color="primary" variant="subheading" component={(p) => 
            <a {...p} href={this.props.href} onClick={this.props.onClick}>{this.props.primaryText}</a>}/>
        /*
        var details: JSX.Element | undefined;
        if (extraDetails){
            details = <List className={ classes.details } disablePadding>
                    { extraDetails.map((detail: SearchResultExtraDetail, index: number) => 
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

        const content = <Typography variant="body1" className={this.props.classes.searchResultContent}>{this.props.secondaryText}</Typography>;

        return <ListItem button onClick={this.props.onClick} divider>
                <ListItemText disableTypography primary={link} secondary={content} />
                <ListItemSecondaryAction>
                { onDelete && <IconButton onClick={onDelete}><DeleteIcon/></IconButton> }
                </ListItemSecondaryAction>
        </ListItem>;
    }
}

export default styles<SearchResultProps>(SearchResult);