/*
 * Copyright (c) 2015 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.timeseries.bootstrap.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;

import com.ge.predix.solsvc.restclient.impl.RestClient;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesRestConfig;

/**
 * 
 * @author 212438846
 *
 */

@ContextConfiguration(locations = {
		"classpath*:META-INF/spring/ext-util-scan-context.xml",
		"classpath*:META-INF/spring/timeseries-bootstrap-scan-context.xml",
		"classpath*:META-INF/spring/predix-websocket-client-scan-context.xml",
		"classpath*:META-INF/spring/predix-rest-client-scan-context.xml",
		"classpath*:META-INF/spring/predix-rest-client-sb-properties-context.xml" })
@PropertySource("classpath:timeseries-config-test.properties")
public abstract class BaseFactoryIT {

	/**
	 * 
	 */
	@Autowired
	protected RestClient restClient;

	/**
	 * 
	 */
	@Autowired
	TimeseriesRestConfig timeseriesRestConfig;

}
