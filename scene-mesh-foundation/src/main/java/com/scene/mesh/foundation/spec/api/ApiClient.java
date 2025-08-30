package com.scene.mesh.foundation.spec.api;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.LaxRedirectStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class ApiClient {

    private final Map<String, String> serviceUrls;

    private RestTemplate restTemplate;

    public ApiClient(Map<String, String> serviceUrls) {
        this.serviceUrls = serviceUrls;
    }

    public void __init__(){
        RequestConfig config = RequestConfig.custom()
                .setRedirectsEnabled(false)
                .setCircularRedirectsAllowed(false)
                .build();

        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .setRedirectStrategy(new LaxRedirectStrategy()) // 宽松的重定向策略，支持所有HTTP方法
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
        this.restTemplate = new RestTemplate(factory);
    }

    public <T> T get(String service, String path, Class<T> responseType, Object... params) {
        String url = serviceUrls.get(service) + path;
        return restTemplate.getForObject(url, responseType, params);
    }

    public <T> T post(String service, String path, Object request, Class<T> responseType) {
        String url = serviceUrls.get(service) + path;
        return restTemplate.postForObject(url, request, responseType);
    }

    public <T> T put(String service, String path, Object request, Class<T> responseType) {
        String url = serviceUrls.get(service) + path;
        HttpEntity<Object> entity = new HttpEntity<>(request);
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType).getBody();
    }

    public <T> T customPost(String url, Object request, Class<T> responseType) {
        return restTemplate.postForObject(url, request, responseType);
    }

    public void delete(String service, String path, Object... params) {
        String url = serviceUrls.get(service) + path;
        restTemplate.delete(url, params);
    }

    public enum ServiceType {
        product,
        llm,
        mcpserver,
    }
}
