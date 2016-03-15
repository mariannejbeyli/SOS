/**
 * Copyright (C) 2012-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.sos.inspire.omso;


import org.n52.sos.w3c.SchemaLocation;

public interface InspireOMSOConstants {
    
    String NS_OMSO_30 = "http://inspire.ec.europa.eu/schemas/omso/3.0";
    
    String NS_OMSO_PREFIX = "omso";
    
    String SCHEMA_LOCATION_URL_OMSO = "http://inspire.ec.europa.eu/schemas/omso/3.0/SpecialisedObservations.xsd";
    
    SchemaLocation OMSO_SCHEMA_LOCATION = new SchemaLocation(NS_OMSO_30, SCHEMA_LOCATION_URL_OMSO);
    
    // observation types
    String OBS_TYPE_POINT_OBSERVATION = "http://inspire.ec.europa.eu/featureconcept/PointObservation";
    
    String OBS_TYPE_POINT_TIME_SERIES_OBSERVATION = "http://inspire.ec.europa.eu/featureconcept/PointTimeSeriesObservation";
    
    String OBS_TYPE_MULTI_POINT_OBSERVATION = "http://inspire.ec.europa.eu/featureconcept/MultiPointObservation";
    
    String OBS_TYPE_PROFILE_OBSERVATION = "http://inspire.ec.europa.eu/featureconcept/ProfileObservation";
    
    String OBS_TYPE_TRAJECTORY_OBSERVATION = "http://inspire.ec.europa.eu/featureconcept/TrajectoryObservation";

}
