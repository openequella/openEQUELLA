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

package com.tle.web.wizard.controls;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.js.impl.CombinedDisableable;
import com.tle.web.wizard.WebWizardPage;

@NonNullByDefault
public interface WebControl extends HTMLControl, Section, HtmlRenderer
{
	WebWizardPage getWebWizardPage();

	void setWebWizardPage(WebWizardPage page);

	void setInColumn(boolean b);

	boolean isInColumn();

	HTMLControl getWrappedControl();

	void setWrappedControl(HTMLControl control);

	CombinedDisableable getDisabler(SectionInfo info);

	// Yes this is rubbish
	void doEditsIfRequired(SectionInfo info);

	void doReads(SectionInfo info);

	void setNested(boolean nested);

	boolean isNested();

	void clearTargets(SectionInfo info, PropBagEx itemxml);

	boolean canHaveChildren();

	void deletedFromParent(SectionInfo info);
}
