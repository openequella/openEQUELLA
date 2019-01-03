/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.result.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class CurrencyLabel implements Label, Serializable
{
	private static final long serialVersionUID = 1L;

	private static final Map<String, CurrencyInfo> CURRENCY_INFO;

	// http://www.xe.com/symbols.php/
	static
	{
		final Map<String, CurrencyInfo> tempSymbols = Maps.newHashMap();
		tempSymbols.put("ALL", new CurrencyInfo("Albania Lek", "ALL", "Lek", true));
		tempSymbols.put("AFN", new CurrencyInfo("Afghanistan Afghani", "AFN", "؋", true));
		tempSymbols.put("ARS", new CurrencyInfo("Argentina Peso", "ARS", "$", true));
		tempSymbols.put("AWG", new CurrencyInfo("Aruba Guilder", "AWG", "ƒ", true));
		tempSymbols.put("AUD", new CurrencyInfo("Australia Dollar", "AUD", "$", true));
		tempSymbols.put("AZN", new CurrencyInfo("Azerbaijan New Manat", "AZN", "ман", true));
		tempSymbols.put("BSD", new CurrencyInfo("Bahamas Dollar", "BSD", "$", true));
		tempSymbols.put("BBD", new CurrencyInfo("Barbados Dollar", "BBD", "$", true));
		tempSymbols.put("BYR", new CurrencyInfo("Belarus Ruble", "BYR", "p.", true));
		tempSymbols.put("BZD", new CurrencyInfo("Belize Dollar", "BZD", "BZ$", true));
		tempSymbols.put("BMD", new CurrencyInfo("Bermuda Dollar", "BMD", "$", true));
		tempSymbols.put("BOB", new CurrencyInfo("Bolivia Boliviano", "BOB", "$b", true));
		tempSymbols.put("BAM", new CurrencyInfo("Bosnia and Herzegovina Convertible Marka", "BAM", "KM", true));
		tempSymbols.put("BWP", new CurrencyInfo("Botswana Pula", "BWP", "P", true));
		tempSymbols.put("BGN", new CurrencyInfo("Bulgaria Lev", "BGN", "лв", true));
		tempSymbols.put("BRL", new CurrencyInfo("Brazil Real", "BRL", "R$", true));
		tempSymbols.put("BND", new CurrencyInfo("Brunei Darussalam Dollar", "BND", "$", true));
		tempSymbols.put("KHR", new CurrencyInfo("Cambodia Riel", "KHR", "៛", true));
		tempSymbols.put("CAD", new CurrencyInfo("Canada Dollar", "CAD", "$", true));
		tempSymbols.put("KYD", new CurrencyInfo("Cayman Islands Dollar", "KYD", "$", true));
		tempSymbols.put("CLP", new CurrencyInfo("Chile Peso", "CLP", "$", true));
		tempSymbols.put("CNY", new CurrencyInfo("China Yuan Renminbi", "CNY", "¥", true));
		tempSymbols.put("COP", new CurrencyInfo("Colombia Peso", "COP", "$", true));
		tempSymbols.put("CRC", new CurrencyInfo("Costa Rica Colon", "CRC", "₡", true));
		tempSymbols.put("HRK", new CurrencyInfo("Croatia Kuna", "HRK", "kn", true));
		tempSymbols.put("CUP", new CurrencyInfo("Cuba Peso", "CUP", "₱", true));
		tempSymbols.put("CZK", new CurrencyInfo("Czech Republic Koruna", "CZK", "Kč", true));
		tempSymbols.put("DKK", new CurrencyInfo("Denmark Krone", "DKK", "kr", true));
		tempSymbols.put("DOP", new CurrencyInfo("Dominican Republic Peso", "DOP", "RD$", true));
		tempSymbols.put("XCD", new CurrencyInfo("East Caribbean Dollar", "XCD", "$", true));
		tempSymbols.put("EGP", new CurrencyInfo("Egypt Pound", "EGP", "£", true));
		tempSymbols.put("SVC", new CurrencyInfo("El Salvador Colon", "SVC", "$", true));
		tempSymbols.put("EEK", new CurrencyInfo("Estonia Kroon", "EEK", "kr", true));
		tempSymbols.put("EUR", new CurrencyInfo("Euro Member Countries", "EUR", "€", true));
		tempSymbols.put("KVP", new CurrencyInfo("Falkland Islands (Malvinas) Pound", "FKP", "£", true));
		tempSymbols.put("FJD", new CurrencyInfo("Fiji Dollar", "FJD", "$", true));
		tempSymbols.put("GHC", new CurrencyInfo("Ghana Cedis", "GHC", "¢", true));
		tempSymbols.put("GIP", new CurrencyInfo("Gibraltar Pound", "GIP", "£", true));
		tempSymbols.put("GTQ", new CurrencyInfo("Guatemala Quetzal", "GTQ", "Q", true));
		tempSymbols.put("GGP", new CurrencyInfo("Guernsey Pound", "GGP", "£", true));
		tempSymbols.put("GYD", new CurrencyInfo("Guyana Dollar", "GYD", "$", true));
		tempSymbols.put("HNL", new CurrencyInfo("Honduras Lempira", "HNL", "L", true));
		tempSymbols.put("HKD", new CurrencyInfo("Hong Kong Dollar", "HKD", "$", true));
		tempSymbols.put("HUF", new CurrencyInfo("Hungary Forint", "HUF", "Ft", true));
		tempSymbols.put("ISK", new CurrencyInfo("Iceland Krona", "ISK", "kr", true));
		tempSymbols.put("INR", new CurrencyInfo("India Rupee", "INR", "\u20B9", true));
		tempSymbols.put("IDR", new CurrencyInfo("Indonesia Rupiah", "IDR", "Rp", true));
		tempSymbols.put("IRR", new CurrencyInfo("Iran Rial", "IRR", "﷼", true));
		tempSymbols.put("IMP", new CurrencyInfo("Isle of Man Pound", "IMP", "£", true));
		tempSymbols.put("ILS", new CurrencyInfo("Israel Shekel", "ILS", "₪", true));
		tempSymbols.put("JMD", new CurrencyInfo("Jamaica Dollar", "JMD", "J$", true));
		tempSymbols.put("JPY", new CurrencyInfo("Japan Yen", "JPY", "¥", true));
		tempSymbols.put("JEP", new CurrencyInfo("Jersey Pound", "JEP", "£", true));
		tempSymbols.put("KZT", new CurrencyInfo("Kazakhstan Tenge", "KZT", "лв", true));
		tempSymbols.put("KPW", new CurrencyInfo("Korea (North) Won", "KPW", "₩", true));
		tempSymbols.put("KRW", new CurrencyInfo("Korea (South) Won", "KRW", "₩", true));
		tempSymbols.put("KGS", new CurrencyInfo("Kyrgyzstan Som", "KGS", "лв", true));
		tempSymbols.put("LAK", new CurrencyInfo("Laos Kip", "LAK", "₭", true));
		tempSymbols.put("LVL", new CurrencyInfo("Latvia Lat", "LVL", "Ls", true));
		tempSymbols.put("LBP", new CurrencyInfo("Lebanon Pound", "LBP", "£", true));
		tempSymbols.put("LRD", new CurrencyInfo("Liberia Dollar", "LRD", "$", true));
		tempSymbols.put("LTL", new CurrencyInfo("Lithuania Litas", "LTL", "Lt", true));
		tempSymbols.put("MKD", new CurrencyInfo("Macedonia Denar", "MKD", "ден", true));
		tempSymbols.put("MYR", new CurrencyInfo("Malaysia Ringgit", "MYR", "RM", true));
		tempSymbols.put("MUR", new CurrencyInfo("Mauritius Rupee", "MUR", "₨", true));
		tempSymbols.put("MXN", new CurrencyInfo("Mexico Peso", "MXN", "$", true));
		tempSymbols.put("MNT", new CurrencyInfo("Mongolia Tughrik", "MNT", "₮", true));
		tempSymbols.put("MZN", new CurrencyInfo("Mozambique Metical", "MZN", "MT", true));
		tempSymbols.put("NAD", new CurrencyInfo("Namibia Dollar", "NAD", "$", true));
		tempSymbols.put("NPR", new CurrencyInfo("Nepal Rupee", "NPR", "₨", true));
		tempSymbols.put("ANG", new CurrencyInfo("Netherlands Antilles Guilder", "ANG", "ƒ", true));
		tempSymbols.put("NZD", new CurrencyInfo("New Zealand Dollar", "NZD", "$", true));
		tempSymbols.put("NIO", new CurrencyInfo("Nicaragua Cordoba", "NIO", "C$", true));
		tempSymbols.put("NGN", new CurrencyInfo("Nigeria Naira", "NGN", "₦", true));
		tempSymbols.put("KPW", new CurrencyInfo("Korea (North) Won", "KPW", "₩", true));
		tempSymbols.put("NOK", new CurrencyInfo("Norway Krone", "NOK", "kr", true));
		tempSymbols.put("OMR", new CurrencyInfo("Oman Rial", "OMR", "﷼", true));
		tempSymbols.put("PKR", new CurrencyInfo("Pakistan Rupee", "PKR", "₨", true));
		tempSymbols.put("PAB", new CurrencyInfo("Panama Balboa", "PAB", "B/.", true));
		tempSymbols.put("PYG", new CurrencyInfo("Paraguay Guarani", "PYG", "Gs", true));
		tempSymbols.put("PEN", new CurrencyInfo("Peru Nuevo Sol", "PEN", "S/.", true));
		tempSymbols.put("PHP", new CurrencyInfo("Philippines Peso", "PHP", "₱", true));
		tempSymbols.put("PLN", new CurrencyInfo("Poland Zloty", "PLN", "zł", true));
		tempSymbols.put("QAR", new CurrencyInfo("Qatar Riyal", "QAR", "﷼", true));
		tempSymbols.put("RON", new CurrencyInfo("Romania New Leu", "RON", "lei", true));
		tempSymbols.put("RUB", new CurrencyInfo("Russia Ruble", "RUB", "руб", true));
		tempSymbols.put("SHP", new CurrencyInfo("Saint Helena Pound", "SHP", "£", true));
		tempSymbols.put("SAR", new CurrencyInfo("Saudi Arabia Riyal", "SAR", "﷼", true));
		tempSymbols.put("RSD", new CurrencyInfo("Serbia Dinar", "RSD", "Дин.", true));
		tempSymbols.put("SCR", new CurrencyInfo("Seychelles Rupee", "SCR", "₨", true));
		tempSymbols.put("SGD", new CurrencyInfo("Singapore Dollar", "SGD", "$", true));
		tempSymbols.put("SBD", new CurrencyInfo("Solomon Islands Dollar", "SBD", "$", true));
		tempSymbols.put("SOS", new CurrencyInfo("Somalia Shilling", "SOS", "S", true));
		tempSymbols.put("ZAR", new CurrencyInfo("South Africa Rand", "ZAR", "R", true));
		tempSymbols.put("KRW", new CurrencyInfo("Korea (South) Won", "KRW", "₩", true));
		tempSymbols.put("LKR", new CurrencyInfo("Sri Lanka Rupee", "LKR", "₨", true));
		tempSymbols.put("SEK", new CurrencyInfo("Sweden Krona", "SEK", "kr", true));
		tempSymbols.put("CHF", new CurrencyInfo("Switzerland Franc", "CHF", "CHF", true));
		tempSymbols.put("SRD", new CurrencyInfo("Suriname Dollar", "SRD", "$", true));
		tempSymbols.put("SYP", new CurrencyInfo("Syria Pound", "SYP", "£", true));
		tempSymbols.put("TWD", new CurrencyInfo("Taiwan New Dollar", "TWD", "NT$", true));
		tempSymbols.put("THB", new CurrencyInfo("Thailand Baht", "THB", "฿", true));
		tempSymbols.put("TTD", new CurrencyInfo("Trinidad and Tobago Dollar", "TTD", "TT$", true));
		tempSymbols.put("TRY", new CurrencyInfo("Turkey Lira", "TRY", "\u20BA", true));
		tempSymbols.put("TRL", new CurrencyInfo("Turkey Lira", "TRL", "₤", true));
		tempSymbols.put("TVD", new CurrencyInfo("Tuvalu Dollar", "TVD", "$", true));
		tempSymbols.put("UAH", new CurrencyInfo("Ukraine Hryvna", "UAH", "₴", true));
		tempSymbols.put("GBP", new CurrencyInfo("United Kingdom Pound", "GBP", "£", true));
		tempSymbols.put("USD", new CurrencyInfo("United States Dollar", "USD", "$", true));
		tempSymbols.put("UYU", new CurrencyInfo("Uruguay Peso", "UYU", "$U", true));
		tempSymbols.put("UZS", new CurrencyInfo("Uzbekistan Som", "UZS", "лв", true));
		tempSymbols.put("VEF", new CurrencyInfo("Venezuela Bolivar Fuerte", "VEF", "Bs", true));
		tempSymbols.put("VND", new CurrencyInfo("Viet Nam Dong", "VND", "₫", true));
		tempSymbols.put("YER", new CurrencyInfo("Yemen Rial", "YER", "﷼", true));
		tempSymbols.put("", new CurrencyInfo("Zimbabwe Dollar", "ZWD", "Z$", true));
		tempSymbols.put("ZWL", new CurrencyInfo("Zimbabwe Dollar", "ZWL", "Z$", true));
		tempSymbols.put("ZWD", new CurrencyInfo("Zimbabwe Dollar", "ZWD", "Z$", true));

		CURRENCY_INFO = Collections.unmodifiableMap(tempSymbols);
	}

	private final Currency currency;

	public CurrencyLabel(Currency currency)
	{
		this.currency = currency;
	}

	@Override
	public String getText()
	{
		return getSymbol(currency.getCurrencyCode());
	}

	@Override
	public boolean isHtml()
	{
		return false;
	}

	public static String getSymbol(String currencyCode)
	{
		final CurrencyInfo currencyInfo = getCurrencyInfo(currencyCode);
		if( currencyInfo != null )
		{
			return currencyInfo.getSymbol();
		}
		return "";
	}

	public static CurrencyInfo getCurrencyInfo(String currencyCode)
	{
		return CURRENCY_INFO.get(currencyCode);
	}

	public static class CurrencyInfo
	{
		private final String name;
		private final String symbol;
		private final String code;
		private final boolean prefixSymbol;

		public CurrencyInfo(String name, String code, String symbol, boolean prefixSymbol)
		{
			this.name = name;
			this.symbol = symbol;
			this.code = code;
			this.prefixSymbol = prefixSymbol;
		}

		public String getName()
		{
			return name;
		}

		public String getSymbol()
		{
			return symbol;
		}

		public String getCode()
		{
			return code;
		}

		public boolean isPrefixSymbol()
		{
			return prefixSymbol;
		}
	}
}
