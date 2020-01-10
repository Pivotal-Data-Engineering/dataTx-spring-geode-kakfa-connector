package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.serialization.PDX;
import io.pivotal.services.dataTx.geode.serialization.SerializationPdxEntryWrapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.batch.item.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

/**
 *
 * Spring Batch Item Writer for the Apache Kafka events
 * @author Gregory Green
 */
public class KafkaGeodePdxItemReader
        implements ItemReader<SerializationPdxEntryWrapper>, ItemStream
{

    private final String groupId;
    private final String bootstrapServers;
    private final String topic;
    private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private ConsumerRecords<String,String> records = null;
    private KafkaConsumer<String,String> kafkaConsumer = null;
    Iterator<ConsumerRecord<String,String>> iterator = null;

    private long timeoutMs = 1000*10; //10 seconds

    /**
     * The constructor
     * @param bootstrapServers the kafka servers connections
     * @param topic the Apache Kakfa topic
     * @param groupId the consumer group ID
     */
    public KafkaGeodePdxItemReader(String bootstrapServers, String topic, String groupId)
    {
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.topic = topic;

    }//-------------------------------------------

    @Override
    public SerializationPdxEntryWrapper read()
    throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException
    {
        synchronized (iterator)
        {
            if(!this.iterator.hasNext())
                return null;

            ConsumerRecord<String,String> record = iterator.next();

            return PDX.toSerializePdxEntryWrapperFromJson
                    (String.valueOf(record.value()));
        }


    }//-------------------------------------------

    @Override
    public void open(ExecutionContext executionContext)
    throws ItemStreamException
    {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("client.id","kakaGeodeConnector");
        props.put("key.deserializer",keyDeserializer);
        props.put("value.deserializer",valueDeserializer);
        props.put("enable.auto.commit","false");
        props.put("auto.offset.reset","earliest");

        kafkaConsumer = new KafkaConsumer<String, String>(props);

        synchronized (kafkaConsumer)
        {
            kafkaConsumer.subscribe(Collections.singleton(topic));

            this.records = kafkaConsumer.
                    poll(Duration.ofMillis(timeoutMs));

            iterator = this.records.iterator();
        }


    }//-------------------------------------------

    @Override
    public void update(ExecutionContext executionContext)
    throws ItemStreamException
    {
        synchronized (kafkaConsumer)
        {
          //  kafkaConsumer.commitAsync();
        }
    }//-------------------------------------------

    @Override
    public void close()
    throws ItemStreamException
    {
        synchronized (kafkaConsumer)
        {
            this.kafkaConsumer.close();
            this.kafkaConsumer = null;
        }
    }
}
