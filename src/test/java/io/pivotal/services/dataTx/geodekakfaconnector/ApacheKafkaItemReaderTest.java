package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.serialization.SerializationPdxEntryWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.ExecutionContext;

import static org.junit.jupiter.api.Assertions.*;

public class ApacheKafkaItemReaderTest
{

    @Test
    public void test_item_reader()  throws Exception
    {
        String bootstrapServers = "localhost:9092";
        String topic = "test";
        String groupId = "group";

        KafkaGeodePdxItemReader reader = new KafkaGeodePdxItemReader(bootstrapServers,
                topic,
                groupId);

        ExecutionContext ec = Mockito.mock(ExecutionContext.class);
        reader.open(ec);

        SerializationPdxEntryWrapper entry = reader.read();

        assertNotNull(entry);

    }
}