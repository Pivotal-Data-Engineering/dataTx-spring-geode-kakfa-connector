package io.pivotal.services.dataTx.geodekakfaconnector;

import nyla.solutions.core.util.Organizer;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeodePdxItemReaderTest
{

    @Test
    void test_read()
    throws Exception
    {
        GeodePdxItemReader reader;
        Region<String, PdxInstance> region = mock(Region.class);
        Set<String> keys = Organizer.toSet("1","2","3");
        String expectedValue = "value";
        String expectedKey = "test";
        String fieldName = "id";

        //PDX mock
        PdxInstance expectedPdx = mock(PdxInstance.class);
        when(expectedPdx.getField(fieldName))
                .thenReturn(expectedValue)
                .thenReturn(expectedValue)
                .thenReturn(expectedValue)
                .thenReturn(expectedValue);

        Map<String,PdxInstance> expected = new HashMap<>();

        for (String key: keys)
        {
            expected.put(key,expectedPdx);
        }

        //mock region
        when(region.getAll(any())).thenReturn(expected);
        when(region.keySetOnServer()).thenReturn(keys);

        reader = new GeodePdxItemReader(region,2);


        for(String k : keys)
        {
            Map.Entry<String,PdxInstance> actual = reader.read();
            assertNotNull(actual);


            PdxInstance pdx = expected.get(k);

            System.out.println("key:"+k+" expected:"+expected);
            assertNotNull(pdx);
            String fieldValue = (String)pdx.getField(fieldName);

            assertEquals(fieldValue,
                    actual.getValue().getField(fieldName));
        }
    }
}