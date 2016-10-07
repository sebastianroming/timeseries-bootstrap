/*
 * Copyright (c) 2015 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.timeseries.bootstrap.config;

import org.springframework.beans.factory.annotation.Value;

/**
 * Holds properties for making calls to the Time Series API.  These properties
 * are shared in both Rest and WebSocket calls.  See Child Class.
 * 
 * @author predix -
 */
public class TimeSeriesBaseConfig
{

    @SuppressWarnings("nls")
    private String   predixZoneIdHeaderName = "Predix-Zone-Id";

    /**
     * The identifier that defines Data Tenancy when calling the Time Series API
     * 
     */
    @Value("${predix.timeseries.zoneid}")
    protected String zoneId;

    /**
     * The HTTP Header name to send when calling the Time Series API with the ZoneId
     * 
     * @return -
     */
    public String getPredixZoneIdHeaderName()
    {
        return this.predixZoneIdHeaderName;
    }

    /**
     * The HTTP Header name to send when calling the Time Series API with the ZoneId
     * 
     * @param predixZoneIdHeaderName -
     */
    public void setPredixZoneIdHeaderName(String predixZoneIdHeaderName)
    {
        this.predixZoneIdHeaderName = predixZoneIdHeaderName;
    }

    /**
     * The identifier that defines Data Tenancy when calling the Time Series API
     *
     * @return -
     */
    public String getZoneId()
    {
        return this.zoneId;
    }

    /**
     * The identifier that defines Data Tenancy when calling the Time Series API
     * 
     * @param predixZoneId -
     */
    public void setZoneId(String predixZoneId)
    {
        this.zoneId = predixZoneId;
    }

}
