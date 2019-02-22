/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.result.util;

import com.tle.web.sections.result.util.CurrencyLabel.CurrencyInfo;
import java.math.BigDecimal;
import java.util.Currency;

public class MoneyLabel extends NumberLabel {
  private static final long serialVersionUID = 1L;

  private final Currency currency;
  private final boolean showSymbol;
  private final boolean showCode;
  private final boolean html;

  public MoneyLabel(long value, Currency currency) {
    this(value, currency, true, true, false);
  }

  public MoneyLabel(BigDecimal value, Currency currency) {
    this(value, currency, true, true, false);
  }

  /**
   * @param value
   * @param currency
   * @param html Do not entity encode the text
   */
  public MoneyLabel(long value, Currency currency, boolean html) {
    this(value, currency, true, true, html);
  }

  public MoneyLabel(
      BigDecimal value, Currency currency, boolean showSymbol, boolean showCode, boolean html) {
    super(value);
    final int frac = currency.getDefaultFractionDigits();
    if (frac != -1) {
      setMinDecimals(frac);
      setMaxDecimals(frac);
    }
    this.currency = currency;
    this.showSymbol = showSymbol;
    this.showCode = showCode;
    this.html = html;
  }

  /**
   * @param value
   * @param currency
   * @param showSymbol
   * @param showCode
   * @param html Do not entity encode the text
   */
  public MoneyLabel(
      long value, Currency currency, boolean showSymbol, boolean showCode, boolean html) {
    super(new BigDecimal(value).movePointLeft(currency.getDefaultFractionDigits()).doubleValue());
    final int frac = currency.getDefaultFractionDigits();
    if (frac != -1) {
      setMinDecimals(frac);
      setMaxDecimals(frac);
    }
    this.currency = currency;
    this.showSymbol = showSymbol;
    this.showCode = showCode;
    this.html = html;
  }

  @SuppressWarnings("nls")
  @Override
  public String getText() {
    final String rawCode = currency.getCurrencyCode();
    final CurrencyInfo currencyInfo = CurrencyLabel.getCurrencyInfo(rawCode);

    if (currencyInfo == null) {
      return super.getText() + (showCode ? " " + rawCode.toUpperCase() : "");
    }

    final String symbol = (showSymbol ? currencyInfo.getSymbol() : "");
    final String code = (showCode ? " " + currencyInfo.getCode() : "");

    // TODO: assuming prefix symbol...
    return symbol + super.getText() + code;
  }

  @Override
  public boolean isHtml() {
    return html;
  }
}
