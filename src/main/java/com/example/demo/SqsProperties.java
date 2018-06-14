package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.aws.sqs")
public class SqsProperties {
    private Integer prefetch = 1;

    public Integer getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(Integer prefetch) {
        this.prefetch = prefetch;
    }
}
