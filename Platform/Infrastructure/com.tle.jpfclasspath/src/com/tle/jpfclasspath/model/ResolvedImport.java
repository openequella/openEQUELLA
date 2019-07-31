package com.tle.jpfclasspath.model;

import com.tle.jpfclasspath.parser.ModelPrerequisite;

public class ResolvedImport {
  private final ModelPrerequisite prerequisite;
  private final IResolvedPlugin resolved;

  public ResolvedImport(ModelPrerequisite modelPrerequisite, IResolvedPlugin resolvedDep) {
    this.prerequisite = modelPrerequisite;
    this.resolved = resolvedDep;
  }

  public ModelPrerequisite getPrerequisite() {
    return prerequisite;
  }

  public IResolvedPlugin getResolved() {
    return resolved;
  }
}
