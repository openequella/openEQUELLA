/*
 * Created on Jun 22, 2005
 */

package com.tle.beans.entity.itemdef;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.Schema;
import com.tle.common.workflow.Workflow;

@Entity
@AccessType("field")
public class ItemDefinition extends BaseEntity
{
	private static final long serialVersionUID = -12L;
	private static final int NO_REVIEW_PERIOD = Integer.MIN_VALUE;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "collectionSchema")
	private Schema schema;

	@Column(length = 100)
	private String wizardcategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "collectionWorkflow")
	private Workflow workflow;

	private int reviewperiod = NO_REVIEW_PERIOD;

	@Column(length = 25)
	private String scormPackagingTransformation;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "collectionBlobs")
	private ItemdefBlobs slow;

	private boolean denyDirectContribution;

	public ItemDefinition()
	{
		super();
	}

	public ItemDefinition(long id)
	{
		this();
		setId(id);
	}

	/**
	 * This shouldn't be called directly except when setting ID to 0
	 */
	public ItemdefBlobs getSlow()
	{
		if( slow == null )
		{
			slow = new ItemdefBlobs();
		}
		return slow;
	}

	public int getReviewperiod()
	{
		return reviewperiod;
	}

	public boolean hasReviewPeriod()
	{
		return reviewperiod != NO_REVIEW_PERIOD;
	}

	public void setReviewperiod(int reviewperiod)
	{
		this.reviewperiod = reviewperiod;
	}

	public Wizard getWizard()
	{
		return getSlow().getWizard();
	}

	public void setWizard(Wizard wizard)
	{
		getSlow().setWizard(wizard);
	}

	public String getWizardcategory()
	{
		return wizardcategory;
	}

	public void setWizardcategory(String wizardcategory)
	{
		this.wizardcategory = wizardcategory;
	}

	public SearchDetails getSearchDetails()
	{
		return getSlow().getSearchDetails();
	}

	public void setSearchDetails(SearchDetails searchDetails)
	{
		getSlow().setSearchDetails(searchDetails);
	}

	public MetadataMapping getMetadataMapping()
	{
		return getSlow().getMetadataMapping();
	}

	public void setMetadataMapping(MetadataMapping metadataMapping)
	{
		getSlow().setMetadataMapping(metadataMapping);
	}

	public Schema getSchema()
	{
		return schema;
	}

	public void setSchema(Schema schema)
	{
		this.schema = schema;
	}

	public Workflow getWorkflow()
	{
		return workflow;
	}

	public void setWorkflow(Workflow workflow)
	{
		this.workflow = workflow;
	}

	public List<ItemMetadataRule> getItemMetadataRules()
	{
		return getSlow().getItemMetadataRules();
	}

	public void setItemMetadataRules(List<ItemMetadataRule> rules)
	{
		getSlow().setItemMetadataRules(rules);
	}

	public List<DynamicMetadataRule> getDynamicMetadataRules()
	{
		return getSlow().getDynamicMetadataRules();
	}

	public void setDynamicMetadataRules(List<DynamicMetadataRule> rules)
	{
		getSlow().setDynamicMetadataRules(rules);
	}

	public String getScormPackagingTransformation()
	{
		return scormPackagingTransformation;
	}

	public void setScormPackagingTransformation(String scormPackagingTransformation)
	{
		this.scormPackagingTransformation = scormPackagingTransformation;
	}

	public SummaryDisplayTemplate getItemSummaryDisplayTemplate()
	{
		return getSlow().getItemSummarySections();
	}

	public void setItemSummaryDisplayTemplate(SummaryDisplayTemplate itemSummaryDisplayTemplate)
	{
		getSlow().setItemSummarySections(itemSummaryDisplayTemplate);
	}

	public boolean isDenyDirectContribution()
	{
		return denyDirectContribution;
	}

	public void setDenyDirectContribution(boolean denyDirectContribution)
	{
		this.denyDirectContribution = denyDirectContribution;
	}
}
