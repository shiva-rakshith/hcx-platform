package org.swasth.hcx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.swasth.AWSClient;
import org.swasth.ICloudService;

@Configuration
public class AwsConfiguration {

    @Value("${certificates.awsAccesskey}")
    private String awsAccesskey;

    @Value("${certificates.awsSecretKey}")
    private String awsSecretKey;

    @Bean
    public ICloudService AwsCloudClient(){
        return new AWSClient(awsAccesskey,awsSecretKey);
    }

}
