/*
 * Copyright (C) 2012-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sos.binding.rest.resources.observations;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.sosREST.x10.LinkType;
import net.opengis.sosREST.x10.ObservationDocument;
import net.opengis.sosREST.x10.ObservationType;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.shetland.ogc.om.OmObservation;
import org.n52.shetland.ogc.ows.exception.NoApplicableCodeException;
import org.n52.shetland.ogc.ows.exception.OperationNotSupportedException;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.ows.extension.Extensions;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesRequest;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.request.GetObservationByIdRequest;
import org.n52.shetland.ogc.sos.request.GetObservationRequest;
import org.n52.shetland.ogc.sos.request.InsertObservationRequest;
import org.n52.shetland.ogc.swes.SwesExtension;
import org.n52.sos.binding.rest.Constants;
import org.n52.sos.binding.rest.decode.ResourceDecoder;
import org.n52.sos.binding.rest.requests.BadRequestException;
import org.n52.sos.binding.rest.requests.RestRequest;
import org.n52.sos.binding.rest.resources.OptionsRestRequest;
import org.n52.sos.ext.deleteobservation.DeleteObservationRequest;
import org.n52.sos.util.XmlHelper;
import org.n52.svalbard.decode.exception.DecodingException;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class ObservationsDecoder extends ResourceDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationsDecoder.class);

    public ObservationsDecoder(Constants constants) {
        super(constants);
    }

    @Override
    protected RestRequest decodeGetRequest(HttpServletRequest httpRequest,
                                           String pathPayload) throws DecodingException, OwsExceptionReport {
        // 0 variables
        RestRequest result = null;

        // 1 identify type of request: by id OR search (OR atom feed)
        // 2.1 by id
        if (pathPayload != null && !pathPayload.isEmpty() && httpRequest.getQueryString() == null) {
            result = decodeObservationByIdRequest(pathPayload);
        } // 2.2 search
        else if (httpRequest.getQueryString() != null && pathPayload == null) {
            result = decodeObservationsSearchRequest(httpRequest);
        } /*
        // 2.3 feed
        else if (pathPayload == null && httpRequest.getQueryString() == null)
        {
            // pathpayload and querystring == null. if paging is implemented the querystring will not be empty
            result = decoderObservationsFeedRequest(httpRequest);
        }*/ else {
            String errorMsg = createBadGetRequestMessage(Constants.REST_RESOURCE_RELATION_OBSERVATIONS, false, true, true);
            BadRequestException bR = new BadRequestException(errorMsg);
            throw new NoApplicableCodeException().causedBy(bR).withMessage(errorMsg);
        }

        return result;
    }

    @Override
    protected RestRequest decodeDeleteRequest(HttpServletRequest httpRequest,
                                              String pathPayload) throws DecodingException {
        if (pathPayload != null && !pathPayload.isEmpty()) {
            return decodeObservationsDeleteRequest(pathPayload);
        } else {
            return null;
        }
    }

    private RestRequest decodeObservationsDeleteRequest(String pathPayload) {
        DeleteObservationRequest request = new DeleteObservationRequest();
        request.setService(bindingConstants.getSosService());
        request.setVersion(bindingConstants.getSosVersion());
        request.setObservationIdentifier(pathPayload);
        return new ObservationsDeleteRequest(request);
    }

    @Override
    protected RestRequest decodePostRequest(HttpServletRequest httpRequest,
                                            String pathPayload) throws DecodingException, OwsExceptionReport {
        if (isContentOfPostRequestValid(httpRequest) && pathPayload == null) {
            // 0 read xml encoded post content
            XmlObject requestDoc = XmlHelper.parseXmlRequest(httpRequest);
            if (requestDoc instanceof ObservationDocument) {
                ObservationDocument xb_ObservationRestDoc = (ObservationDocument) requestDoc;
                ObservationType xb_ObservationRest = xb_ObservationRestDoc.getObservation();
                OMObservationType xb_OMObservation = xb_ObservationRest.getOMObservation();

                // 1 check for gml:identifier
                if (!xb_OMObservation.isSetIdentifier()) {
                    // 1.1 if not available set to newly generated UUID
                    CodeWithAuthorityType xb_gmlIdentifier = CodeWithAuthorityType.Factory.newInstance();
                    xb_gmlIdentifier.setCodeSpace(UUID.class.getName());
                    xb_gmlIdentifier.setStringValue(UUID.randomUUID().toString());
                    xb_OMObservation.setIdentifier(xb_gmlIdentifier);
                }

                // 2 get offering link
                LinkType[] xb_links = xb_ObservationRest.getLinkArray();
                List<String> offeringIds = new ArrayList<>(xb_links.length);
                for (LinkType xb_Link : xb_links) {
                    if (isOfferingLink(xb_Link)) {
                        String href = xb_Link.getHref();
                        int lastSlashIndex = href.lastIndexOf('/');
                        String offeringId = href.substring(lastSlashIndex + 1);
                        offeringIds.add(offeringId);
                    }
                }

                // 2.1 clean links to resources like procedure and feature
                if (isProcedureReferenced(xb_OMObservation) && isProcedureReferencePoitingToMe(xb_OMObservation)) {
                    resetProcedureReference(xb_OMObservation);
                }
                if (isFeatureReferenced(xb_OMObservation) && isFeatureReferencePointingToMe(xb_OMObservation)) {
                    resetFeatureOfInterstReference(xb_OMObservation);
                }

                // 3 build insert observation request
                InsertObservationRequest insertObservationRequest = new InsertObservationRequest();
                insertObservationRequest.setService(bindingConstants.getSosService());
                insertObservationRequest.setVersion(bindingConstants.getSosVersion());
                insertObservationRequest.setOfferings(offeringIds);
                OmObservation sosObs = createSosObservationFromOMObservation(xb_OMObservation);
                insertObservationRequest.addObservation(sosObs);

                return new ObservationsPostRequest(insertObservationRequest, xb_OMObservation);
            }
        }
        String errorMsg = String.format(bindingConstants.getErrorMessageHttpMethodNotAllowedForResource(),
                                        "POST", Constants.REST_RESOURCE_RELATION_OBSERVATIONS);
        LOGGER.error(errorMsg);
        throw new OperationNotSupportedException("HTTP POST").withMessage(errorMsg);
    }

    private void resetProcedureReference(OMObservationType xb_OMObservation) {
        xb_OMObservation.getProcedure().setHref(getResourceIdFromRestfulHref(xb_OMObservation.getProcedure().getHref()));
    }

    private boolean isProcedureReferencePoitingToMe(OMObservationType xb_OMObservation) {
        return xb_OMObservation.getProcedure().getHref().startsWith(bindingConstants.getServiceUrl());
    }

    private boolean isProcedureReferenced(OMObservationType xb_OMObservation) {
        return xb_OMObservation != null && xb_OMObservation.getProcedure() != null && xb_OMObservation.getProcedure()
               .isSetHref();
    }

    private void resetFeatureOfInterstReference(OMObservationType xb_OMObservation) {
        xb_OMObservation.getFeatureOfInterest().setHref(getResourceIdFromRestfulHref(xb_OMObservation
                .getFeatureOfInterest().getHref()));
    }

    private boolean isFeatureReferencePointingToMe(OMObservationType xb_OMObservation) {
        return xb_OMObservation.getFeatureOfInterest().getHref().startsWith(bindingConstants.getServiceUrl());
    }

    private boolean isFeatureReferenced(OMObservationType xb_OMObservation) {
        return xb_OMObservation != null && xb_OMObservation.getFeatureOfInterest() != null && xb_OMObservation
               .getFeatureOfInterest().isSetHref();
    }

    @Override
    protected RestRequest decodePutRequest(HttpServletRequest httpRequest,
                                           String pathPayload) throws DecodingException, OwsExceptionReport {
        throw new OperationNotSupportedException(String.format("HTTP-PUT + '%s'", Constants.REST_RESOURCE_RELATION_OBSERVATIONS));
    }

    private ObservationsSearchRequest decodeObservationsSearchRequest(HttpServletRequest httpRequest) throws
            DecodingException {
        // 2.2.1 get kvp encoded parameters from querystring
        Map<String, String> parameterMap = getKvPEncodedParameters(httpRequest);

        // 2.2.2 build requests
        GetObservationRequest getObservationRequest = buildGetObservationSearchRequest(parameterMap);

        String queryString = httpRequest.getQueryString();

        return new ObservationsSearchRequest(getObservationRequest, queryString);
    }

    private ObservationsGetRequest decodeObservationByIdRequest(String pathPayload) {
        // build get observation by id request
        GetObservationByIdRequest getObservationRequest = buildGetObservationByIdRequest(pathPayload);
        // FIXME remove unused GetCapabilitiesRequest
        // build get capabilities request reduced to contents section
        GetCapabilitiesRequest getCapabilitesRequestOnlyContents = createGetCapabilitiesRequestWithContentSectionOnly();

        return new ObservationsGetRequest(getObservationRequest, getCapabilitesRequestOnlyContents);
    }

    private GetObservationRequest buildGetObservationSearchRequest(Map<String, String> parameterMap) throws
            DecodingException {
        GetObservationRequest request = new GetObservationRequest();
        request.setVersion(bindingConstants.getSosVersion());
        request.setService(bindingConstants.getSosService());
        request.setExtensions(createSubsettingExtension(true));

        boolean parameterMapValid = false; // if at least one parameter is valid

        // TODO add checking of parameters
        for (String parameter : parameterMap.keySet()) {

            String value = parameterMap.get(parameter);
            if (parameter.equalsIgnoreCase(Constants.REST_HTTP_GET_PARAMETERNAME_FEATURE) &&
                value != null && value.length() > 0) {
                request.setFeatureIdentifiers(splitKvpParameterValueToList(value));
                parameterMapValid = true;
            } else if (parameter.equalsIgnoreCase(Constants.REST_HTTP_GET_PARAMETERNAME_OBSERVED_PROPERTIES) &&
                       value != null && value.length() > 0) {
                request.setObservedProperties(splitKvpParameterValueToList(value));
                parameterMapValid = true;
            } else if (parameter.equalsIgnoreCase(Constants.REST_HTTP_GET_PARAMETERNAME_OFFERING) &&
                       value != null && value.length() > 0) {
                request.setOfferings(splitKvpParameterValueToList(value));
                parameterMapValid = true;
            } else if (parameter.equalsIgnoreCase(Constants.REST_HTTP_GET_PARAMETERNAME_PROCEDURES) &&
                       value != null && value.length() > 0) {
                request.setProcedures(splitKvpParameterValueToList(value));
                parameterMapValid = true;
            } else if (parameter.equalsIgnoreCase(Constants.REST_HTTP_GET_PARAMETERNAME_SPATIAL_FILTER) &&
                       value != null && value.length() > 0) {
                request.setSpatialFilter(parseSpatialFilter(splitKvpParameterValueToList(value), parameter));
                parameterMapValid = true;
            } else if (parameter.equalsIgnoreCase(Constants.REST_HTTP_GET_PARAMETERNAME_TEMPORAL_FILTER) &&
                       value != null && value.length() > 0) {
                request.setTemporalFilters(parseTemporalFilter(splitKvpParameterValueToList(value)));
                parameterMapValid = true;
            } else if (parameter.equalsIgnoreCase(Constants.REST_HTTP_GET_PARAMETERNAME_NAMESPACES) &&
                       value != null && value.length() > 0) {
                request.setNamespaces(parseNamespaces(value));
                parameterMapValid = true;
            } else {
                throw new DecodingException(parameter, "The value '%s' of the parameter '%s' is invalid", value);
            }
        }
        if (!parameterMapValid) {
            throw new DecodingException(bindingConstants.getErrorMessageBadGetRequestNoValidKvpParameter());
        }
        return request;
    }

    private GetObservationByIdRequest buildGetObservationByIdRequest(String observationId) {
        GetObservationByIdRequest request = new GetObservationByIdRequest();
        ArrayList<String> observationIds = new ArrayList<>(1);
        observationIds.add(observationId);
        request.setObservationIdentifier(observationIds);
        request.setService(bindingConstants.getSosService());
        request.setVersion(bindingConstants.getSosVersion());
        Extensions extensions = createSubsettingExtension(true);
        request.setExtensions(extensions);
        return request;
    }

    private Extensions createSubsettingExtension(boolean enabled) {
        Boolean value = enabled ? Boolean.TRUE : Boolean.FALSE;

        Extensions extensions = new Extensions();
        SwesExtension<Boolean> antiSubsettingExtension = new SwesExtension<>();
        antiSubsettingExtension.setDefinition(Sos2Constants.Extensions.MergeObservationsIntoDataArray.name());
        antiSubsettingExtension.setValue(value);
        extensions.addExtension(antiSubsettingExtension);

        return extensions;
    }

    private OmObservation createSosObservationFromOMObservation(OMObservationType omObservation) throws
            DecodingException {
        Object decodedObject = decodeXmlObject(omObservation);
        if (decodedObject != null && decodedObject instanceof OmObservation) {
            OmObservation sosObservation = (OmObservation) decodedObject;
            return sosObservation;
        } else {
            throw new DecodingException(Sos2Constants.InsertObservationParams.observationType,
                    "The value '%s' of the parameter '%s' is invalid",
                    decodedObject != null ? decodedObject.getClass().getName(): "null");
        }
    }

    private boolean isOfferingLink(LinkType xb_Link) {
        return !xb_Link.isNil() && xb_Link.getRel().equalsIgnoreCase(getRelationIdentifierWithNamespace(Constants.REST_RESOURCE_RELATION_OFFERING_GET));
    }

    @Override
    protected RestRequest decodeOptionsRequest(HttpServletRequest httpRequest,
                                               String pathPayload) {
        boolean isGlobal = false;
        boolean isCollection = false;
        if (httpRequest != null && httpRequest.getQueryString() != null && pathPayload == null) {
            isGlobal = true;
            isCollection = true;
        } else if (httpRequest != null && httpRequest.getQueryString() == null && pathPayload == null) {
            isGlobal = true;
            isCollection = false;
        } else if (httpRequest != null && httpRequest.getQueryString() == null && pathPayload != null) {
            isGlobal = false;
            isCollection = false;
        }
        return new OptionsRestRequest((Constants.REST_RESOURCE_RELATION_OBSERVATIONS), isGlobal, isCollection);
    }

}
