package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.client.GeodeClient;
import io.pivotal.services.dataTx.geode.serialization.PDX;
import io.pivotal.services.dataTx.geode.serialization.SerializationPdxEntryWrapper;
import io.pivotal.services.dataTx.spring.batch.geode.GeodePdxItemReader;
import nyla.solutions.core.data.MapEntry;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * Configuration focused on the Spring Batch job definitions and dependencies.
 *
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

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

    @StepScope
    @Bean
    public KafkaItemWriter kafkaItemWriter(
            KafkaTemplate<String, SerializationPdxEntryWrapper> kafkaTemplate,
            @Value("#{jobParameters['topic']}") String topic)
    {
        KafkaItemWriter writer = new KafkaItemWriter();
        kafkaTemplate.setDefaultTopic(topic);
        writer.setKafkaTemplate(kafkaTemplate);
        Converter<SerializationPdxEntryWrapper, Serializable> converter = (pdx) -> pdx.deserializeKey();
        writer.setItemKeyMapper(converter);

        return writer;
    }//-------------------------------------------

    /**
     * The Item Reader
     * @param topicName the topic the name
     * @return the created item reader
     */
    @StepScope
    @Bean
    public KafkaGeodePdxItemReader kafkaItemReader(
            @Value("#{jobParameters['topic']}") String topicName)
    {
        KafkaGeodePdxItemReader itemReader = new KafkaGeodePdxItemReader(kakfaBootstrapServers,
                topicName,
                kakfaGroup);


        return itemReader;

    }//-------------------------------------------
    /*@StepScope
    @Bean
    public KafkaItemReader kafkaItemReader(
            @Value("#{jobParameters['topic']}") String topicName,
            @Value("#{jobParameters[kafka.read.partitions']}") List<Integer> partitions)
    {


        Properties consumerProperties = new Properties();
        consumerProperties.setProperty("bootstrap.servers", kakfaBootstrapServers);
        consumerProperties.setProperty("group.id",kakfaGroup);
        consumerProperties.setProperty("key.deserializer", JsonDeserializer.class.getName());
        consumerProperties.setProperty("value.deserializer",JsonDeserializer.class.getName());
        consumerProperties.setProperty("spring.json.trusted.packages","io.pivotal.services.dataTx.geode.serialization");

        System.out.println("*******************topicName:"+topicName);

        KafkaTemplate<String,String> kafkaTemplate = new KafkaTemplate<>(null);


        KafkaItemReader reader = new KafkaItemReader(consumerProperties,topicName,partitions);


        return reader;
    }*/
    //-------------------------------------------

    @StepScope
    @Bean
    public GeodePdxItemReader reader(
            @Value("#{jobParameters['region']}") String regionName)
    {
        Region<Serializable, PdxInstance> region = GeodeClient.getRegion(ClientCacheFactory.getAnyInstance(), regionName);

        return new GeodePdxItemReader(region, 10);
    }//-------------------------------------------

    /**
     *
     * @param regionName the region to write entries
     * @return The item writer for Apache Geode
     */
    @StepScope
    @Bean
    public GeodePdxWrapperItemWriter geodeWriter(@Value("#{jobParameters['region']}") String regionName)
    {
        Region<Serializable, PdxInstance> region = GeodeClient
                .getRegion(ClientCacheFactory.getAnyInstance(), regionName);

        return new GeodePdxWrapperItemWriter(region);
    }//-------------------------------------------
    @StepScope
    @Bean("fromGeodePdxToKakfaItemProcesor")
    ItemProcessor<Map.Entry<Serializable,PdxInstance>,SerializationPdxEntryWrapper<Serializable>> fromGeodePdxToKakfaItemProcessor
            (
            @Value("#{jobParameters['valueClassName']}")
                    String valueClassName)
    {
        ItemProcessor<Map.Entry<Serializable,PdxInstance>,SerializationPdxEntryWrapper<Serializable>> processor;
        processor = mapEntry -> PDX.toSerializePdxEntryWrapper(
                mapEntry.getKey(),valueClassName,
                mapEntry.getValue());

        return processor;
    }//-------------------------------------------
    /**
     * Job step to reader from Apache Geode and write to Apache Kafka
     * @param reader the Geode reader
     * @param writer the
     * @return Step
     */
    @Bean("fromGeodePdxToKakfaStep")
    public Step fromGeodePdxToKakfaStep(GeodePdxItemReader reader,
                                        @Autowired
                                        @Qualifier("fromGeodePdxToKakfaItemProcesor")
                                        ItemProcessor<Map.Entry<Serializable,PdxInstance>,
                                        SerializationPdxEntryWrapper<Serializable>>
                                        processor,
                                        KafkaItemWriter writer,
                                        @Value(("${geode.to.kakfa.chunkSize}"))
                                         int chunkSize,
                                        TaskExecutor taskExecutor)
    {


        return stepBuilderFactory

                .get("fromGeodePdxToKakfaStep")
                .chunk(chunkSize)
                .reader(reader)
                .processor((ItemProcessor)processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .build();
    }//-------------------------------------------
    @Bean("fromKakfaToGeodePdxStep")
    public Step fromKakfaToGeodePdxStep(
            KafkaGeodePdxItemReader kakfaReader,
            GeodePdxWrapperItemWriter geodeWriter,
            @Value(("${kakfa.to.geode.chunkSize}"))
            int chunkSize,
            TaskExecutor taskExecutor)
    {

        /*
        Function<SerializationPdxEntryWrapper, Map.Entry> processor = wrapper ->
        {
            System.out.println("wrapper.key:"+wrapper.getKeyString());

            return new MapEntry
                    (wrapper.deserializeKey(),
                            wrapper.toPdxInstance());
        };
        */


        return stepBuilderFactory

                .get("fromGeodePdxToKakfaStep")
                .chunk(chunkSize)
                .reader(kakfaReader)
                //.processor((Function) processor)
                .writer((ItemWriter) geodeWriter)
                .taskExecutor(taskExecutor)
                .build();

    }//-------------------------------------------
}