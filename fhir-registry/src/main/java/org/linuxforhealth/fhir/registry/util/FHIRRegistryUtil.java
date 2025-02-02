/*
 * (C) Copyright IBM Corp. 2019, 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.linuxforhealth.fhir.registry.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.linuxforhealth.fhir.model.format.Format;
import org.linuxforhealth.fhir.model.parser.FHIRParser;
import org.linuxforhealth.fhir.model.resource.ActivityDefinition;
import org.linuxforhealth.fhir.model.resource.CapabilityStatement;
import org.linuxforhealth.fhir.model.resource.ChargeItemDefinition;
import org.linuxforhealth.fhir.model.resource.CodeSystem;
import org.linuxforhealth.fhir.model.resource.CompartmentDefinition;
import org.linuxforhealth.fhir.model.resource.ConceptMap;
import org.linuxforhealth.fhir.model.resource.EventDefinition;
import org.linuxforhealth.fhir.model.resource.Evidence;
import org.linuxforhealth.fhir.model.resource.EvidenceVariable;
import org.linuxforhealth.fhir.model.resource.ExampleScenario;
import org.linuxforhealth.fhir.model.resource.GraphDefinition;
import org.linuxforhealth.fhir.model.resource.ImplementationGuide;
import org.linuxforhealth.fhir.model.resource.Library;
import org.linuxforhealth.fhir.model.resource.Measure;
import org.linuxforhealth.fhir.model.resource.MessageDefinition;
import org.linuxforhealth.fhir.model.resource.NamingSystem;
import org.linuxforhealth.fhir.model.resource.OperationDefinition;
import org.linuxforhealth.fhir.model.resource.PlanDefinition;
import org.linuxforhealth.fhir.model.resource.Questionnaire;
import org.linuxforhealth.fhir.model.resource.ResearchDefinition;
import org.linuxforhealth.fhir.model.resource.ResearchElementDefinition;
import org.linuxforhealth.fhir.model.resource.Resource;
import org.linuxforhealth.fhir.model.resource.SearchParameter;
import org.linuxforhealth.fhir.model.resource.StructureDefinition;
import org.linuxforhealth.fhir.model.resource.StructureMap;
import org.linuxforhealth.fhir.model.resource.TerminologyCapabilities;
import org.linuxforhealth.fhir.model.resource.TestScript;
import org.linuxforhealth.fhir.model.resource.ValueSet;
import org.linuxforhealth.fhir.model.util.ModelSupport;
import org.linuxforhealth.fhir.registry.resource.FHIRRegistryResource;
import org.linuxforhealth.fhir.registry.resource.FHIRRegistryResource.Version;
import org.linuxforhealth.fhir.registry.util.Index.Entry;

public final class FHIRRegistryUtil {
    private static final Logger log = Logger.getLogger(FHIRRegistryUtil.class.getName());

    private static final Set<Class<? extends Resource>> DEFINITIONAL_RESOURCE_TYPES = new HashSet<>(Arrays.asList(
        ActivityDefinition.class,
        CapabilityStatement.class,
        ChargeItemDefinition.class,
        CodeSystem.class,
        CompartmentDefinition.class,
        ConceptMap.class,
        EventDefinition.class,
        Evidence.class,
        EvidenceVariable.class,
        ExampleScenario.class,
        GraphDefinition.class,
        ImplementationGuide.class,
        Library.class,
        Measure.class,
        MessageDefinition.class,
        NamingSystem.class,
        OperationDefinition.class,
        PlanDefinition.class,
        Questionnaire.class,
        ResearchDefinition.class,
        ResearchElementDefinition.class,
        SearchParameter.class,
        StructureDefinition.class,
        StructureMap.class,
        TerminologyCapabilities.class,
        TestScript.class,
        // TODO: check for R4B resources to add
        ValueSet.class));

    private FHIRRegistryUtil() { }

    public static String getUrl(Resource resource) {
        DefinitionalResourceVisitor visitor = new DefinitionalResourceVisitor();
        resource.accept(visitor);
        return visitor.getUrl();
    }

    public static String getVersion(Resource resource) {
        DefinitionalResourceVisitor visitor = new DefinitionalResourceVisitor();
        resource.accept(visitor);
        return visitor.getVersion();
    }

    public static boolean isDefinitionalResource(Resource resource) {
        return isDefinitionalResourceType(resource.getClass());
    }

    /**
     * Throw an {@link IllegalArgumentException} if the resource type is not a definitional resource type per:
     * <a href="http://hl7.org/fhir/definition.html">http://hl7.org/fhir/definition.html</a>
     *
     * @param resourceType
     *     the resourceType
     */
    public static void requireDefinitionalResourceType(Class<? extends Resource> resourceType) {
        if (!isDefinitionalResourceType(resourceType)) {
            throw new IllegalArgumentException(resourceType.getSimpleName() + " is not a definitional resource type");
        }
    }

    /**
     * Indicates whether the resource type is a definitional resource type per:
     * <a href="http://hl7.org/fhir/definition.html">http://hl7.org/fhir/definition.html</a>
     *
     * @param resourceType
     *     the resource type
     * @return
     *     true if the resource type is a definitional resource, false otherwise
     */
    public static boolean isDefinitionalResourceType(Class<? extends Resource> resourceType) {
        return DEFINITIONAL_RESOURCE_TYPES.contains(resourceType);
    }

    public static Resource loadResource(String path) {
        try (InputStream in = FHIRRegistryUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                log.log(Level.WARNING, "Resource at '" + path + "' was not found");
                return null;
            }
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to load resource: " + path, e);
        }
        return null;
    }

    public static Collection<FHIRRegistryResource> getRegistryResources(String packageId) {
        List<FHIRRegistryResource> resources = new ArrayList<>();
        String packageDirectory = packageId.replace(".", "/") + "/package";
        for (Entry entry : readIndex(packageDirectory + "/.index.json")) {
            resources.add(new PackageRegistryResource(
                ModelSupport.getResourceType(entry.getResourceType()),
                entry.getId(),
                entry.getUrl(),
                (entry.getVersion() != null) ? Version.from(entry.getVersion()) : Version.NO_VERSION,
                entry.getKind(),
                entry.getType(),
                packageDirectory + "/" + entry.getFileName()));
        }
        return Collections.unmodifiableList(resources);
    }

    public static Set<Entry> readIndex(String indexPath) {
        log.info("Loading index: " + indexPath);
        try (InputStream in = FHIRRegistryUtil.class.getClassLoader().getResourceAsStream(indexPath)) {
            if (in == null) {
                log.log(Level.WARNING, "Index '" + indexPath + "' was not found");
                return Collections.emptySet();
            }
            Index index = new Index();
            index.load(in);
            return index.getEntries();
        } catch (Exception e) {
            log.log(Level.WARNING, "Unexpected error while loading index '" + indexPath + "'", e);
        }
        return Collections.emptySet();
    }
}