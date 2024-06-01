package com.sen.senojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.sen.senojbackendquestionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.sen")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.sen.senojbackendserviceclient.service"})
public class SenojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SenojBackendQuestionServiceApplication.class, args);
    }

}
