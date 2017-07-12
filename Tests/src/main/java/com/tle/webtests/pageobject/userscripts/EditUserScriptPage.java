package com.tle.webtests.pageobject.userscripts;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;

public class EditUserScriptPage extends AbstractEditEntityPage<EditUserScriptPage, ShowUserScriptsPage> {
    @FindBy(id = "{editorSectionId}_scriptTypeList")
    private WebElement scriptTypeList;
    @FindBy(id = "{editorSectionId}_moduleNameField")
    private WebElement moduleNameField;
    @FindBy(id = "syntax-div")
    private WebElement syntaxCheckDiv;
    @FindBy(id = "{editorSectionId}_checkSyntaxButton")
    private WebElement checkSyntaxButton;
    @FindBy(xpath = "//div[@class='editor-container']")
    private WebElement editorContainer;
    @FindBy(xpath = "//*[@id='syntax-div']/pre/span")
    private WebElement syntaxCheckResult;
    @FindBy(id = "{editorSectionId}_freemarkerlinkDiv")
    private WebElement scriptHelp;

    protected EditUserScriptPage(ShowUserScriptsPage showUserScriptsPage) {
        super(showUserScriptsPage);
    }

    @Override
    protected String getEntityName() {
        return "script";
    }

    @Override
    protected String getContributeSectionId() {
        return "usc";
    }

    @Override
    protected String getEditorSectionId() {
        return "use";
    }

    public void pickScriptType(String type) {
        WaitingPageObject<EditUserScriptPage> ajaxUpdate = ExpectWaiter.waiter(
                ExpectedConditions2.ajaxUpdateExpectBy(driver.findElement(By.id("script-field")),
                        By.id(getEditorSectionId() + (type.equals("executable") ? "_moduleNameField" : "_freemarkerlinkDiv"))), this);
        new EquellaSelect(getContext(), scriptTypeList).selectByValue(type);
        ajaxUpdate.get();
    }

    public boolean isModuleNameInvalid() {
        return isPresent(By.xpath("//div[@class='module-name']/following-sibling::p"));
    }

    public boolean isScriptInvalid() {
        return isPresent(By.xpath("//div[@class='editor-container']/following-sibling::p"));
    }

    public void setModuleName(String moduleName) {
        moduleNameField.clear();
        moduleNameField.sendKeys(moduleName);
    }

    /**
     * NOTE: You will need to pick script type before the script editing
     * javascript variables become available
     */
    public void setScript(String script, boolean executable) {
        if (executable) {
            ((JavascriptExecutor) driver).executeScript("cmuse_javascript.setValue('" + script + "');");
        } else {
            ((JavascriptExecutor) driver).executeScript("cmuse_freemarker.setValue('" + script + "');");
        }
    }

    public boolean syntaxOk() {
        WaitingPageObject<EditUserScriptPage> ajaxUpdate = ajaxUpdateExpect(syntaxCheckDiv, syntaxCheckResult);
        checkSyntaxButton.click();
        ajaxUpdate.get();
        return isPresent(By.className("ok")) && !isPresent(By.className("fail"));
    }
}
