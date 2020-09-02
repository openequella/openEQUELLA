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
import { ReactElement, useState } from "react";
import * as React from "react";
import * as OEQ from "@openequella/rest-api-client";
import {
  Classification,
  SelectedCategories,
} from "../../modules/SearchFacetsModule";
import { languageStrings } from "../../util/langstrings";

const useStyles = makeStyles({
  classificationList: {
    maxHeight: 500,
    overflow: "auto",
  },
});
export interface FacetSelectorProps {
  /**
   * A list of available Classifications returned from server.
   */
  classifications: Classification[];
  /**
   * A list of selected categories which are grouped by their Classification ID.
   */
  selectedCategories?: SelectedCategories[];
  /**
   * Handler for selecting/deselecting categories.
   * @param terms A list of currently selected terms.
   */
  onSelectedCategoriesChange: (categories: SelectedCategories[]) => void;
}
export const FacetSelector = ({
  classifications,
  selectedCategories = [],
  onSelectedCategoriesChange,
}: FacetSelectorProps) => {
  const classes = useStyles();
  const [showMoreMap, setShowMoreMap] = useState<Map<number, boolean>>(
    new Map(classifications.map((classification) => [classification.id, true]))
  );

  const onShowMore = (classificationID: number, showMore: boolean) => {
    const copiedMap = new Map(showMoreMap);
    copiedMap.set(classificationID, showMore);
    setShowMoreMap(copiedMap);
  };
  /**
   * The list of selected categories are grouped by Classification ID.
   * If there is a group matching the ID, then update this group's selected categories,
   * and otherwise add a new group for the ID and its firstly selected category.
   *
   * @param classificationID The ID of a Classification
   * @param category The selected or unselected category
   */
  const handleSelectCategories = (
    classificationID: number,
    category: string
  ) => {
    const categoryGroupIndex = selectedCategories.findIndex(
      (c) => c.id === classificationID
    );
    const copiedCategoryGroups = [...selectedCategories];

    // If there is no group for this category then add a new group,
    // and otherwise update the category list of this group.
    if (categoryGroupIndex === -1) {
      copiedCategoryGroups.push({
        id: classificationID,
        categories: [category],
      });
    } else {
      const copiedSelectedCategories = [
        ...selectedCategories[categoryGroupIndex].categories,
      ];
      const categoryIndex = copiedSelectedCategories.indexOf(category);
      if (categoryIndex === -1) {
        copiedSelectedCategories.push(category);
      } else {
        copiedSelectedCategories.splice(categoryIndex, 1);
      }
      copiedCategoryGroups.splice(categoryGroupIndex, 1, {
        id: classificationID,
        categories: copiedSelectedCategories,
      });
    }

    onSelectedCategoriesChange(copiedCategoryGroups);
  };

  /**
   * Create a button to show more/less categories for each Classification.
   * @param classificationID The ID of a Classification.
   * @param showMore Whether to display 'Show more' or 'Show less'.
   */
  const showMoreButton = (
    classificationID: number,
    showMore: boolean
  ): ReactElement => (
    <ListItem>
      <Grid container justify="center">
        <Grid item>
          <Button
            variant="text"
            onClick={() => onShowMore(classificationID, !showMore)}
          >
            {showMore
              ? languageStrings.searchpage.facetSelector.showMoreButton
              : languageStrings.searchpage.facetSelector.showLessButton}
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
   * @param classificationID The name of a Classification
   * @param facet A facet
   */
  const facetListItem = (
    classificationID: number,
    facet: OEQ.SearchFacets.Facet
  ): ReactElement => {
    const { term } = facet;
    return (
      <ListItem key={`${classificationID}:${term}`} style={{ padding: 0 }}>
        <FormControlLabel
          control={
            <Checkbox
              checked={
                selectedCategories
                  ?.find((c) => c.id === classificationID)
                  ?.categories?.includes(term) ?? false
              }
              onChange={() => handleSelectCategories(classificationID, term)}
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
   * @param id The ID of a Classification
   * @param categories A list of terms to build into a list
   * @param maxDisplay Default maximum number of displayed facets
   * @param showMore Whether to show more facets or not
   */
  const listCategories = (
    { id, categories, maxDisplay }: Classification,
    showMore: boolean
  ): ReactElement[] => {
    const group = selectedCategories?.find((c) => c.id === id);
    let orderedCategories: OEQ.SearchFacets.Facet[];
    // If this Classification does not have any category selected, don't reorder its categories.
    if (!group) {
      orderedCategories = categories;
    }
    // Otherwise reorder to ensure displaying selected categories first.
    else {
      const selectedCategories = categories.filter((c) =>
        group.categories.includes(c.term)
      );
      const unselectedCategories = categories.filter(
        (c) => !group.categories.includes(c.term)
      );
      orderedCategories = selectedCategories.concat(unselectedCategories);
    }
    return orderedCategories
      .slice(0, showMore ? maxDisplay : undefined)
      .map((facet) => facetListItem(id, facet));
  };

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
      const { id, name, categories, maxDisplay } = classification;
      const showMore = showMoreMap.get(id) ?? true;
      return (
        <ListItem divider key={id}>
          <Grid container direction="column">
            <Grid item>
              <Typography variant="subtitle1">{name}</Typography>
            </Grid>
            <Grid item>
              <List
                dense
                className={!showMore ? classes.classificationList : ""}
              >
                {listCategories(classification, showMore)}
                {categories.length > maxDisplay && showMoreButton(id, showMore)}
              </List>
            </Grid>
          </Grid>
        </ListItem>
      );
    });

  return <List>{buildClassifications}</List>;
};
