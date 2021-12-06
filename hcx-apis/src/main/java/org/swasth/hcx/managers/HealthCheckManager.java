package org.swasth.hcx.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.swasth.common.dto.Response;
import org.swasth.hcx.utils.Constants;
import org.swasth.kafka.client.KafkaClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HealthCheckManager {

    @Autowired
    KafkaClient kafkaClient;

    public Response checkAllSystemHealth(){
        List<Map<String,Object>> allChecks = new ArrayList<>();
        boolean allSystemHealthResult = true;
        allChecks.add(generateCheck(Constants.KAFKA, kafkaClient.isHealthy()));
        for(Map<String,Object> check:allChecks) {
            if(!(boolean)check.get(Constants.HEALTHY))
                allSystemHealthResult = false;
        }
        Response response = new Response(Constants.CHECKS, allChecks);
        response.put(Constants.HEALTHY, allSystemHealthResult);
        return response;
    }

    private Map<String,Object> generateCheck(String serviceName, boolean health){
        return new HashMap<>() {{
            put(Constants.NAME, serviceName);
            put(Constants.HEALTHY, health);
        }};
    }

}
