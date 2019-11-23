package io.pivotal.services.dataTx.geodekakfaconnector;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.config.annotation.EnableStatistics;
import org.springframework.data.gemfire.function.config.EnableGemfireFunctionExecutions;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Gregory Green
 */
@Configuration
@EnableGemfireRepositories
@EnableSwagger2
@EnableGemfireFunctionExecutions
@EnableStatistics
@EnableGemfireCaching
@EnableEntityDefinedRegions(clientRegionShortcut = ClientRegionShortcut.PROXY)
@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.PROXY)
@EnablePdx(readSerialized = true)
@EnableBinding(Source.class)
public class GeodeKafkaConfig
{

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }


    //@Autowired
    //public JobBuilderFactory jobBuilderFactory;

}
