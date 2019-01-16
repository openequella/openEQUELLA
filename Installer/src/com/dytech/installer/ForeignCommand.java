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

package com.dytech.installer;

import java.util.Iterator;

import com.dytech.common.text.ResolverException;
import com.dytech.common.text.Substitution;
import com.dytech.devlib.PropBagEx;
import com.dytech.installer.commands.Command;

public abstract class ForeignCommand extends Command {
  protected static Substitution resolver = null;

  protected PropBagEx commandBag;
  protected PropBagEx resultBag;

  public ForeignCommand(PropBagEx commandBag, PropBagEx resultBag) {
    this.commandBag = commandBag;
    this.resultBag = resultBag;

    if (resolver == null) {
      resolver = new Substitution(new XpathResolver(resultBag), "${ }");
    }
  }

  protected String getForeignValue(String key) throws InstallerException {
    Iterator iter = commandBag.iterator("foreign");
    while (iter.hasNext()) {
      PropBagEx foreign = (PropBagEx) iter.next();
      if (foreign.getNode("@key").equals(key)) {
        String value = foreign.getNode("@value");
        try {
          return resolver.resolve(value);
        } catch (ResolverException e) {
          throw new InstallerException(e);
        }
      }
    }
    return null;
  }
}
