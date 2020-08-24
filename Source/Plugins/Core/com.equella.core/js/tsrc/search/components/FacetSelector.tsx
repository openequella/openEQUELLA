/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {
  Button,
  Checkbox,
  FormControlLabel,
  Grid,
  List,
  ListItem,
  Typography,
} from "@material-ui/core";
import { makeStyles } from "@material-ui/core/styles";
import { ReactElement } from "react";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import { Classification } from "../../modules/SearchFacetsModule";
import { languageStrings } from "../../util/langstrings";

const useStyles = makeStyles({
  classificationList: {
    maxHeight: 500,
    overflow: "auto",
  },
});

/**
 * Represents a Classification that is specific to Search page.
 */
export interface SearchPageClassification extends Classification {
  /**
   * A boolean indicating if a classification has hidden categories to show.
   */
  showMore: boolean;
}

export interface FacetSelectorProps {
  /**
   * A list of Classifications.
   */
  classifications: SearchPageClassification[];
  /**
   * A map where the key is a Classification's name and value is
   * a list of terms.
   */
  selectedClassificationTerms?: Map<string, string[]>;
  /**
   * Handler for selecting/deselecting Classification terms.
   * @param terms A list of currently selected terms.
   */
  onSelectTermsChange: (terms: Map<string, string[]>) => void;
  /**
   * Handler for clicking a 'SHOW MORE' button.
   * @param classificationName The name of a Classification.
   */
  onShowMore: (classificationName: string) => void;
}

export const FacetSelector = ({
  classifications,
  selectedClassificationTerms,
  onSelectTermsChange,
  onShowMore,
}: FacetSelectorProps) => {
  const classes = useStyles();

  /**
   * Updates the list of selected Classification terms. If the term exists then remove it
   * from the list. Add it to the list otherwise.
   * A copy of the map and a copy of the array of terms are created internally
   * to avoid mutating parent component's state.
   *
   * @param classificationName The name of a Classification
   * @param term The selected or unselected term
   */
  const handleSelectTerms = (classificationName: string, term: string) => {
    const terms = selectedClassificationTerms?.get(classificationName);
    const copiedTerms = terms ? [...terms] : [];
    const termIndex = copiedTerms.indexOf(term);
    if (termIndex === -1) {
      copiedTerms.push(term);
    } else {
      copiedTerms.splice(termIndex, 1);
    }
    const copiedMap = new Map(selectedClassificationTerms ?? []);
    copiedMap.set(classificationName, copiedTerms);
    onSelectTermsChange(copiedMap);
  };

  /**
   * Render a 'SHOW MORE' button for each Classification.
   * @param classificationName The name of a Classification.
   */
  const showMoreButton = (classificationName: string): ReactElement => (
    <ListItem>
      <Grid container justify="center">
        <Grid item>
          <Button variant="text" onClick={() => onShowMore(classificationName)}>
            {languageStrings.searchpage.facetSelector.showMoreButton}
          </Button>
        </Grid>
      </Grid>
    </ListItem>
  );

  /**
   * Generate texts in the format of 'term (count)' for displaying a facet.
   * @param term The term of a facet
   * @param count The count of a facet
   */
  const facetLabel = ({
    term,
    count,
  }: OEQ.SearchFacets.Facet): ReactElement => (
    <Grid container spacing={1}>
      <Grid item>
        <Typography>{term}</Typography>
      </Grid>
      <Grid item>
        <Typography color="textSecondary">{`(${count})`}</Typography>
      </Grid>
    </Grid>
  );

  /**
   * Build a ListItem consisting of a MUI Checkbox and a Label for a facet.
   * @param classificationName The name of a Classification
   * @param facet A facet
   */
  const facetListItem = (
    classificationName: string,
    facet: OEQ.SearchFacets.Facet
  ): ReactElement => {
    const { term, count } = facet;
    return (
      <ListItem key={`${facet} ${count}`} style={{ padding: 0 }}>
        <FormControlLabel
          control={
            <Checkbox
              checked={
                selectedClassificationTerms
                  ?.get(classificationName)
                  ?.includes(term) ?? false
              }
              onChange={() => handleSelectTerms(classificationName, term)}
            />
          }
          label={facetLabel(facet)}
        />
      </ListItem>
    );
  };

  /**
   * Build a list for a Classification's categories. Some categories may have facets
   * not displayed due to the configured maximum display number.
   *
   * @param name The name of a Classification
   * @param categories A list of terms to build into a list
   * @param showMore Whether to show more facets or not
   * @param maxDisplay Default maximum number of displayed facets
   */
  const listCategories = ({
    name,
    categories,
    showMore,
    maxDisplay,
  }: SearchPageClassification): ReactElement[] =>
    categories
      .slice(0, showMore ? maxDisplay : undefined)
      .map((facet) => facetListItem(name, facet));

  /**
   * Sort and build Classifications that have categories.
   * For each Classification, a scroll bar and a 'Show more' button may or may not
   * be added, depending on whether a classification has more categories to show or not.
   */
  const buildClassifications: ReactElement[] = classifications
    .filter((classification) => classification.categories.length > 0)
    .sort(
      (prevClassification, nextClassification) =>
        prevClassification.orderIndex - nextClassification.orderIndex
    )
    .map((classification) => {
      const { name, showMore } = classification;
      return (
        <ListItem divider key={name}>
          <Grid container direction="column">
            <Grid item>
              <Typography variant="subtitle1">{name}</Typography>
            </Grid>
            <Grid item>
              <List
                dense
                className={!showMore ? classes.classificationList : ""}
              >
                {listCategories(classification)}
                {showMore && showMoreButton(name)}
              </List>
            </Grid>
          </Grid>
        </ListItem>
      );
    });

  return <List>{buildClassifications}</List>;
};
