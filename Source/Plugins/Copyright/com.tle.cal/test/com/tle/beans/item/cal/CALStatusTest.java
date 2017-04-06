package com.tle.beans.item.cal;

import junit.framework.TestCase;

import com.tle.core.activation.validation.PageCounter;

public class CALStatusTest extends TestCase
{
	private void evalTotal(String testString, int expectedTotal)
	{
		int total = PageCounter.countTotalPages(testString);
		assertEquals(expectedTotal, total);
	}

	private void evalRange(String testString, int expectedTotal)
	{
		int total = PageCounter.countTotalRange(testString);
		assertEquals(expectedTotal, total);
	}

	public void testTotal3()
	{
		String st = "[16] p., [40] leaves of plates";
		evalTotal(st, 16);
	}

	public void testTotal4()
	{
		String st = "[8], 155 p.";
		evalTotal(st, 163);
	}

	public void testTotal6()
	{
		String st = "2 v. (xvi, 329; xx, 412 p.)";
		evalTotal(st, 0);
	}

	public void testTotal7()
	{
		String st = "2 v. (xxxxi, 999 p.)";
		evalTotal(st, 0);
	}

	public void testTotal8()
	{
		String st = "226, [44] p.";
		evalTotal(st, 270);
	}

	public void testTotal9()
	{
		String st = "230 p., 25 leaves of plates (some folded)";
		evalTotal(st, 230);
	}

	public void testTotal10()
	{
		String st = "246 p., 32 p. of plates";
		evalTotal(st, 246);
	}

	public void testTotal11()
	{
		String st = "3 cases";
		evalTotal(st, 0);
	}

	public void testTotal12()
	{
		String st = "3 pamphlets";
		evalTotal(st, 0);
	}

	public void testTotal13()
	{
		String st = "3 parts";
		evalTotal(st, 0);
	}

	public void testTotal14()
	{
		String st = "3 pieces";
		evalTotal(st, 0);
	}

	public void testTotal15()
	{
		String st = "3 portfolios";
		evalTotal(st, 0);
	}

	public void testTotal16()
	{
		String st = "3 v.";
		evalTotal(st, 0);
	}

	public void testTotal17()
	{
		String st = "3 v. (xx, 800 p.)";
		evalTotal(st, 0);
	}

	public void testTotal18()
	{
		String st = "329 [i.e. 392] p.";
		evalTotal(st, 392);
	}

	public void testTotal19()
	{
		String st = "33, [31] leaves";
		evalTotal(st, 64);
	}

	public void testTotal20()
	{
		String st = "333 p. : ill., col. maps, ports. (some col.)";
		evalTotal(st, 333);
	}

	public void testTotal21()
	{
		String st = "333 p. : ill., maps";
		evalTotal(st, 333);
	}

	public void testTotal22()
	{
		String st = "366, 98, [99] p.";
		evalTotal(st, 563);
	}

	public void testTotal23()
	{
		String st = "48 [i.e. 96] p.";
		evalTotal(st, 96);
	}

	public void testTotal24()
	{
		String st = "74 p. of ill., 15 p.";
		evalTotal(st, 15);
	}

	public void testTotal25()
	{
		String st = "74 p., 15 leaves of ill.";
		evalTotal(st, 74);
	}

	public void testTotal26()
	{
		String st = "8 v. in 5";
		evalTotal(st, 0);
	}

	public void testTotal27()
	{
		String st = "8, vii, ca. 300, 73 p.";
		evalTotal(st, 388);
	}

	public void testTotal29()
	{
		String st = "ca. 600 p.";
		evalTotal(st, 600);
	}

	public void testTotal30()
	{
		String st = "ix, 155, 127, x p.";
		evalTotal(st, 301);
	}

	public void testTotal33()
	{
		String st = "x, 32, 73 p., [1] leaf of plates";
		evalTotal(st, 115);
	}

	public void testTotal34()
	{
		String st = "xii, 24 p., 212, [43] leaves of plates";
		evalTotal(st, 248);
	}

	public void testTotal35()
	{
		String st = "xii, 35, 25 p.";
		evalTotal(st, 72);
	}

	public void testTotal36()
	{
		String st = "xvi, 249 p., [12] leaves of plates";
		evalTotal(st, 265);
	}

	public void testTotal37()
	{
		String st = "xvii, 333 p. : maps, plans";
		evalTotal(st, 350);
	}

	public void testTotal38()
	{
		String st = "[80] p. of plates";
		evalTotal(st, 0);
	}

	public void testTotal39()
	{
		String st = "[93] p.";
		evalTotal(st, 93);
	}

	public void testTotal40()
	{
		String st = "1000 p. in various pagings";
		evalTotal(st, 0);
	}

	public void testTotal41()
	{
		String st = "122 folded leaves";
		evalTotal(st, 0);
	}

	public void testTotal42()
	{
		String st = "25 folded leaves of plates";
		evalTotal(st, 0);
	}

	public void testTotal43()
	{
		String st = "256 leaves in various pagings";
		evalTotal(st, 0);
	}

	public void testTotal44()
	{
		String st = "321 leaves";
		evalTotal(st, 321);
	}

	public void testTotal45()
	{
		String st = "327 p.";
		evalTotal(st, 327);
	}

	public void testTotal46()
	{
		String st = "333 p. : all ill.";
		evalTotal(st, 333);
	}

	public void testTotal47()
	{
		String st = "333 p. : chiefly maps";
		evalTotal(st, 333);
	}

	public void testTotal48()
	{
		String st = "333 p. : col. ill";
		evalTotal(st, 333);
	}

	public void testTotal49()
	{
		String st = "333 p. : ill., 3 forms, 1 map";
		evalTotal(st, 333);
	}

	public void testTotal50()
	{
		String st = "333 p. : maps";
		evalTotal(st, 333);
	}

	public void testTotal51()
	{
		String st = "xvii, 323 p.";
		evalTotal(st, 340);
	}

	public void testTotal52()
	{
		String st = "xxiv, 179 + p.";
		evalTotal(st, 203);
	}

	public void testTotal53()
	{
		String st = "xxvi p.";
		evalTotal(st, 26);
	}

	public void testTotal54()
	{
		String st = "[1], xxiii,171 p., plate :";
		evalTotal(st, 195);
	}

	public void testTotal55()
	{
		String st = "[118], 178 [i.e. 180], [5] p.";
		evalTotal(st, 303);
	}

	public void testTotal56()
	{
		String st = "[12], 156 p.";
		evalTotal(st, 168);
	}

	public void testTotal57()
	{
		String st = "[3], 215 p. :";
		evalTotal(st, 218);
	}

	public void testTotal58()
	{
		String st = "[4] v. (various pagings) :";
		evalTotal(st, 0);
	}

	public void testTotal59()
	{
		String st = "[5], 177 p. :";
		evalTotal(st, 182);
	}

	public void testTotal60()
	{
		String st = "[ix], 302 p. ;";
		evalTotal(st, 311);
	}

	public void testTotal61()
	{
		String st = "[x], 371 :";
		evalTotal(st, 381);
	}

	public void testTotal62()
	{
		String st = "[xii], 298 p., [24] p. of plates :";
		evalTotal(st, 310);
	}

	public void testTotal63()
	{
		String st = "[xiii], 309 p. :";
		evalTotal(st, 322);
	}

	public void testTotal64()
	{
		String st = "[xiv], 376 p., [8] p. of plates :";
		evalTotal(st, 390);
	}

	public void testTotal65()
	{
		String st = "[xxv], 303 p. ;";
		evalTotal(st, 328);
	}

	public void testTotal66()
	{
		String st = "<v.  > :";
		evalTotal(st, 0);
	}

	public void testTotal67()
	{
		String st = "<v.  > :";
		evalTotal(st, 0);
	}

	public void testTotal70()
	{
		String st = "1 v.";
		evalTotal(st, 0);
	}

	public void testTotal72()
	{
		String st = "1 v. (various pagings) :";
		evalTotal(st, 0);
	}

	public void testTotal73()
	{
		String st = "1 v. (various pagings) :";
		evalTotal(st, 0);
	}

	public void testTotal74()
	{
		String st = "1 v. ;";
		evalTotal(st, 0);
	}

	public void testTotal75()
	{
		String st = "1 v. ;";
		evalTotal(st, 0);
	}

	public void testTotal76()
	{
		String st = "135 p., [20] leaves of plates :";
		evalTotal(st, 135);
	}

	public void testTotal77()
	{
		String st = "136p :";
		evalTotal(st, 136);
	}

	public void testTotal78()
	{
		String st = "143p. :";
		evalTotal(st, 143);
	}

	public void testTotal79()
	{
		String st = "163p :";
		evalTotal(st, 163);
	}

	public void testTotal80()
	{
		String st = "184p :";
		evalTotal(st, 184);
	}

	public void testTotal81()
	{
		String st = "192p. :";
		evalTotal(st, 192);
	}

	public void testTotal82()
	{
		String st = "196 p., [4] leaves of plates :";
		evalTotal(st, 196);
	}

	public void testTotal83()
	{
		String st = "199 p., 8 p. of plates :";
		evalTotal(st, 199);
	}

	public void testTotal84()
	{
		String st = "2 booklets, 1 chart, 1 calculator, 1 pad of worksheets ;";
		evalTotal(st, 0);
	}

	public void testTotal85()
	{
		String st = "2 v.";
		evalTotal(st, 0);
	}

	public void testTotal86()
	{
		String st = "2 v. (138; v, 258 p.) :";
		evalTotal(st, 0);
	}

	public void testTotal87()
	{
		String st = "2 v. (625, 620 p.) ;";
		evalTotal(st, 0);
	}

	public void testTotal88()
	{
		String st = "2 v. (625, 620 p.) ;";
		evalTotal(st, 0);
	}

	public void testTotal89()
	{
		String st = "2 v. (625, 620 p.) ;";
		evalTotal(st, 0);
	}

	public void testTotal90()
	{
		String st = "2 v. (625, 620 p.) ;";
		evalTotal(st, 0);
	}

	public void testTotal91()
	{
		String st = "2 v. (625, 620 p.) ;";
		evalTotal(st, 0);
	}

	public void testTotal92()
	{
		String st = "2 v. (xi, 1824 p.) :";
		evalTotal(st, 0);
	}

	public void testTotal93()
	{
		String st = "2 v. (xxiv, 229 p. ; xlv, 292 p.) :";
		evalTotal(st, 0);
	}

	public void testTotal94()
	{
		String st = "2 v. (xxxiii, 1195 p.) ;";
		evalTotal(st, 0);
	}

	public void testTotal95()
	{
		String st = "2 v. :";
		evalTotal(st, 0);
	}

	public void testTotal96()
	{
		String st = "2 v. :";
		evalTotal(st, 0);
	}

	public void testTotal97()
	{
		String st = "2 v. :";
		evalTotal(st, 0);
	}

	public void testTotal98()
	{
		String st = "2 v. :";
		evalTotal(st, 0);
	}

	public void testTotal99()
	{
		String st = "2 v. :";
		evalTotal(st, 0);
	}

	public void testTotal100()
	{
		String st = "2 v. :";
		evalTotal(st, 0);
	}

	public void testTotal101()
	{
		String st = "2 v. ;";
		evalTotal(st, 0);
	}

	public void testTotal102()
	{
		String st = "2 v. ;";
		evalTotal(st, 0);
	}

	public void testTotal103()
	{
		String st = "222 p., [24] p. of plates :";
		evalTotal(st, 222);
	}

	public void testTotal104()
	{
		String st = "241, [9] p. :";
		evalTotal(st, 250);
	}

	public void testTotal105()
	{
		String st = "278 p., [85] p. of plates :";
		evalTotal(st, 278);
	}

	public void testTotal106()
	{
		String st = "281p ;";
		evalTotal(st, 281);
	}

	public void testTotal107()
	{
		String st = "3 v. (xxxvi, 2384 p.) :";
		evalTotal(st, 0);
	}

	public void testTotal108()
	{
		String st = "3 v. ;";
		evalTotal(st, 0);
	}

	public void testTotal109()
	{
		String st = "3 v. in 5 :";
		evalTotal(st, 0);
	}

	public void testTotal110()
	{
		String st = "32 v.";
		evalTotal(st, 0);
	}

	public void testTotal111()
	{
		String st = "342 p., [8] p. of plates :";
		evalTotal(st, 342);
	}

	public void testTotal112()
	{
		String st = "357p ;";
		evalTotal(st, 357);
	}

	public void testTotal113()
	{
		String st = "361 p., [8] p. of plates :";
		evalTotal(st, 361);
	}

	public void testTotal114()
	{
		String st = "4 v. :";
		evalTotal(st, 0);
	}

	public void testTotal115()
	{
		String st = "4 v. :";
		evalTotal(st, 0);
	}

	public void testTotal116()
	{
		String st = "4 v. in 6 :";
		evalTotal(st, 0);
	}

	public void testTotal118()
	{
		String st = "568p ;";
		evalTotal(st, 568);
	}

	public void testTotal119()
	{
		String st = "706 p., [90] p. of plates :";
		evalTotal(st, 706);
	}

	public void testTotal120()
	{
		String st = "787 p., [16] p. of plates :";
		evalTotal(st, 787);
	}

	public void testTotal121()
	{
		String st = "8 v. :";
		evalTotal(st, 0);
	}

	public void testTotal122()
	{
		String st = "8 v. :";
		evalTotal(st, 0);
	}

	public void testTotal123()
	{
		String st = "iv,150p ;";
		evalTotal(st, 154);
	}

	public void testTotal124()
	{
		String st = "ix, 315 p. [8] p. of plates :";
		evalTotal(st, 9);
	}

	public void testTotal125()
	{
		String st = "ix, 337 p., [8] p. of plates :";
		evalTotal(st, 346);
	}

	public void testTotal126()
	{
		String st = "ix,333p,[8]p of plates :";
		evalTotal(st, 342);
	}

	public void testTotal127()
	{
		String st = "lxiv, 614 p., [4] leaves of plates :";
		evalTotal(st, 678);
	}

	public void testTotal130()
	{
		String st = "p. cm.";
		evalTotal(st, 0);
	}

	public void testTotal131()
	{
		String st = "v, 219 p. :";
		evalTotal(st, 224);
	}

	public void testTotal132()
	{
		String st = "v, 231 p. :";
		evalTotal(st, 236);
	}

	public void testTotal133()
	{
		String st = "v, 398 p. :";
		evalTotal(st, 403);
	}

	public void testTotal134()
	{
		String st = "v, 422 p. :";
		evalTotal(st, 427);
	}

	public void testTotal135()
	{
		String st = "v. :";
		evalTotal(st, 0);
	}

	public void testTotal136()
	{
		String st = "v. :";
		evalTotal(st, 0);
	}

	public void testTotal137()
	{
		String st = "v. ;";
		evalTotal(st, 0);
	}

	public void testTotal138()
	{
		String st = "v. ;";
		evalTotal(st, 0);
	}

	public void testTotal144()
	{
		String st = "vii, 325p. :";
		evalTotal(st, 332);
	}

	public void testTotal145()
	{
		String st = "viii, 156 p., 32 p. of plates :";
		evalTotal(st, 164);
	}

	public void testTotal146()
	{
		String st = "viii, 225 p., [8]p. of plates :";
		evalTotal(st, 233);
	}

	public void testTotal147()
	{
		String st = "viii, 318 p., [16] p. of plates :";
		evalTotal(st, 326);
	}

	public void testTotal148()
	{
		String st = "viii,175 p. :";
		evalTotal(st, 183);
	}

	public void testTotal149()
	{
		String st = "x v, 205 p. ;";
		evalTotal(st, 220);
	}

	public void testTotal150()
	{
		String st = "x, 218 p., [8] p. of plates :";
		evalTotal(st, 228);
	}

	public void testTotal151()
	{
		String st = "x, 325 p., [24] p. of plates :";
		evalTotal(st, 335);
	}

	public void testTotal152()
	{
		String st = "xi, 132 p., [8] p. of plates :";
		evalTotal(st, 143);
	}

	public void testTotal153()
	{
		String st = "xi, 313 p., [16] p. of plates :";
		evalTotal(st, 324);
	}

	public void testTotal154()
	{
		String st = "xi, 358 p., [4] p. of plates :";
		evalTotal(st, 369);
	}

	public void testTotal155()
	{
		String st = "xi, 87, 617 p. :";
		evalTotal(st, 715);
	}

	public void testTotal156()
	{
		String st = "xi,212p :";
		evalTotal(st, 223);
	}

	public void testTotal157()
	{
		String st = "xii, 178 p., 16 leaves of plates :";
		evalTotal(st, 190);
	}

	public void testTotal158()
	{
		String st = "xii, 235 p., [4] p. of plates :";
		evalTotal(st, 247);
	}

	public void testTotal159()
	{
		String st = "xii, 275 p., [12] p. of plates :";
		evalTotal(st, 287);
	}

	public void testTotal160()
	{
		String st = "xii, 387p., [32] leaves of plates :";
		evalTotal(st, 399);
	}

	public void testTotal161()
	{
		String st = "xii,256p ;";
		evalTotal(st, 268);
	}

	public void testTotal162()
	{
		String st = "xii,335p ;";
		evalTotal(st, 347);
	}

	public void testTotal163()
	{
		String st = "xiii, 234 p. ;20 cm.";
		evalTotal(st, 247);
	}

	public void testTotal164()
	{
		String st = "xiii, 460 p., [8] p. of plates :";
		evalTotal(st, 473);
	}

	public void testTotal165()
	{
		String st = "xiii, 99, [1] p. ;";
		evalTotal(st, 113);
	}

	public void testTotal166()
	{
		String st = "xiv, 345 p., [9] p. of plates :";
		evalTotal(st, 359);
	}

	public void testTotal167()
	{
		String st = "xiv, 345 p., [9] p. of plates :";
		evalTotal(st, 359);
	}

	public void testTotal168()
	{
		String st = "xiv, 347 p., [6] p. of plates :";
		evalTotal(st, 361);
	}

	public void testTotal169()
	{
		String st = "xiv, 347 p., [6] p. of plates :";
		evalTotal(st, 361);
	}

	public void testTotal170()
	{
		String st = "xiv, 368 p., [16] p. of plates :";
		evalTotal(st, 382);
	}

	public void testTotal171()
	{
		String st = "xiv, 769 p. [in 2 parts] :";
		evalTotal(st, 783);
	}

	public void testTotal172()
	{
		String st = "xix, 1098 p., [12] p. of plates [some col] : ill., maps ; 25 cm.";
		evalTotal(st, 1117);
	}

	public void testTotal173()
	{
		String st = "xix,496 p. :";
		evalTotal(st, 515);
	}

	public void testTotal174()
	{
		String st = "xlv, 687, [143] p. :";
		evalTotal(st, 875);
	}

	public void testTotal175()
	{
		String st = "xv, 220 p., [14] p. of plates :";
		evalTotal(st, 235);
	}

	public void testTotal176()
	{
		String st = "xv, 234 p., [8] p. of plates :";
		evalTotal(st, 249);
	}

	public void testTotal177()
	{
		String st = "xv, 252 p., [8] leaves of plates :";
		evalTotal(st, 267);
	}

	public void testTotal178()
	{
		String st = "xv, 354 p., [8] leaves of plates :";
		evalTotal(st, 369);
	}

	public void testTotal179()
	{
		String st = "xv, 363 p., [8] p. of plates :";
		evalTotal(st, 378);
	}

	public void testTotal180()
	{
		String st = "xv, 701 p., (16) p. of plates :";
		evalTotal(st, 716);
	}

	public void testTotal181()
	{
		String st = "xvi, 1162 p., [8] p. of plates :";
		evalTotal(st, 1178);
	}

	public void testTotal182()
	{
		String st = "xvi, 304 p., [14] p. of plates :";
		evalTotal(st, 320);
	}

	public void testTotal183()
	{
		String st = "xvi, 365 p., [8 p.] of plates :";
		evalTotal(st, 381);
	}

	public void testTotal184()
	{
		String st = "xvii, 198 p., [14] p. of plates :";
		evalTotal(st, 215);
	}

	public void testTotal185()
	{
		String st = "xvii, 216 p., [24] p. of plates :";
		evalTotal(st, 233);
	}

	public void testTotal186()
	{
		String st = "xvii, 374 p., [16] p. of plates :";
		evalTotal(st, 391);
	}

	public void testTotal187()
	{
		String st = "xvii, 389 :";
		evalTotal(st, 406);
	}

	public void testTotal188()
	{
		String st = "xvii, 542 p., [2] leaves of plates :";
		evalTotal(st, 559);
	}

	public void testTotal189()
	{
		String st = "xviii, 168 p., [1] leaf of plates :";
		evalTotal(st, 186);
	}

	public void testTotal190()
	{
		String st = "xx, 390 p., [8] leaves of plates :";
		evalTotal(st, 410);
	}

	public void testTotal191()
	{
		String st = "xxi, 484 p., [16] p. of plates :";
		evalTotal(st, 505);
	}

	public void testTotal192()
	{
		String st = "xxi,308 p. :";
		evalTotal(st, 329);
	}

	public void testTotal193()
	{
		String st = "xxiv, 211 p., [64] p. of plates :";
		evalTotal(st, 235);
	}

	public void testTotal194()
	{
		String st = "xxiv, 607, [73] p. :";
		evalTotal(st, 704);
	}

	public void testTotal195()
	{
		String st = "xxix, 758 p., [16] p. of plates :";
		evalTotal(st, 787);
	}

	public void testTotal196()
	{
		String st = "xxvi, 308 p., [40] p. of plates :";
		evalTotal(st, 334);
	}

	public void testTotal197()
	{
		String st = "xxviii, [11], 626 p., [24] p. of plates :";
		evalTotal(st, 665);
	}

	public void testTotal198()
	{
		String st = "xxx, 1190, [76] p. :";
		evalTotal(st, 1296);
	}

	public void testTotal199()
	{
		String st = "xxx, 274 p., [12] p. of plates :";
		evalTotal(st, 304);
	}

	public void testTotal200()
	{
		String st = "xxx,528p :";
		evalTotal(st, 558);
	}

	public void testTotal201()
	{
		String st = "xxxi, 749 p. [4] p. of plates :";
		evalTotal(st, 31);
	}

	public void testTotal202()
	{
		String st = "xxxii, 271 p., [95] leaves of plates :";
		evalTotal(st, 303);
	}

	public void testTotal203()
	{
		String st = "xxxvi, 1879 p., [2] p. of plates :";
		evalTotal(st, 1915);
	}

	public void testTotal204()
	{
		String st = "xxxvi, 2240, 89 p. :";
		evalTotal(st, 2365);
	}

	public void testTotal205()
	{
		String st = "xxxviii, 979 p., [2] p. of plates :";
		evalTotal(st, 1017);
	}

	public void testTotal5()
	{
		String st = "1 v. (loose-leaf)";
		evalTotal(st, 0);
	}

	public void testTotal68()
	{
		String st = "1 CD-ROM :";
		evalTotal(st, 0);
	}

	public void testTotal69()
	{
		String st = "1 CD-ROM :";
		evalTotal(st, 0);
	}

	public void testTotal71()
	{
		String st = "1 v. (loose-leaf) ;";
		evalTotal(st, 0);
	}

	public void testTotal117()
	{
		String st = "5 v. (loose-leaf) ;";
		evalTotal(st, 0);
	}

	public void testRange1()
	{
		String st = "1-5";
		evalRange(st, 5);
	}

	public void testRange2()
	{
		String st = "1-5 & 20-25";
		evalRange(st, 11);
	}

	public void testRange3()
	{
		String st = "41-50, [45]";
		evalRange(st, 55);
	}

	public void testRange4()
	{
		String st = "100";
		evalRange(st, 1);
	}

	public void testRange5()
	{
		String st = "[7]";
		evalRange(st, 7);
	}
}
