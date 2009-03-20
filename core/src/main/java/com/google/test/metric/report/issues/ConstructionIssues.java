/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.test.metric.report.issues;

import com.google.test.metric.CyclomaticCost;
import com.google.test.metric.MethodCost;
import com.google.test.metric.MethodInvokationCost;
import com.google.test.metric.ViolationCost;
import static com.google.test.metric.collection.LazyHashMap.newLazyHashMap;
import static com.google.test.metric.report.issues.ConstructionIssues.ConstructionType.*;
import com.google.test.metric.report.issues.ConstructionIssues.ConstructionType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Issues that arise from expensive constructors.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class ConstructionIssues extends IssuesCategory<ConstructionType> {
  public ConstructionIssues(Map<ConstructionType, List<Issue>> issues) {
    super(issues);
  }

  public ConstructionIssues() {
    super();
  }

  public void workInConstructor(MethodCost methodCost, Issue issue, long totalComplexityCost, long totalGlobalCost) {
    long complexityCost = 0, staticCost = 0, nonMockableCost = 0;
    for (ViolationCost costSource : methodCost.getViolationCosts()) {
      if (costSource instanceof CyclomaticCost) {
        complexityCost += costSource.getCost().getCyclomaticComplexityCost();
      }
      if (costSource instanceof MethodInvokationCost) {
        if (((MethodInvokationCost)costSource).getMethodCost().isStatic()) {
          staticCost += costSource.getCost().getCyclomaticComplexityCost();
        } else {
          nonMockableCost += costSource.getCost().getCyclomaticComplexityCost();
        }
      }
    }
    if (staticCost > 0) {
      issue.setContributionToClassCost(staticCost / (float)totalComplexityCost);
      issues.get(STATIC_METHOD).add(issue);
    }
    if (nonMockableCost > 0) {
      issue.setContributionToClassCost(nonMockableCost / (float)totalComplexityCost);
      issues.get(NEW_OPERATOR).add(issue);
    }
    if (complexityCost > 0) {
      issue.setContributionToClassCost(complexityCost / (float)totalComplexityCost);
      issues.get(COMPLEXITY).add(issue);
    }
  }

  @Override
  Class<ConstructionType> getTypeLiteral() {
    return ConstructionType.class;
  }

  @Override
  public String getName() {
    return "Construction";
  }

  public List<Issue> getComplexityIssues() {
    return issues.get(COMPLEXITY);
  }

  public List<Issue> getStaticMethodIssues() {
    return issues.get(STATIC_METHOD);
  }

  public List<Issue> getNewOperatorIssues() {
    return issues.get(NEW_OPERATOR);
  }

  public enum ConstructionType {
    STATIC_INIT,
    COMPLEXITY,
    STATIC_METHOD,
    NEW_OPERATOR,
    SETTER
  }
}
