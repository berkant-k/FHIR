/**
 * (C) Copyright IBM Corp. 2016,2017,2018,2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.watsonhealth.fhir.persistence.test.common;

import static org.testng.AssertJUnit.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;

import com.ibm.watsonhealth.fhir.model.Resource;
import com.ibm.watsonhealth.fhir.model.test.FHIRModelTestBase;
import com.ibm.watsonhealth.fhir.persistence.FHIRPersistence;
import com.ibm.watsonhealth.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.watsonhealth.fhir.search.context.FHIRSearchContext;
import com.ibm.watsonhealth.fhir.search.exception.FHIRSearchException;
import com.ibm.watsonhealth.fhir.search.util.SearchUtil;

/**
 * This is a common abstract base class for all persistence-related tests.
 */
public abstract class AbstractPersistenceTest extends FHIRModelTestBase {
    
    // The persistence layer instance to be used by the tests. 
    protected static FHIRPersistence persistence = null;
    
    // Each concrete subclass needs to implement this to obtain the appropriate persistence layer instance.
    public abstract FHIRPersistence getPersistenceImpl() throws Exception;
    
    @BeforeClass
    public void setUp() throws Exception {
        persistence = getPersistenceImpl();
    }
    
    protected List<Resource> runQueryTest(Class<? extends Resource> resourceType, FHIRPersistence persistence, String parmName, String parmValue) throws FHIRSearchException, FHIRPersistenceException {
        Map<String, List<String>> queryParms = new HashMap<String, List<String>>();
        if (parmName != null && parmValue != null) {
            queryParms.put(parmName, Collections.singletonList(parmValue));
        }
        FHIRSearchContext context = SearchUtil.parseQueryParameters(resourceType, queryParms);
        List<Resource> resources = persistence.search(resourceType, context);
        assertNotNull(resources);
        return resources;
    }
}
