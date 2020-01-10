package io.pivotal.services.dataTx.geodekakfaconnector;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.services.dataTx.geode.serialization.SerializationPdxEntryWrapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for KafkaGeodePdxItemReader
 * @author Gregory Green
 */
public class KafkaGeodePdxItemReaderTest
{
    @Test
    void test_reader_returns_valid_Data()
            throws Exception
    {
        String bootstrapServers = "";
        String topic = "";
        String groupId = "";

        String expectedKey = "key";
        String expectedValueJson = "{\"@type\":\""+UserQaData.class.getName()+"\",\"email\":\"e@mail.com\"}";
        SerializationPdxEntryWrapper expectedWrapper = new SerializationPdxEntryWrapper();
        expectedWrapper.setValueJson(expectedValueJson);
        expectedWrapper.setKeyClassName(String.class.getName());
        expectedWrapper.setKeyString(expectedKey);


        String expectedWrapperJsonValue = new ObjectMapper()
                .writeValueAsString(expectedWrapper);


        KafkaGeodePdxItemReader reader = new KafkaGeodePdxItemReader(
                bootstrapServers,
                topic,groupId);


        reader.iterator = mock(Iterator.class);
        when(reader.iterator.hasNext()).thenReturn(true) //first return true
                .thenReturn(false); //then return false

        ConsumerRecord<String,String> expectedConsumerRecord = mock(ConsumerRecord.class);
        when(expectedConsumerRecord.key()).thenReturn(expectedKey);
        when(expectedConsumerRecord.value()).thenReturn(expectedWrapperJsonValue);


        when(reader.iterator.next()).thenReturn(expectedConsumerRecord);

        assertEquals(expectedWrapper,reader.read());


    }
}