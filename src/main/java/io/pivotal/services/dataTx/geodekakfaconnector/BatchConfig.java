package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.client.GeodeClient;
import io.pivotal.services.dataTx.geode.serialization.PDX;
import io.pivotal.services.dataTx.geode.serialization.SerializationPdxEntryWrapper;
import io.pivotal.services.dataTx.spring.batch.geode.GeodeMapEntryWriter;
import io.pivotal.services.dataTx.spring.batch.geode.GeodePdxItemReader;
import nyla.solutions.core.data.MapEntry;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.kafka.KafkaItemReader;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.Serializable;
import java.security.Key;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * @author Gregory Green
 */
@Configuration
@EnableBatchProcessing
public class BatchConfig
{
    /**
     * Spring Batch step builder
     */
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Value("${spring.kafka.consumer.group-id}")
    private String kakfaGroup;

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String kakfaBootstrapServers;

    //s@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    /*@Bean("itemProcessor")
    public Function<Map.Entry<Serializable, PdxInstance>, SerializationPdxEntryWrapper> itemProcessor(
            @Value("#{jobParameters['valueClassName']}") String valueClassName)
    {
        return (pdxEntry) -> PDX.toSerializePdxEntryWrapper(pdxEntry.getKey(), valueClassName, pdxEntry.getValue());
    }*/
    //-------------------------------------------

    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean
    public KafkaItemWriter kafkaItemWriter(KafkaTemplate<String, SerializationPdxEntryWrapper> kafkaTemplate,
                                           @Value("#{jobParameters['topic']}") String topic)
    {
        KafkaItemWriter writer = new KafkaItemWriter();
        kafkaTemplate.setDefaultTopic(topic);
        writer.setKafkaTemplate(kafkaTemplate);
        Converter<SerializationPdxEntryWrapper, Serializable> converter = (pdx) -> pdx.deserializeKey();
        writer.setItemKeyMapper(converter);

        return writer;
    }//-------------------------------------------
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean
    public KafkaItemReader kafkaItemReader(KafkaTemplate<String, SerializationPdxEntryWrapper> kafkaTemplate,
                                           @Value("#{jobParameters['topic']}") String topicName)
    {

        Properties consumerProperties = new Properties();
        consumerProperties.setProperty("bootstrap.servers", kakfaBootstrapServers);
        consumerProperties.setProperty("group.id",kakfaGroup);
        consumerProperties.setProperty("key.deserializer", JsonDeserializer.class.getName());
        consumerProperties.setProperty("value.deserializer",JsonDeserializer.class.getName());
        consumerProperties.setProperty("spring.json.trusted.packages","io.pivotal.services.dataTx.geode.serialization");

        System.out.println("*******************topicName:"+topicName);
        KafkaItemReader reader = new KafkaItemReader(consumerProperties,topicName,0,1,2);



        return reader;
    }//-------------------------------------------

    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean
    public GeodePdxItemReader reader(@Value("#{jobParameters['region']}") String regionName)
    {
        Region<Serializable, PdxInstance> region = GeodeClient.getRegion(ClientCacheFactory.getAnyInstance(), regionName);

        return new GeodePdxItemReader(region, 10);
    }//-------------------------------------------

    /**
     *
     * @param regionName the region to write entries
     * @return The item writer for Apache Geode
     */
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean
    public GeodeMapEntryWriter<Serializable,Serializable> geodeWriter(@Value("#{jobParameters['region']}") String regionName)
    {
        Region<Serializable, Serializable> region = GeodeClient.getRegion(ClientCacheFactory.getAnyInstance(), regionName);

        return new GeodeMapEntryWriter(region);
    }//-------------------------------------------
    @Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean("fromGeodePdxToKakfaItemProcesor")
    ItemProcessor<Map.Entry<Serializable,PdxInstance>,SerializationPdxEntryWrapper<Serializable>> fromGeodePdxToKakfaItemProcessor
            (
            @Value("#{jobParameters['valueClassName']}")
                    String valueClassName)
    {
        ItemProcessor<Map.Entry<Serializable,PdxInstance>,SerializationPdxEntryWrapper<Serializable>> processor;
        processor = mapEntry -> PDX.toSerializePdxEntryWrapper(mapEntry.getKey(),valueClassName,mapEntry.getValue());

        return processor;
    }//-------------------------------------------
    /**
     * Job step to reader from Apache Geode and write to Apache Kafka
     * @param reader the Geode reader
     * @param writer the
     * @return
     */
    //@Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean("fromGeodePdxToKakfaStep")
    public Step fromGeodePdxToKakfaStep(GeodePdxItemReader reader,
                                        @Autowired
                                        @Qualifier("fromGeodePdxToKakfaItemProcesor")
                                        ItemProcessor<Map.Entry<Serializable,PdxInstance>,SerializationPdxEntryWrapper<Serializable>>
                                                processor
                                       ,
                                        KafkaItemWriter writer)
    {


        return stepBuilderFactory

                .get("fromGeodePdxToKakfaStep")
                .chunk(10)
                .reader(reader)
                .processor((ItemProcessor)processor)
                .writer(writer)
                .build();
    }//-------------------------------------------

    @Bean("fromKakfaToGeodePdxStep")
    public Step fromKakfaToGeodePdxStep(
            KafkaItemReader<Serializable, SerializationPdxEntryWrapper<Key>> kakfaReader,
            GeodeMapEntryWriter<Serializable, Serializable> geodeWriter)
    {

        Function<SerializationPdxEntryWrapper, Map.Entry> processor = wrapper ->
        {
            System.out.println("wrapper.key:"+wrapper.getKeyString());

            return new MapEntry
                    (wrapper.deserializeKey(),
                            wrapper.toPdxInstance());
        };

        return stepBuilderFactory

                .get("fromGeodePdxToKakfaStep")
                .chunk(10)
                .reader(kakfaReader)
                .processor((Function) processor)
                .writer(geodeWriter).build();

    }
}