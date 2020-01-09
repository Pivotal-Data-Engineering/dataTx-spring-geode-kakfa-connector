package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.serialization.PDX;
import io.pivotal.services.dataTx.geode.serialization.SerializationPdxEntryWrapper;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.batch.item.ItemWriter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Gregory Green
 */
public class GeodePdxWrapperItemWriter implements ItemWriter<SerializationPdxEntryWrapper>
{
    private final Region<Serializable,PdxInstance> region;

    public GeodePdxWrapperItemWriter(Region<Serializable, PdxInstance> region)
    {
        this.region = region;
    }//-------------------------------------------

    @Override
    public void write(List<? extends SerializationPdxEntryWrapper> items)
    throws Exception
    {
        if(items == null || items.isEmpty())
            return;

       try
       {
           Map<Serializable,PdxInstance> map = items.stream().collect
                   (
                           Collectors.toMap
                                   (
                                           wrapper -> wrapper.deserializeKey(),
                                           wrapper -> PDX.fromJSON(wrapper.getValueJson()),
                                           (a,b) -> a
                                   )
                   );

           region.putAll(map);
       }
       catch(RuntimeException e)
       {
           e.printStackTrace();
           throw e;
       }

    }
}
