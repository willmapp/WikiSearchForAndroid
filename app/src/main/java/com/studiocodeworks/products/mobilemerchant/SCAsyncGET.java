/***************************************************************************************
 * 
 * Studio Codeworks, Inc.
 * 
 * Product: ASKY
 * 
 * Module Name: SCAsyncGET.java
 * 
 * Author: James A Cooke
 * 
 * Creation Date: July 1, 2013
 * 
 * Description: This class is used to perform an asynchronous get request to the Asky
 * 				server.  Objects are returned and posted via concurrent linked queue.
 * 
 * Usage of this file is restricted to products designed and built by Studio Codeworks, 
 * Inc. You may not use this file without the expressed written permission obtained from
 * Studio Codeworks, Inc.
 * 
 * Copyright (C) MMXIII
 * 
 * All Rights Reserved
 * 
 *******************************************************************************************/

package com.studiocodeworks.products.mobilemerchant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Custom {@link AsyncTask} used to asynchronously get {@link json} from a web server
 * and place them in a {@link ConcurrentLinkedQueue} for an adapter.
 * @author James
 * @version 1.3 Sinful Siren
 */
public class SCAsyncGET extends AsyncTask<String, Integer, Integer>
{
    private boolean                             m_expectingSingle   = false;
    private HttpContext                         m_httpContext       = null;
    private String                              m_target            = null;
    private JSONArray                           m_resultArray       = null;
    private JSONObject                          m_resultObject      = null;
	private IDataReceivedHandler			    m_dataHandler	    = null;

	public SCAsyncGET()
	{
	}

	/**
	 * Overridden to get JSON data from as many urls that are passed in.
	 */
	@Override
	protected Integer doInBackground(String... params) 
	{
		{
			 HttpClient     httpclient  = new DefaultHttpClient();
		     HttpGet        httpget     = null; // new HttpGet(params[0]);

		     // httpget.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		     HttpResponse   response;

		     try 
		     {
                 m_target               = params[0];

                 // before we get started make sure this parameter list is properly
                 // formatted
                 // subtract the url target, it should always be the first parameter
                 // which means that the number of parameters should be even
                 // there should be a key and a value
                 if((params.length - 1) % 2 == 0 && params.length > 1)
                 {
                     m_target += "?";
                     // set up the get parameters, advance two positions to skip
                     // to the next key/value pair in the list
                     for(int i = 1; i < params.length; i+=2)
                     {
                         String key     = params[i];
                         String value   = params[i+1];

                         m_target += key + "=" + URLEncoder.encode(value, "UTF-8");

                         if((i+2) < params.length)
                         {
                             m_target += "&";
                         }
                     }
                 }

                 Log.d("SCAsyncGET", "Target: " + m_target);
                 // plug in the url that we will hit
                 httpget                = new HttpGet(m_target);
                 // execute the request
		    	 response               = httpclient.execute(httpget, m_httpContext);
                 // get the response from this request
		         HttpEntity entity      = response.getEntity();

		         if (entity != null) 
		         {
                     // we need to read the response data using a stream
                     // what's a stream
                     // just a series of bytes, that's all, don't over think it
		        	 InputStream    instream    = entity.getContent();
                     // use a buffering construct to smoothen out the read process
		             BufferedReader reader      = new BufferedReader(new InputStreamReader(instream));
                     // use the string builder to compile the response over time
		             StringBuilder  sb          = new StringBuilder();
		             String         line        = null;

                     // read each line of input until there's nothing left
		             while ((line = reader.readLine()) != null) 
		             {
		                 sb.append(line + "\n");
		             }

                     // close the stream
		             instream.close();
		             String         result      = sb.toString();
                     // these lines are commented out because some servers do a little
                     // more decorating to their json
                     // google just sends straigh text
                     // result                     = result.replace("\"{", "{");
                     // result                     = result.replace("}\"", "}");
                     // result                     = result.replace("\\", "");

                     Log.d("SCAsyncGET", "Returned Result: " + result);
                     if(m_dataHandler != null)
                     {
                         // create the returning object or array to the interested party
                         if(m_expectingSingle)
                         {
                             m_resultObject = new JSONObject(result);
                         }
                         else
                         {
                             m_resultArray = new JSONArray(result);
                         }
                     }
		             
		             Log.i("SCAsyncGET", "doInBackground - Returned JSON From GET: " + result);
		         }
		     } 
		     catch (ClientProtocolException e) {} 
		     catch (IOException e) {} 
		     catch (JSONException e) 
		     {
		    	 Log.e("SCAsyncGET", "doInBackground - An Exception Occurred While Parsing JSON: " + e);
		     }
		}
		return null;
	}

    @Override
    protected void onPostExecute(Integer integer)
    {
        super.onPostExecute(integer);

        // if the calling party set a data handler alert them we are done
        if(m_dataHandler != null)
        {
            m_dataHandler.dataReceived(this);
        }
    }

    public JSONObject getResultObject()
    {
        return m_resultObject;
    }

    public JSONArray getResultArray()
    {
        return  m_resultArray;
    }

    public String getTarget()
    {
        return m_target;
    }

    public void setDataHandler(IDataReceivedHandler handler)
    {
        m_dataHandler = handler;
    }

    public void setExpectingSingleResult(boolean single)
    {
        m_expectingSingle = single;
    }

    public void setHttpContext(HttpContext context)
    {
        m_httpContext = context;
    }
}
