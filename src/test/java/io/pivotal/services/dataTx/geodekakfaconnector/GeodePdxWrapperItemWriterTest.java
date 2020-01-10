package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.serialization.SerializationPdxEntryWrapper;
import org.apache.geode.cache.CacheClosedException;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for GeodePdxWrapperItemWriter
 * @author Gregory Green
 */
public class GeodePdxWrapperItemWriterTest
{
    @BeforeAll
    public static void setUp()
    {
        try
        {

            if(CacheFactory.getAnyInstance() == null)
                new CacheFactory().create();
        }
        catch(CacheClosedException e)
        {
            new CacheFactory().create();
        }
    }//-------------------------------------------

    @Test
    void test_write_into_geode()
            throws Exception
    {
        Region<Serializable, PdxInstance> region = mock(Region.class);
        GeodePdxWrapperItemWriter writer = new GeodePdxWrapperItemWriter(region);

        String expectedKey = "key";
        String expectedValueJson = "{\"@type\":\""+UserQaData.class.getName()+"\",\"email\":\"e@mail.com\"}";
        SerializationPdxEntryWrapper expectedWrapper = new SerializationPdxEntryWrapper();
        expectedWrapper.setValueJson(expectedValueJson);
        expectedWrapper.setKeyClassName(String.class.getName());
        expectedWrapper.setKeyString(expectedKey);


        List<? extends SerializationPdxEntryWrapper> list = Collections.singletonList(expectedWrapper);

        writer.write(list);

        verify(region).putAll(any());

    }
}