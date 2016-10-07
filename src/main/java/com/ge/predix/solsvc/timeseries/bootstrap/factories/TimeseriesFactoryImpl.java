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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.predix.entity.timeseries.aggregations.AggregationsList;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.entity.timeseries.datapoints.ingestionresponse.AcknowledgementMessage;
import com.ge.predix.entity.timeseries.datapoints.queryrequest.DatapointsQuery;
import com.ge.predix.entity.timeseries.datapoints.queryrequest.latest.DatapointsLatestQuery;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
import com.ge.predix.entity.timeseries.tags.TagsList;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.restclient.impl.RestClient;
import com.ge.predix.solsvc.timeseries.bootstrap.api.TimeSeriesAPIV1;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesRestConfig;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesWSConfig;
import com.ge.predix.solsvc.websocket.client.WebSocketClient;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;

/**
 * The main entry point for using the Timeseries Bootstrap. Each method
 * represents a major Time Series API.
 * 
 * @author 212438846
 *
 */
@Component
public class TimeseriesFactoryImpl
        implements TimeseriesFactory
{
    private static Logger        log = LoggerFactory.getLogger(TimeseriesFactory.class);

    @Autowired
    private RestClient           restClient;

    @Autowired
    private TimeseriesWSConfig   tsIngestionWSConfig;

    @Autowired
    private WebSocketClient      wsClient;

    @Autowired
    private JsonMapper           jsonMapper;

    @Autowired
    private TimeseriesRestConfig timeseriesRestConfig;

    /**
     * @param messageListener
     *            - method accepts custom message listener
     * @since Predix Time Series API v1.0 Method to create connection to TS
     *        Websocket to the configured TS Server List<Header> headers
     */
    @Override
    public void createConnectionToTimeseriesWebsocket(WebSocketAdapter messageListener)
    {
        try
        {
            WebSocketAdapter listener = messageListener;
            if ( listener == null )
            {
                listener = registerDefaultMessageListener();
            }
            List<Header> nullHeaders = null;
            this.wsClient.init(this.tsIngestionWSConfig, nullHeaders, listener);
        }
        catch (Exception e)
        {
            log.error("Connection to websocket failed. " + e); //$NON-NLS-1$
            throw new RuntimeException("Connection to websocket failed. ", e); //$NON-NLS-1$
        }

    }

    /**
     * @since Predix Time Series API v1.0 Method to create connection to TS
     *        Websocket to the configured TS Server List<Header> headers
     */
    @Override
    public void createConnectionToTimeseriesWebsocket()
    {
        try
        {
            WebSocketAdapter listener = null;
            createConnectionToTimeseriesWebsocket(listener);
        }
        catch (Exception e)
        {
            log.error("Connection to websocket failed. " + e); //$NON-NLS-1$
            throw new RuntimeException("Connection to websocket failed. ", e); //$NON-NLS-1$
        }

    }
    
    private WebSocketAdapter registerDefaultMessageListener()
    {
        WebSocketAdapter mListener = new WebSocketAdapter()
        {
            private JsonMapper jMapper = new JsonMapper();
            private Logger     logger  = LoggerFactory.getLogger(TimeseriesFactory.class);

            @Override
            public void onTextMessage(WebSocket wsocket, String message)
            {
                handleErrorMessage(message);
            }

            @Override
            public void onBinaryMessage(WebSocket wsocket, byte[] binary)
            {
                String message = new String(binary, StandardCharsets.UTF_8);
                handleErrorMessage(message);
            }

            /**
             * @param message
             */
            private void handleErrorMessage(String message)
            {
                AcknowledgementMessage am = this.jMapper.fromJson(message, AcknowledgementMessage.class);
                if ( am.getStatusCode() > 299 && am.getStatusCode() < 400 )
                {
                    this.logger.info("STATUS CODE...." + am.getStatusCode() + "--ID:" + am.getMessageId()); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if ( am.getStatusCode() > 399 )
                {
                    this.logger.error("ERROR STATUS CODE...." + am.getStatusCode() + "--ID:" + am.getMessageId()); //$NON-NLS-1$ //$NON-NLS-2$
                }
                else
                {
                    this.logger.debug("SUCCESS...." + am.getStatusCode() + "--ID:" + am.getMessageId()); //$NON-NLS-1$ //$NON-NLS-2$

                }
            }
        };
        return mListener;
    }

    /**
     * @since Predix Time Series API v1.0 Method to post data through Websocket
     *        to the configured TS Server
     * @param datapointsIngestion
     *            -
     * @see DatapointsIngestion
     * 
     */
    @Override
    public void postDataToTimeseriesWebsocket(DatapointsIngestion datapointsIngestion)
    {
        String request = this.jsonMapper.toJson(datapointsIngestion);
        log.debug(request);

        try
        {
            this.wsClient.postTextWSData(request);
        }
        catch (IOException | WebSocketException e)
        {
            throw new RuntimeException("Failed to post data to websocket. " + e); //$NON-NLS-1$
        }
    }

    /**
     * @since Predix Time Series API v1.0
     * @param uri
     *            -
     * @see TimeSeriesAPIV1
     * @param datapoints
     *            -
     * @see DatapointsQuery
     * @param headers
     *            {@href https://github.com/PredixDev/predix-rmd-ref-app}
     * @return @see DatapointsResponse
     */

    @Override
    public DatapointsResponse queryForDatapoints(String baseUrl, DatapointsQuery datapoints, List<Header> headers)
    {
        DatapointsResponse response = null;

        if ( datapoints == null )
        {
            log.debug("datapoints request obj is null"); //$NON-NLS-1$
            return response;
        }
        CloseableHttpResponse httpResponse = null;
        try
        {
            String request = this.jsonMapper.toJson(datapoints);
            log.debug(request);
            httpResponse = this.restClient.post(baseUrl + TimeSeriesAPIV1.datapointsURI,
                    this.jsonMapper.toJson(datapoints), headers,
                    this.timeseriesRestConfig.getTimeseriesConnectionTimeout(),
                    this.timeseriesRestConfig.getTimeseriesSocketTimeout());
            handleIfErrorResponse(httpResponse);
            String responseEntity = processHttpResponseEntity(httpResponse.getEntity());
            if ( responseEntity == null ) return null;
            response = this.jsonMapper.fromJson(responseEntity, DatapointsResponse.class);
            return response;
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Error occured calling=" + baseUrl //$NON-NLS-1$
                            + " for query=" + datapoints + " with headers=" + headers, //$NON-NLS-1$ //$NON-NLS-2$
                    e);
        }
        finally
        {
            if ( httpResponse != null ) try
            {
                httpResponse.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * @since Predix Time Series API v1.0
     * @param uri
     *            -
     * @see TimeSeriesAPIV1
     * @param datapoints
     *            -
     * @see DatapointsLatestQuery
     * @param headers
     *            {@href https://github.com/PredixDev/predix-rmd-ref-app}
     * @return @see DatapointsResponse
     */

    @Override
    public DatapointsResponse queryForLatestDatapoint(String baseUrl, DatapointsLatestQuery latestDatapoints,
            List<Header> headers)
    {
        DatapointsResponse response = null;

        if ( latestDatapoints == null )
        {
            log.debug("datapoints obj is null"); //$NON-NLS-1$
            return response;
        }
        CloseableHttpResponse httpResponse = null;
        try
        {
            String request = this.jsonMapper.toJson(latestDatapoints);
            log.debug(request);
            httpResponse = this.restClient.post(baseUrl + TimeSeriesAPIV1.latestdatapointsURI,
                    this.jsonMapper.toJson(latestDatapoints), headers,
                    this.timeseriesRestConfig.getTimeseriesConnectionTimeout(),
                    this.timeseriesRestConfig.getTimeseriesSocketTimeout());
            handleIfErrorResponse(httpResponse);
            String responseEntity = processHttpResponseEntity(httpResponse.getEntity());
            log.debug("Response from TS service = " + responseEntity); //$NON-NLS-1$
            if ( responseEntity == null ) return null;
            response = this.jsonMapper.fromJson(responseEntity, DatapointsResponse.class);
            return response;
        }
        catch (IOException e)
        {
            throw new RuntimeException("error occurred calling baseUrl=" //$NON-NLS-1$
                    + baseUrl + " for query=" + latestDatapoints //$NON-NLS-1$
                    + " with headers=" + headers, e); //$NON-NLS-1$
        }
        finally
        {
            if ( httpResponse != null ) try
            {
                httpResponse.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * @since Predix Time Series API v1.0
     * @param baseUrl
     *            -
     * @see TimeSeriesAPIV1
     * @param headers
     *            {@href https://github.com/PredixDev/predix-rmd-ref-app}
     * @return @see TagsResponse
     */
    @Override
    public TagsList listTags(String baseUrl, List<Header> headers)
    {
        TagsList responseTagList = null;
        CloseableHttpResponse httpResponse = null;
        try
        {
            httpResponse = this.restClient.get(baseUrl + TimeSeriesAPIV1.tagsURI, headers,
                    this.timeseriesRestConfig.getTimeseriesConnectionTimeout(),
                    this.timeseriesRestConfig.getTimeseriesSocketTimeout());
            handleIfErrorResponse(httpResponse);
            String responseEntity = processHttpResponseEntity(httpResponse.getEntity());
            log.debug("Response from TS service = " + responseEntity); //$NON-NLS-1$
            if ( responseEntity == null ) return null;
            responseTagList = this.jsonMapper.fromJson(responseEntity, TagsList.class);
            return responseTagList;
        }
        catch (IOException e)
        {
            throw new RuntimeException("error occurred calling baseUrl=" //$NON-NLS-1$
                    + baseUrl + " for query=" + TimeSeriesAPIV1.tagsURI //$NON-NLS-1$
                    + " with headers=" + headers, e); //$NON-NLS-1$
        }
        finally
        {
            if ( httpResponse != null ) try
            {
                httpResponse.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public AggregationsList listAggregations(String baseUrl, List<Header> headers)
    {
        AggregationsList responseAggregationsList = null;
        CloseableHttpResponse httpResponse = null;
        try
        {
            httpResponse = this.restClient.get(baseUrl + TimeSeriesAPIV1.aggregationsURI, headers,
                    this.timeseriesRestConfig.getTimeseriesConnectionTimeout(),
                    this.timeseriesRestConfig.getTimeseriesSocketTimeout());
            handleIfErrorResponse(httpResponse);
            String responseEntity = processHttpResponseEntity(httpResponse.getEntity());
            log.debug("Response from TS service = " + responseEntity); //$NON-NLS-1$
            if ( responseEntity == null ) return null;
            responseAggregationsList = this.jsonMapper.fromJson(responseEntity, AggregationsList.class);
            return responseAggregationsList;
        }
        catch (IOException e)
        {
            throw new RuntimeException("error occurred calling baseUrl=" //$NON-NLS-1$
                    + baseUrl + " for query=" + TimeSeriesAPIV1.aggregationsURI //$NON-NLS-1$
                    + " with headers=" + headers, e); //$NON-NLS-1$
        }
        finally
        {
            if ( httpResponse != null ) try
            {
                httpResponse.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private String processHttpResponseEntity(org.apache.http.HttpEntity entity)
            throws IOException
    {
        if ( entity == null ) return null;
        if ( entity instanceof GzipDecompressingEntity )
        {
            return IOUtils.toString(((GzipDecompressingEntity) entity).getContent(), "UTF-8"); //$NON-NLS-1$
        }
        return EntityUtils.toString(entity);
    }

    @SuppressWarnings("nls")
	private static void handleIfErrorResponse(CloseableHttpResponse httpResponse)
    {
        try {
			if ( httpResponse.getStatusLine() == null )
			{
			    log.info("No Status response was received. Locale:" //$NON-NLS-1$
			            + httpResponse.getLocale());
			    throw new RuntimeException("No Status response was received. Locale:" //$NON-NLS-1$
			            + httpResponse.getLocale());
			}
			if ( httpResponse.getStatusLine().getStatusCode() >= 300 )
			{
			    log.info("Query was unsuccessful. Status Code:" //$NON-NLS-1$
			            + httpResponse.getStatusLine().getStatusCode());
			    String body = (httpResponse == null) ? "Response body was empty" //$NON-NLS-1$
			            : EntityUtils.toString(httpResponse.getEntity());
				log.info(body );
			    throw new RuntimeException("Query was unsuccessful. Status Code:" //$NON-NLS-1$
			            + httpResponse.getStatusLine().getStatusCode() + " Response Body=" + body);
			}
		}  catch (IOException e) {
			throw new RuntimeException("Unable to get response", e);
		}
    }

}
