package io.pivotal.services.dataTx.geodekakfaconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;

/**
 * Main Class
 * @author Gregory Green
 */
@SpringBootApplication
@ClientCacheApplication
public class GeodeKakfaConnectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeodeKakfaConnectorApplication.class, args);
	}
}
