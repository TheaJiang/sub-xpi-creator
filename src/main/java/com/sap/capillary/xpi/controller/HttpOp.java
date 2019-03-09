package com.sap.capillary.xpi.controller;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpOp {
    public String basic_auth;

    public HttpOp() {
    }
    
    public HttpOp(String basic_auth) {
        this.basic_auth = basic_auth;
    }

    String postHttpRequest(String url, String body) throws Exception{
        return postHttpRequest(url, true, body);
    }

    String postHttpRequest(String url, boolean auth, String body) throws Exception{
        HttpPost httpPost = new HttpPost(url);
        if(auth)
            httpPost.setHeader("Authorization", basic_auth);
        httpPost.setHeader("Content-type", "application/json");
        StringEntity entity = new StringEntity(body, "UTF-8");
        entity.setContentType("application/json;charset=UTF-8");
        httpPost.setEntity(entity);
        return executeHttpRequest(httpPost);
    }

    String getHttpRequest(String url) throws Exception{
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", basic_auth);
        httpGet.setHeader("Content-type", "application/json");
        return executeHttpRequest(httpGet);
    }

    String executeHttpRequest(HttpUriRequest uriRequest) throws Exception{
        String response = null;
        
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
    
            @Override
            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException(
                            String.format("Unexpected response status: %d\nError details:\n%s",
                                    status, EntityUtils.toString(response.getEntity())));
                }
    
            }
        };
        try(CloseableHttpClient httpclient = HttpClients.createDefault()) {
            response = httpclient.execute(uriRequest, responseHandler);
        } catch(Exception ex) {
            throw ex;
        }
        return response;
    }
}