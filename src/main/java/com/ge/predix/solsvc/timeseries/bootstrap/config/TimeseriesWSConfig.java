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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.websocket.config.IWebSocketConfig;

/**
 * 
 * @author 212421693
 */
@Component
public class TimeseriesWSConfig
        implements EnvironmentAware, IWebSocketConfig
{
    private static Logger      log                    = LoggerFactory.getLogger(TimeseriesWSConfig.class);

    /**
     * vcap variable name
     */
    public static final String TIME_SERIES_VCAPS_NAME = "predix_timeseries_name";                         //$NON-NLS-1$

	@Value("${predix.oauth.proxyHost:}")
	private String wsProxyHost;
	
	@Value("${predix.oauth.proxyPort:}")
	private String wsProxyPort;
	
	@Value("${predix.timeseries.ingestUri}")
	private String wsUri;
	
	@Value("${predix.timeseries.zoneid}")
	private String zoneId;
	
	@Value("${predix.websocket.pool.maxIdle:}")
	private int wsMaxIdle;
	
	@Value("${predix.websocket.pool.maxActive:}")
	private int wsMaxActive;
	
	@Value("${predix.websocket.pool.maxWait}")
	private int wsMaxWait;
	
	@Value("${predix.websocket.pool.maxInstances}")
	private int wsMaxInstances;

    
	/*
     * (non-Javadoc)
     * @see org.springframework.context.EnvironmentAware#setEnvironment(org.
     * springframework.core.env.Environment)
     */
    @Override
    public void setEnvironment(Environment env)
    {
        String tsName = env.getProperty(TIME_SERIES_VCAPS_NAME); // this is set
                                                                 // on the
                                                                 // manifest
                                                                 // of the
                                                                 // application
        if ( StringUtils.isEmpty(tsName) )
        {
            tsName = "predixTimeseries"; //$NON-NLS-1$
        }
        //set ingest.uri
        if ( StringUtils.isNotBlank(env.getProperty("vcap.services." + tsName + ".credentials.ingest.uri")) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
        	this.wsUri = env.getProperty("vcap.services." + tsName + ".credentials.ingest.uri"); //$NON-NLS-1$ //$NON-NLS-2$
        }      

      //set zoneIdHeaderValue
        if ( StringUtils.isNotBlank(env.getProperty("vcap.services." + tsName + ".credentials.ingest.zone-http-header-value")) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
            this.zoneId = env.getProperty("vcap.services." + tsName + ".credentials.ingest.zone-http-header-value"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        log.info("Setting from Env-----" + toString()); //$NON-NLS-1$
    }

  


	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsMaxIdle()
	 */
	@Override
	public int getWsMaxIdle() {
		return this.wsMaxIdle;
	}



	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsMaxActive()
	 */
	@Override
	public int getWsMaxActive() {
		return this.wsMaxActive;
	}



	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsMaxWait()
	 */
	@Override
	public int getWsMaxWait() {
		return this.wsMaxWait;
	}



	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsMaxInstances()
	 */
	//@Override
	/*public int getWsMaxInstances() {
		return this.wsMaxInstances;
	}*/



	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsProxyHost()
	 */
	@Override
	public String getWsProxyHost() {
		return this.wsProxyHost;
	}



	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsProxyPort()
	 */
	@Override
	public String getWsProxyPort() {
		return this.wsProxyPort;
	}



	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsUri()
	 */
	@Override
	public String getWsUri() {
		return this.wsUri;
	}



	/* (non-Javadoc)
	 * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getZoneId()
	 */
	@Override
	public String getZoneId() {
		return this.zoneId;
	}


	

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Ingestion URL =" + this.wsUri + ","); //$NON-NLS-1$//$NON-NLS-2$
        return stringBuffer.toString();
    }

}
