package com.tle.web.api.newuitheme.impl;

public class NewUITheme {
	private String primaryColor = "#2196f3";
	private String secondaryColor = "#ff9800";
	private String backgroundColor = "#fafafa";
	private String menuItemColor = "#ffffff";
	private String menuItemTextColor = "#000000";
	private int fontSize = 14;

	public String getPrimaryColor() {
		return primaryColor;
	}

	public void setPrimaryColor(String primaryColor) {
		this.primaryColor = primaryColor;
	}

	public String getSecondaryColor() {
		return secondaryColor;
	}

	public void setSecondaryColor(String secondaryColor) {
		this.secondaryColor = secondaryColor;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getMenuItemColor() {
		return menuItemColor;
	}

	public void setMenuItemColor(String menuItemColor) {
		this.menuItemColor = menuItemColor;
	}

	public String getMenuItemTextColor() {
		return menuItemTextColor;
	}

	public void setMenuItemTextColor(String menuItemTextColor) {
		this.menuItemTextColor = menuItemTextColor;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
}
