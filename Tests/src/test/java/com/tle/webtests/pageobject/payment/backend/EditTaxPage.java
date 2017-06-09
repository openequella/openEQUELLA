package com.tle.webtests.pageobject.payment.backend;

import java.math.BigDecimal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

/**
 * @author Aaron
 */
public class EditTaxPage extends AbstractEditEntityPage<EditTaxPage, ShowTaxesPage>
{
	public static final String VALIDATION_MISSING_CODE = "Tax code is mandatory";
	public static final String VALIDATION_MASSIVE_CODE = "Tax code can be at most 10 characters in length";

	public static final String VALIDATION_MISSING_RATE = "Rate is mandatory";
	public static final String VALIDATION_MASSIVE_RATE = "Rate must be a value less than 1,000";
	public static final String VALIDATION_TRUNCATED_RATE = "Rate can have at most 4 decimal places";
	public static final String VALIDATION_NEGATIVE_RATE = "Rate must be a valid number value, zero or greater";
	public static final String VALIDATION_NON_NUMERIC_RATE = "Rate must be a valid number value, zero or greater";

	@FindBy(id = "{editorSectionId}_cd")
	private WebElement codeField;
	@FindBy(id = "{editorSectionId}_pc")
	private WebElement rateField;

	protected EditTaxPage(ShowTaxesPage showTaxesPage)
	{
		super(showTaxesPage);
	}

	@Override
	protected String getEntityName()
	{
		return "tax";
	}

	@Override
	protected String getTitle(boolean create)
	{
		return (create ? "Create new " : "Edit ");
	}

	@Override
	public String getEditorSectionId()
	{
		return "te";
	}

	@Override
	public String getContributeSectionId()
	{
		return "tc";
	}

	public EditTaxPage setCode(String code)
	{
		codeField.clear();
		codeField.sendKeys(code);
		return this;
	}

	public String getCode()
	{
		return codeField.getAttribute("value");
	}

	public String getCodeValidationMessage()
	{
		return invalidMessage(codeField);
	}

	public EditTaxPage setRate(BigDecimal rate)
	{
		return setRate(rate.toPlainString());
	}

	public EditTaxPage setRate(String rate)
	{
		rateField.clear();
		rateField.sendKeys(rate);
		return this;
	}

	public String getRate()
	{
		return rateField.getAttribute("value");
	}

	public BigDecimal getRateBigDecimal()
	{
		return new BigDecimal(getRate());
	}

	public String getRateValidationMessage()
	{
		return invalidMessage(rateField);
	}
	//
	// public boolean isInvalid(Field field)
	// {
	// final WebElement elem = resolveField(field);
	// if( elem == null )
	// {
	// return false;
	// }
	// return isInvalid(elem);
	// }
	//
	// public String getInvalidMessage(Field field)
	// {
	// return invalidMessage(resolveField(field));
	// }
	//
	// private WebElement resolveField(Field field)
	// {
	// switch( field )
	// {
	// case CODE:
	// return codeField;
	// case RATE:
	// return percentField;
	// default:
	// return null;
	// }
	// }
}
