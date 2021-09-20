/*
 * (C) Copyright IBM Corp. 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.operation.erase.mock;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.ws.rs.core.MultivaluedMap;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.patch.FHIRPatch;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.OperationOutcome.Builder;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.code.IssueType;
import com.ibm.fhir.persistence.FHIRPersistenceTransaction;
import com.ibm.fhir.persistence.ResourceEraseRecord;
import com.ibm.fhir.persistence.ResourceEraseRecord.Status;
import com.ibm.fhir.persistence.SingleResourceResult;
import com.ibm.fhir.persistence.erase.EraseDTO;
import com.ibm.fhir.persistence.exception.FHIRPersistenceException;
import com.ibm.fhir.persistence.helper.FHIRTransactionHelper;
import com.ibm.fhir.persistence.interceptor.FHIRPersistenceEvent;
import com.ibm.fhir.persistence.payload.PayloadKey;
import com.ibm.fhir.search.context.FHIRSearchContext;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.operation.spi.FHIRRestOperationResponse;
import com.ibm.fhir.server.util.FHIROperationUtil;
import com.ibm.fhir.server.util.FHIRRestHelper.Interaction;

/**
 * Helper for Mocking failure tests with the FHIR Resource Helpers
 */
public class MockFHIRResourceHelpers implements FHIRResourceHelpers {
    private boolean throwEx;
    private boolean partial;
    private boolean notFound;
    private boolean greaterThanGreatest = false;
    private boolean latest = false;

    public MockFHIRResourceHelpers(boolean throwEx, boolean partial) {
        this.throwEx = throwEx;
        this.partial = partial;
        this.notFound = false;
    }

    public MockFHIRResourceHelpers(boolean throwEx, boolean partial, boolean notFound) {
        this.throwEx = throwEx;
        this.partial = partial;
        this.notFound = notFound;
    }

    public MockFHIRResourceHelpers(boolean throwEx, boolean partial, boolean notFound, boolean greaterThanGreatest, boolean latest) {
        this.throwEx = throwEx;
        this.partial = partial;
        this.notFound = notFound;
        this.greaterThanGreatest = greaterThanGreatest;
        this.latest = latest;
    }

    @Override
    public FHIRPersistenceTransaction getTransaction() throws Exception {

        return null;
    }

    @Override
    public int doReindex(FHIROperationContext operationContext, Builder operationOutcomeResult, Instant tstamp, List<Long> indexIds,
        String resourceLogicalId) throws Exception {
        return 0;
    }

    @Override
    public ResourceEraseRecord doErase(FHIROperationContext operationContext, EraseDTO eraseDto) throws FHIROperationException {
        if (throwEx) {
            throw FHIROperationUtil.buildExceptionWithIssue("Bad Deal", IssueType.EXCEPTION, new Exception("Test"));
        }

        ResourceEraseRecord record = new ResourceEraseRecord();
        if (partial) {
            record.setStatus(Status.PARTIAL);
        }

        if (notFound) {
            record.setStatus(Status.NOT_FOUND);
        } else if (greaterThanGreatest) {
            record.setStatus(Status.NOT_SUPPORTED_GREATER);
        } else if (latest) {
            record.setStatus(Status.NOT_SUPPORTED_LATEST);
        }
        return record;
    }

    @Override
    public FHIRRestOperationResponse doCreate(String type, Resource resource, String ifNoneExist, boolean doValidation) throws Exception {

        return null;
    }

    @Override
    public FHIRRestOperationResponse doUpdate(String type, String id, Resource newResource, String ifMatchValue, String searchQueryString,
        boolean skippableUpdate, boolean doValidation) throws Exception {

        return null;
    }

    @Override
    public FHIRRestOperationResponse doPatch(String type, String id, FHIRPatch patch, String ifMatchValue, String searchQueryString, boolean skippableUpdate)
        throws Exception {

        return null;
    }

    @Override
    public FHIRRestOperationResponse doDelete(String type, String id, String searchQueryString) throws Exception {

        return null;
    }

    @Override
    public SingleResourceResult<? extends Resource> doRead(String type, String id, boolean throwExcOnNull, boolean includeDeleted, Resource contextResource,
        MultivaluedMap<String, String> queryParameters) throws Exception {

        return null;
    }

    @Override
    public Resource doVRead(String type, String id, String versionId, MultivaluedMap<String, String> queryParameters) throws Exception {

        return null;
    }

    @Override
    public Bundle doHistory(String type, String id, MultivaluedMap<String, String> queryParameters, String requestUri) throws Exception {

        return null;
    }

    @Override
    public Bundle doHistory(MultivaluedMap<String, String> queryParameters, String requestUri) throws Exception {

        return null;
    }

    @Override
    public Bundle doSearch(String type, String compartment, String compartmentId, MultivaluedMap<String, String> queryParameters, String requestUri,
        Resource contextResource) throws Exception {

        return null;
    }

    @Override
    public Resource doInvoke(FHIROperationContext operationContext, String resourceTypeName, String logicalId, String versionId,
        Resource resource, MultivaluedMap<String, String> queryParameters) throws Exception {

        return null;
    }

    @Override
    public Bundle doBundle(Bundle bundle, boolean skippableUpdates) throws Exception {

        return null;
    }

    @Override
    public List<Long> doRetrieveIndex(FHIROperationContext operationContext, String resourceTypeName, int count, java.time.Instant notModifiedAfter, Long afterIndexId) throws Exception {

        return null;
    }

    @Override
    public Bundle doSearch(String type, String compartment, String compartmentId, MultivaluedMap<String, String> queryParameters, String requestUri,
        Resource contextResource, boolean checkIfInteractionAllowed) throws Exception {
        return null;
    }

    @Override
    public String generateResourceId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Future<PayloadKey> storePayload(Resource resource, String logicalId, int newVersionNumber) {
        return null;
    }

    @Override
    public void validateInteraction(Interaction interaction, String resourceType) throws FHIROperationException {
    }

    @Override
    public FHIRRestOperationResponse doCreateMeta(FHIRPersistenceEvent event, List<Issue> warnings, String type, Resource resource, 
        String ifNoneExist) throws Exception {
        return null;
    }

    @Override
    public FHIRRestOperationResponse doCreatePersist(FHIRPersistenceEvent event, List<Issue> warnings, Resource resource) throws Exception {
        return null;
    }

    @Override
    public FHIRRestOperationResponse doUpdateMeta(FHIRPersistenceEvent event, String type, String id, FHIRPatch patch, Resource newResource, String ifMatchValue, String searchQueryString,
        boolean skippableUpdate, boolean doValidation, List<Issue> warnings) throws Exception {
        return null;
    }

    @Override
    public FHIRRestOperationResponse doPatchOrUpdatePersist(FHIRPersistenceEvent event, String type, String id, boolean isPatch,
        Resource newResource, Resource prevResource, List<Issue> warnings, boolean isDeleted) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> buildPersistenceEventProperties(String type, String id, String version, FHIRSearchContext searchContext)
        throws FHIRPersistenceException {
        return null;
    }
}