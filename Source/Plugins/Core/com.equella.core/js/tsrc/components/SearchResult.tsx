import * as React from 'react'
import { ListItem, ListItemText } from 'material-ui/List';
import { Theme, withStyles, WithStyles } from 'material-ui/styles';

const styles = withStyles((theme: Theme) => (
{
    searchResultContent: {
        display: "flex",
        marginTop: "8px"
    },
    itemThumb: {
        maxWidth: "88px",
        maxHeight: "66px",
        marginRight: "12px"
    },
    displayNode: {
        padding: 0
    }
}));

export interface SearchResultProps {
    href: string;
    onClick: (e: React.MouseEvent<HTMLAnchorElement>) => void;
    primaryText: string;
    secondaryText?: string;
}

type PropsWithStyles = SearchResultProps & WithStyles<"searchResultContent" | "itemThumb" | "displayNode">

class SearchResult extends React.Component<PropsWithStyles> {
    render() {
        const link: any = <a href={this.props.href} onClick={this.props.onClick}>{this.props.primaryText}</a>;
        //
        const content: any = <div className={this.props.classes.displayNode}>{this.props.secondaryText}</div>;
        return <ListItem button disableGutters>
                <ListItemText disableTypography primary={link} secondary={content} />
        </ListItem>;
    }
}

export default styles<SearchResultProps>(SearchResult);