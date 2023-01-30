package org.swasth.hcx;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.swasth.common.utils.JSONUtils;
import org.swasth.hcx.controllers.HealthController;

import java.util.Map;

@EnableAspectJAutoProxy
@SpringBootApplication
public class HCXApplication {

	public static void main(String[] args) throws JsonProcessingException {
		SpringApplication.run(HCXApplication.class, args);
		HealthController healthController = new HealthController();
		System.out.println(JSONUtils.serialize(healthController.serviceHealth().getBody()));
	}

}
