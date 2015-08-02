/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bpmscript.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.bpmscript.BpmScriptException;
import org.bpmscript.util.StreamService;

/**
 * 
 */
public class HttpClientRestService implements IRestService  {

    /**
     * @throws BpmScriptException 
     * @see org.bpmscript.http.IRestService#get(java.lang.String)
     */
    public String get(String url) throws BpmScriptException {
        HttpClient client = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        try {
            int status = client.executeMethod(getMethod);
            if(status == 200) {
                String result = StreamService.DEFAULT_INSTANCE.readFully(getMethod.getResponseBodyAsStream());
                return result;
            } else {
                throw new BpmScriptException("Error status of " + status);
            }
        } catch (HttpException e) {
            throw new BpmScriptException(e);
        } catch (IOException e) {
            throw new BpmScriptException(e);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * @throws BpmScriptException 
     * @see org.bpmscript.http.IRestService#post(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("deprecation")
    public String post(String url, String content) throws BpmScriptException {
        HttpClient client = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        postMethod.setRequestBody(content);
        try {
            int status = client.executeMethod(postMethod);
            if(status == 200) {
                String result = StreamService.DEFAULT_INSTANCE.readFully(postMethod.getResponseBodyAsStream());
                return result;
            } else {
                throw new BpmScriptException("Error status of " + status);
            }
        } catch (HttpException e) {
            throw new BpmScriptException(e);
        } catch (IOException e) {
            throw new BpmScriptException(e);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
