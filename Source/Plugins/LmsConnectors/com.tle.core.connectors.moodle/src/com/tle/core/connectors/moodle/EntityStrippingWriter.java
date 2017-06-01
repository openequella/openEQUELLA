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

package com.tle.core.connectors.moodle;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import org.ccil.cowan.tagsoup.XMLWriter;
import org.xml.sax.SAXException;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Due to a bug in Moodle 2.0 and 2.1 we need to use this class. (the webservice
 * returns HTML entities in the XML, which is not valid)
 * 
 * @author Aaron
 */
public class EntityStrippingWriter extends XMLWriter
{
	private static final Map<String, String> HTML_ENTITIES = Maps.newHashMap();
	private static final Set<String> XML_ENTITIES = Sets.newHashSet();
	static
	{
		XML_ENTITIES.add("lt");
		XML_ENTITIES.add("gt");
		XML_ENTITIES.add("amp");
		XML_ENTITIES.add("quot");
		XML_ENTITIES.add("apos");

		HTML_ENTITIES.put("nbsp", "00A0");
		HTML_ENTITIES.put("iexcl", "00A1");
		HTML_ENTITIES.put("cent", "00A2");
		HTML_ENTITIES.put("pound", "00A3");
		HTML_ENTITIES.put("curren", "00A4");
		HTML_ENTITIES.put("yen", "00A5");
		HTML_ENTITIES.put("brvbar", "00A6");
		HTML_ENTITIES.put("sect", "00A7");
		HTML_ENTITIES.put("uml", "00A8");
		HTML_ENTITIES.put("copy", "00A9");
		HTML_ENTITIES.put("ordf", "00AA");
		HTML_ENTITIES.put("laquo", "00AB");
		HTML_ENTITIES.put("not", "00AC");
		HTML_ENTITIES.put("shy", "00AD");
		HTML_ENTITIES.put("reg", "00AE");
		HTML_ENTITIES.put("macr", "00AF");
		HTML_ENTITIES.put("deg", "00B0");
		HTML_ENTITIES.put("plusmn", "00B1");
		HTML_ENTITIES.put("sup2", "00B2");
		HTML_ENTITIES.put("sup3", "00B3");
		HTML_ENTITIES.put("acute", "00B4");
		HTML_ENTITIES.put("micro", "00B5");
		HTML_ENTITIES.put("para", "00B6");
		HTML_ENTITIES.put("middot", "00B7");
		HTML_ENTITIES.put("cedil", "00B8");
		HTML_ENTITIES.put("sup1", "00B9");
		HTML_ENTITIES.put("ordm", "00BA");
		HTML_ENTITIES.put("raquo", "00BB");
		HTML_ENTITIES.put("frac14", "00BC");
		HTML_ENTITIES.put("frac12", "00BD");
		HTML_ENTITIES.put("frac34", "00BE");
		HTML_ENTITIES.put("iquest", "00BF");
		HTML_ENTITIES.put("Agrave", "00C0");
		HTML_ENTITIES.put("Aacute", "00C1");
		HTML_ENTITIES.put("Acirc", "00C2");
		HTML_ENTITIES.put("Atilde", "00C3");
		HTML_ENTITIES.put("Auml", "00C4");
		HTML_ENTITIES.put("Aring", "00C5");
		HTML_ENTITIES.put("AElig", "00C6");
		HTML_ENTITIES.put("Ccedil", "00C7");
		HTML_ENTITIES.put("Egrave", "00C8");
		HTML_ENTITIES.put("Eacute", "00C9");
		HTML_ENTITIES.put("Ecirc", "00CA");
		HTML_ENTITIES.put("Euml", "00CB");
		HTML_ENTITIES.put("Igrave", "00CC");
		HTML_ENTITIES.put("Iacute", "00CD");
		HTML_ENTITIES.put("Icirc", "00CE");
		HTML_ENTITIES.put("Iuml", "00CF");
		HTML_ENTITIES.put("ETH", "00D0");
		HTML_ENTITIES.put("Ntilde", "00D1");
		HTML_ENTITIES.put("Ograve", "00D2");
		HTML_ENTITIES.put("Oacute", "00D3");
		HTML_ENTITIES.put("Ocirc", "00D4");
		HTML_ENTITIES.put("Otilde", "00D5");
		HTML_ENTITIES.put("Ouml", "00D6");
		HTML_ENTITIES.put("times", "00D7");
		HTML_ENTITIES.put("Oslash", "00D8");
		HTML_ENTITIES.put("Ugrave", "00D9");
		HTML_ENTITIES.put("Uacute", "00DA");
		HTML_ENTITIES.put("Ucirc", "00DB");
		HTML_ENTITIES.put("Uuml", "00DC");
		HTML_ENTITIES.put("Yacute", "00DD");
		HTML_ENTITIES.put("THORN", "00DE");
		HTML_ENTITIES.put("szlig", "00DF");
		HTML_ENTITIES.put("agrave", "00E0");
		HTML_ENTITIES.put("aacute", "00E1");
		HTML_ENTITIES.put("acirc", "00E2");
		HTML_ENTITIES.put("atilde", "00E3");
		HTML_ENTITIES.put("auml", "00E4");
		HTML_ENTITIES.put("aring", "00E5");
		HTML_ENTITIES.put("aelig", "00E6");
		HTML_ENTITIES.put("ccedil", "00E7");
		HTML_ENTITIES.put("egrave", "00E8");
		HTML_ENTITIES.put("eacute", "00E9");
		HTML_ENTITIES.put("ecirc", "00EA");
		HTML_ENTITIES.put("euml", "00EB");
		HTML_ENTITIES.put("igrave", "00EC");
		HTML_ENTITIES.put("iacute", "00ED");
		HTML_ENTITIES.put("icirc", "00EE");
		HTML_ENTITIES.put("iuml", "00EF");
		HTML_ENTITIES.put("eth", "00F0");
		HTML_ENTITIES.put("ntilde", "00F1");
		HTML_ENTITIES.put("ograve", "00F2");
		HTML_ENTITIES.put("oacute", "00F3");
		HTML_ENTITIES.put("ocirc", "00F4");
		HTML_ENTITIES.put("otilde", "00F5");
		HTML_ENTITIES.put("ouml", "00F6");
		HTML_ENTITIES.put("divide", "00F7");
		HTML_ENTITIES.put("oslash", "00F8");
		HTML_ENTITIES.put("ugrave", "00F9");
		HTML_ENTITIES.put("uacute", "00FA");
		HTML_ENTITIES.put("ucirc", "00FB");
		HTML_ENTITIES.put("uuml", "00FC");
		HTML_ENTITIES.put("yacute", "00FD");
		HTML_ENTITIES.put("thorn", "00FE");
		HTML_ENTITIES.put("yuml", "00FF");
		HTML_ENTITIES.put("OElig", "0152");
		HTML_ENTITIES.put("oelig", "0153");
		HTML_ENTITIES.put("Scaron", "0160");
		HTML_ENTITIES.put("scaron", "0161");
		HTML_ENTITIES.put("Yuml", "0178");
		HTML_ENTITIES.put("fnof", "0192");
		HTML_ENTITIES.put("circ", "02C6");
		HTML_ENTITIES.put("tilde", "02DC");
		HTML_ENTITIES.put("Alpha", "0391");
		HTML_ENTITIES.put("Beta", "0392");
		HTML_ENTITIES.put("Gamma", "0393");
		HTML_ENTITIES.put("Delta", "0394");
		HTML_ENTITIES.put("Epsilon", "0395");
		HTML_ENTITIES.put("Zeta", "0396");
		HTML_ENTITIES.put("Eta", "0397");
		HTML_ENTITIES.put("Theta", "0398");
		HTML_ENTITIES.put("Iota", "0399");
		HTML_ENTITIES.put("Kappa", "039A");
		HTML_ENTITIES.put("Lambda", "039B");
		HTML_ENTITIES.put("Mu", "039C");
		HTML_ENTITIES.put("Nu", "039D");
		HTML_ENTITIES.put("Xi", "039E");
		HTML_ENTITIES.put("Omicron", "039F");
		HTML_ENTITIES.put("Pi", "03A0");
		HTML_ENTITIES.put("Rho", "03A1");
		HTML_ENTITIES.put("Sigma", "03A3");
		HTML_ENTITIES.put("Tau", "03A4");
		HTML_ENTITIES.put("Upsilon", "03A5");
		HTML_ENTITIES.put("Phi", "03A6");
		HTML_ENTITIES.put("Chi", "03A7");
		HTML_ENTITIES.put("Psi", "03A8");
		HTML_ENTITIES.put("Omega", "03A9");
		HTML_ENTITIES.put("alpha", "03B1");
		HTML_ENTITIES.put("beta", "03B2");
		HTML_ENTITIES.put("gamma", "03B3");
		HTML_ENTITIES.put("delta", "03B4");
		HTML_ENTITIES.put("epsilon", "03B5");
		HTML_ENTITIES.put("zeta", "03B6");
		HTML_ENTITIES.put("eta", "03B7");
		HTML_ENTITIES.put("theta", "03B8");
		HTML_ENTITIES.put("iota", "03B9");
		HTML_ENTITIES.put("kappa", "03BA");
		HTML_ENTITIES.put("lambda", "03BB");
		HTML_ENTITIES.put("mu", "03BC");
		HTML_ENTITIES.put("nu", "03BD");
		HTML_ENTITIES.put("xi", "03BE");
		HTML_ENTITIES.put("omicron", "03BF");
		HTML_ENTITIES.put("pi", "03C0");
		HTML_ENTITIES.put("rho", "03C1");
		HTML_ENTITIES.put("sigmaf", "03C2");
		HTML_ENTITIES.put("sigma", "03C3");
		HTML_ENTITIES.put("tau", "03C4");
		HTML_ENTITIES.put("upsilon", "03C5");
		HTML_ENTITIES.put("phi", "03C6");
		HTML_ENTITIES.put("chi", "03C7");
		HTML_ENTITIES.put("psi", "03C8");
		HTML_ENTITIES.put("omega", "03C9");
		HTML_ENTITIES.put("thetasym", "03D1");
		HTML_ENTITIES.put("upsih", "03D2");
		HTML_ENTITIES.put("piv", "03D6");
		HTML_ENTITIES.put("ensp", "2002");
		HTML_ENTITIES.put("emsp", "2003");
		HTML_ENTITIES.put("thinsp", "2009");
		HTML_ENTITIES.put("zwnj", "200C");
		HTML_ENTITIES.put("zwj", "200D");
		HTML_ENTITIES.put("lrm", "200E");
		HTML_ENTITIES.put("rlm", "200F");
		HTML_ENTITIES.put("ndash", "2013");
		HTML_ENTITIES.put("mdash", "2014");
		HTML_ENTITIES.put("lsquo", "2018");
		HTML_ENTITIES.put("rsquo", "2019");
		HTML_ENTITIES.put("sbquo", "201A");
		HTML_ENTITIES.put("ldquo", "201C");
		HTML_ENTITIES.put("rdquo", "201D");
		HTML_ENTITIES.put("bdquo", "201E");
		HTML_ENTITIES.put("dagger", "2020");
		HTML_ENTITIES.put("Dagger", "2021");
		HTML_ENTITIES.put("bull", "2022");
		HTML_ENTITIES.put("hellip", "2026");
		HTML_ENTITIES.put("permil", "2030");
		HTML_ENTITIES.put("prime", "2032");
		HTML_ENTITIES.put("Prime", "2033");
		HTML_ENTITIES.put("lsaquo", "2039");
		HTML_ENTITIES.put("rsaquo", "203A");
		HTML_ENTITIES.put("oline", "203E");
		HTML_ENTITIES.put("frasl", "2044");
		HTML_ENTITIES.put("euro", "20AC");
		HTML_ENTITIES.put("image", "2111");
		HTML_ENTITIES.put("weierp", "2118");
		HTML_ENTITIES.put("real", "211C");
		HTML_ENTITIES.put("trade", "2122");
		HTML_ENTITIES.put("alefsym", "2135");
		HTML_ENTITIES.put("larr", "2190");
		HTML_ENTITIES.put("uarr", "2191");
		HTML_ENTITIES.put("rarr", "2192");
		HTML_ENTITIES.put("darr", "2193");
		HTML_ENTITIES.put("harr", "2194");
		HTML_ENTITIES.put("crarr", "21B5");
		HTML_ENTITIES.put("lArr", "21D0");
		HTML_ENTITIES.put("uArr", "21D1");
		HTML_ENTITIES.put("rArr", "21D2");
		HTML_ENTITIES.put("dArr", "21D3");
		HTML_ENTITIES.put("hArr", "21D4");
		HTML_ENTITIES.put("forall", "2200");
		HTML_ENTITIES.put("part", "2202");
		HTML_ENTITIES.put("exist", "2203");
		HTML_ENTITIES.put("empty", "2205");
		HTML_ENTITIES.put("nabla", "2207");
		HTML_ENTITIES.put("isin", "2208");
		HTML_ENTITIES.put("notin", "2209");
		HTML_ENTITIES.put("ni", "220B");
		HTML_ENTITIES.put("prod", "220F");
		HTML_ENTITIES.put("sum", "2211");
		HTML_ENTITIES.put("minus", "2212");
		HTML_ENTITIES.put("lowast", "2217");
		HTML_ENTITIES.put("radic", "221A");
		HTML_ENTITIES.put("prop", "221D");
		HTML_ENTITIES.put("infin", "221E");
		HTML_ENTITIES.put("ang", "2220");
		HTML_ENTITIES.put("and", "2227");
		HTML_ENTITIES.put("or", "2228");
		HTML_ENTITIES.put("cap", "2229");
		HTML_ENTITIES.put("cup", "222A");
		HTML_ENTITIES.put("int", "222B");
		HTML_ENTITIES.put("there4", "2234");
		HTML_ENTITIES.put("sim", "223C");
		HTML_ENTITIES.put("cong", "2245");
		HTML_ENTITIES.put("asymp", "2248");
		HTML_ENTITIES.put("ne", "2260");
		HTML_ENTITIES.put("equiv", "2261");
		HTML_ENTITIES.put("le", "2264");
		HTML_ENTITIES.put("ge", "2265");
		HTML_ENTITIES.put("sub", "2282");
		HTML_ENTITIES.put("sup", "2283");
		HTML_ENTITIES.put("nsub", "2284");
		HTML_ENTITIES.put("sube", "2286");
		HTML_ENTITIES.put("supe", "2287");
		HTML_ENTITIES.put("oplus", "2295");
		HTML_ENTITIES.put("otimes", "2297");
		HTML_ENTITIES.put("perp", "22A5");
		HTML_ENTITIES.put("sdot", "22C5");
		HTML_ENTITIES.put("lceil", "2308");
		HTML_ENTITIES.put("rceil", "2309");
		HTML_ENTITIES.put("lfloor", "230A");
		HTML_ENTITIES.put("rfloor", "230B");
		HTML_ENTITIES.put("lang", "2329");
		HTML_ENTITIES.put("rang", "232A");
		HTML_ENTITIES.put("loz", "25CA");
		HTML_ENTITIES.put("spades", "2660");
		HTML_ENTITIES.put("clubs", "2663");
		HTML_ENTITIES.put("hearts", "2665");
		HTML_ENTITIES.put("diams", "2666");
	}

	protected final StringWriter w;

	public EntityStrippingWriter(StringWriter w)
	{
		super(w);
		this.w = w;
	}

	public String getOutput()
	{
		return w.toString();
	}

	@Override
	public void characters(char[] chars, int start, int length) throws SAXException
	{
		int newCharsIndex = 0;
		char[] newChars = new char[length * 4]; // should be plenty big enough
		int bufferIndex = 0;
		char[] buffer = new char[length];
		for( int i = start; i < start + length; i++ )
		{
			char c = chars[i];
			if( c == '&' && i + 1 < start + length )
			{
				i++;
				c = chars[i];

				// scan until next ';', if any
				while( c != ';' )
				{
					buffer[bufferIndex] = c;
					bufferIndex++;

					i++;
					if( i == start + length )
					{
						// copy buffer
						// '&'
						newChars[newCharsIndex] = '&';
						newCharsIndex++;

						System.arraycopy(buffer, 0, newChars, newCharsIndex, bufferIndex);
						newCharsIndex += bufferIndex;
						bufferIndex = 0;
						break;
					}
					c = chars[i];
				}

				if( c == ';' )
				{
					// what's in the buffer?
					final String ent = new String(buffer, 0, bufferIndex);
					if( !XML_ENTITIES.contains(ent) )
					{
						final String code = HTML_ENTITIES.get(ent);
						if( code != null )
						{
							char[] cs = code.toCharArray();
							// '&'
							newChars[newCharsIndex] = '&';
							newCharsIndex++;

							System.arraycopy(cs, 0, newChars, newCharsIndex, cs.length);
							newCharsIndex += cs.length;
							bufferIndex = 0;
							// ';'
							newChars[newCharsIndex] = c;
							newCharsIndex++;
						}
					}
					else
					{
						// '&'
						newChars[newCharsIndex] = '&';
						newCharsIndex++;

						System.arraycopy(buffer, 0, newChars, newCharsIndex, bufferIndex);
						newCharsIndex += bufferIndex;
						bufferIndex = 0;
						// ';'
						newChars[newCharsIndex] = c;
						newCharsIndex++;
					}
				}
			}
			else
			{
				newChars[newCharsIndex] = c;
				newCharsIndex++;
			}
		}

		super.characters(newChars, 0, newCharsIndex);
	}
}