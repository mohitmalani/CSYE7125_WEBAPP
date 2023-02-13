package com.csye7125.webapp.webapp.configuration.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class Config {

    @Value(value = "${spring.elastic.bootstrap-servers.host}")
    private String bootstrapAddress;

    @Value(value = "${spring.elastic.bootstrap-servers.port}")
    private int bootstrapPort;

    @Bean
    public RestClient getRestClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(bootstrapAddress, bootstrapPort)).build();
        return restClient;
    }

    @Bean
    public ElasticsearchTransport getElasticsearchTransport() {
        return new RestClientTransport(
                getRestClient(), new JacksonJsonpMapper());
    }


    @Bean
    public ElasticsearchClient getElasticsearchClient(){
        ElasticsearchClient client = new ElasticsearchClient(getElasticsearchTransport());
        return client;
    }
}