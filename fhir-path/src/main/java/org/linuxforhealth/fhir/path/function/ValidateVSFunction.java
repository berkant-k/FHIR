/*
 * (C) Copyright IBM Corp. 2020, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.path.function;

import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.empty;
import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.getElementNode;
import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.isResourceNode;
import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.isStringValue;
import static org.linuxforhealth.fhir.path.util.FHIRPathUtil.singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.linuxforhealth.fhir.model.resource.Parameters;
import org.linuxforhealth.fhir.model.resource.ValueSet;
import org.linuxforhealth.fhir.model.type.Boolean;
import org.linuxforhealth.fhir.model.type.Code;
import org.linuxforhealth.fhir.model.type.CodeableConcept;
import org.linuxforhealth.fhir.model.type.Coding;
import org.linuxforhealth.fhir.model.type.DateTime;
import org.linuxforhealth.fhir.model.type.Element;
import org.linuxforhealth.fhir.model.type.code.IssueSeverity;
import org.linuxforhealth.fhir.model.type.code.IssueType;
import org.linuxforhealth.fhir.path.FHIRPathElementNode;
import org.linuxforhealth.fhir.path.FHIRPathNode;
import org.linuxforhealth.fhir.path.FHIRPathResourceNode;
import org.linuxforhealth.fhir.path.evaluator.FHIRPathEvaluator.EvaluationContext;
import org.linuxforhealth.fhir.term.service.ValidationOutcome;
import org.linuxforhealth.fhir.term.service.ValidationParameters;

public class ValidateVSFunction extends FHIRPathAbstractTermFunction {
    @Override
    public String getName() {
        return "validateVS";
    }

    @Override
    public int getMinArity() {
        return 2;
    }

    @Override
    public int getMaxArity() {
        return 3;
    }

    @Override
    protected Map<String, Function<String, Element>> buildElementFactoryMap() {
        Map<String, Function<String, Element>> map = new HashMap<>();
        map.put("date", DateTime::of);
        map.put("abstract", Boolean::of);
        map.put("displayLanguage", Code::of);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Collection<FHIRPathNode> apply(EvaluationContext evaluationContext, Collection<FHIRPathNode> context, List<Collection<FHIRPathNode>> arguments) {
        if (!isTermServiceNode(context) ||
                (!isResourceNode(arguments.get(0)) && !isStringValue(arguments.get(0))) ||
                !isCodedElementNode(arguments.get(1)) ||
                (arguments.size() == 3 && !isStringValue(arguments.get(2)))) {
            return empty();
        }
        ValueSet valueSet = getResource(arguments, ValueSet.class);
        FHIRPathElementNode codedElementNode = getElementNode(arguments.get(1));
        Element codedElement = getCodedElement(evaluationContext.getTree(), codedElementNode);
        Parameters parameters = getParameters(arguments);
        ValidationOutcome outcome = codedElement.is(CodeableConcept.class) ?
                service.validateCode(valueSet, codedElement.as(CodeableConcept.class), ValidationParameters.from(parameters)) :
                service.validateCode(valueSet, codedElement.as(Coding.class), ValidationParameters.from(parameters));
        if (Boolean.FALSE.equals(outcome.getResult()) && outcome.getMessage() != null) {
            generateIssue(evaluationContext, IssueSeverity.ERROR, IssueType.CODE_INVALID, outcome.getMessage().getValue(), "%terminologies");
        }
        return singleton(FHIRPathResourceNode.resourceNode(outcome.toParameters()));
    }
}
