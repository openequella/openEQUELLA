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

package com.tle.core.institution.migration.v64;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.google.inject.Singleton;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.InternalI18NString;
import com.tle.common.i18n.KeyString;
import com.tle.common.i18n.LangUtils;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DisplayTempateTitleMigration extends AbstractHibernateDataMigration {
  private static PluginResourceHelper r =
      ResourcesService.getResourceHelper(DisplayTempateTitleMigration.class);

  @Override
  public MigrationInfo createMigrationInfo() {
    // Legacy string key
    return new MigrationInfo(
        "com.tle.core.entity.services.migration.v64.displaytemplatetitle.title");
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    final List<FakeItemdefBlobs> idbs = session.createQuery("FROM ItemdefBlobs").list();
    for (FakeItemdefBlobs idb : idbs) {
      final String sectionsXml = idb.getItemSummarySections();
      if (!Check.isEmpty(sectionsXml)) {
        PropBagEx xml = new PropBagEx(sectionsXml);

        PropBagIterator iter =
            xml.iterator("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");

        while (iter.hasNext()) {
          PropBagEx config = iter.next();
          if (Check.isEmpty(config.getNode("uuid"))) {
            config.createNode("uuid", UUID.randomUUID().toString());
          }

          if (config.getSubtree("bundleTitle") != null) {
            continue;
          }

          String configValue = config.getNode("value");
          String title = config.getNode("bundleTitle");
          switch (configValue) {
            case "attachmentsSection":
              InternalI18NString attachmentTitle =
                  new KeyString(r.key("summary.content.attachments.title"));
              addNewBundleTitle(attachmentTitle.toString(), config);
              break;
            case "commentsSection":
              InternalI18NString commentTitle = new KeyString(r.key("comments.addnew"));
              addNewBundleTitle(commentTitle.toString(), config);
              break;
            case "citationSummarySection":
              InternalI18NString citationTitle =
                  new KeyString("com.equella.core.citation.summary.title");
              addNewBundleTitle(citationTitle.toString(), config);
              break;
            default:
              if (!Check.isEmpty(title)) {
                addNewBundleTitle(title, config);
              } else {
                addOtherSectionNames(configValue, config);
              }
              break;
          }
        }
        // Update
        idb.setItemSummarySections(xml.toString());
        session.update(idb);
        session.flush();
      }
      result.incrementStatus();
    }
  }

  public static void addOtherSectionNames(String sectionValue, PropBagEx config) {
    switch (sectionValue) {
      case "purchasedItemSummarySection":
        addNewBundleTitle("Purchase details", config);
        break;
      case "paymentSummarySection":
        addNewBundleTitle("Pricing Information", config);
        break;
      case "basicSection":
        addNewBundleTitle("Basic Information", config);
        break;
      case "displayNodes":
        addNewBundleTitle("Extra Metadata", config);
        break;
      case "xsltSection":
        addNewBundleTitle("XSLT", config);
        break;
      case "freemarkerSection":
        addNewBundleTitle("Scripted section", config);
        break;
      default:
        addNewBundleTitle("", config);
        break;
    }
  }

  public static void addNewBundleTitle(String title, PropBagEx config) {
    LanguageString langstring =
        LangUtils.createLanguageString(null, CurrentLocale.getLocale(), title);
    PropBagEx newTitle = config.newSubtree("bundleTitle");
    newTitle.setNode("", "");
    PropBagEx entry = newTitle.newSubtree("strings/entry");
    entry.setNode("string", langstring.getLocale());

    PropBagEx ls = entry.newSubtree("com.tle.beans.entity.LanguageString");
    ls.setNode("locale", langstring.getLocale());
    ls.setNode("priority", langstring.getPriority());
    ls.setNode("text", langstring.getText());
    ls.setNode("bundle/@reference", "../../../..");
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return count(session, "FROM ItemdefBlobs");
  }

  @Override
  public boolean isBackwardsCompatible() {
    return false;
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class[] {FakeItemdefBlobs.class};
  }

  @Entity(name = "ItemdefBlobs")
  @AccessType("field")
  public static class FakeItemdefBlobs {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Lob public String itemSummarySections;

    public String getItemSummarySections() {
      return itemSummarySections;
    }

    public void setItemSummarySections(String itemSummarySections) {
      this.itemSummarySections = itemSummarySections;
    }
  }
}
