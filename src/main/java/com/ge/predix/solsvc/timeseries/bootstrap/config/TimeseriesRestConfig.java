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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Properties needed to make rest calls to the Time Series instance
 * 
 * @author 212421693
 */
@Component
public class TimeseriesRestConfig extends TimeSeriesBaseConfig implements
		EnvironmentAware {

	/**
	 * The name of the VCAP property holding the 
	 */
	public static final String TIME_SERIES_VCAPS_NAME = "predix_timeseries_name"; //$NON-NLS-1$

	@Value("${predix.timeseries.baseUrl}")
	private String baseUrl;

	@Value("${predix.timeseries.override.oauthClientId}")
	private String oauthClientId;

	@Value("${predix.timeseries.override.oauthRestHost}")
	private String oauthRestHost;

	@Value("${predix.timeseries.override.oauthOverride}")
	private Boolean oauthOverride;

	@Value("${predix.timeseries.connectionTimeout:10000}")	
	private int timeseriesConnectionTimeout;
	
	@Value("${predix.timeseries.socketTimeout:10000}")	
	private int timeseriesSocketTimeout;


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.EnvironmentAware#setEnvironment(org.
	 * springframework.core.env.Environment)
	 */
	@SuppressWarnings("nls")
    @Override
	public void setEnvironment(Environment env) {
		String vcapPropertyName = null;
		String tsName = env.getProperty(TIME_SERIES_VCAPS_NAME); // this is set on the manifest of the application

		vcapPropertyName = null;
		if (StringUtils.isEmpty(tsName)) {
			vcapPropertyName = "vcap.services.predixTimeseries.credentials.query.uri"; // this is set from vcap of the application //$NON-NLS-1$
		} else {
			vcapPropertyName = "vcap.services." + tsName + ".credentials.query.uri"; //$NON-NLS-1$//$NON-NLS-2$
		}
		if (!StringUtils.isEmpty(env.getProperty(vcapPropertyName))) {
			try {
				this.baseUrl = getHost(env.getProperty(vcapPropertyName));
			} catch (MalformedURLException e) {
				throw new RuntimeException(
						"Unable to get hostname for timeseries service.", e);
			}
		}
		if (StringUtils.isEmpty(tsName)) {
			vcapPropertyName = "vcap.services.predixTimeseries.credentials.query.zone-http-header-value"; // this is set from vcaps of the application //$NON-NLS-1$
		} else {
			vcapPropertyName = "vcap.services." + tsName + ".credentials.query.zone-http-header-value"; //$NON-NLS-1$//$NON-NLS-2$
		}
		if (!StringUtils.isEmpty(env.getProperty(vcapPropertyName))) {
			this.zoneId = env.getProperty(vcapPropertyName);
		}
		if (StringUtils.isEmpty(tsName)) {
			vcapPropertyName = "vcap.services.predixTimeseries.credentials.query.zone-http-header-name"; // this is set from vcaps of the application //$NON-NLS-1$
		} else {
			vcapPropertyName = "vcap.services." + tsName + ".credentials.query.zone-http-header-name"; //$NON-NLS-1$//$NON-NLS-2$
		}
		if (!StringUtils.isEmpty(env.getProperty(vcapPropertyName))) {
			setPredixZoneIdHeaderName(env.getProperty(vcapPropertyName));
		}

	}

	/**
	 * @param url - the hostname of the Time Series api
	 * @return -
	 * @throws MalformedURLException -
	 */
	@SuppressWarnings("nls")
    public String getHost(String url) throws MalformedURLException {
		URL aURL = new URL(url);
		return aURL.getProtocol() + "://" + aURL.getAuthority();
	}

	/**
	 * @return -
	 */
	public String getBaseUrl() {
		return this.baseUrl;
	}

	/**
	 * @return -
	 */
	public String getOauthClientId() {
		return this.oauthClientId;
	}

	/**
	 * @return -
	 */
	public String getOauthRestHost() {
		return this.oauthRestHost;
	}

	/**
	 * @return -
	 */
	public boolean getOauthTimeseriesOverride() {
		return this.oauthOverride;
	}

	/**
	 * @return -
	 */
	public int getTimeseriesConnectionTimeout() {
		return this.timeseriesConnectionTimeout;
	}

	/**
	 * @return -
	 */
	public int getTimeseriesSocketTimeout() {
		return this.timeseriesSocketTimeout;
	}
}
