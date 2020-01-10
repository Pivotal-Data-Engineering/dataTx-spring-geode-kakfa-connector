package io.pivotal.services.dataTx.geodekakfaconnector;

import nyla.solutions.core.data.MapEntry;
import nyla.solutions.core.util.Organizer;
import org.apache.geode.cache.Region;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.Serializable;
import java.util.*;

/**
 * @author Gregory Green
 */
public class GeodePdxItemReader<K extends Serializable> implements ItemReader<Map.Entry<K,PdxInstance>>
{
    private final Region<K, PdxInstance> region;
    private final List<Collection<K>> keySet;
    private final Iterator<Collection<K>> keyIterator;
    private Map<K,PdxInstance> currentBatchMap =null;
    private Set<K> currentBatchKeySet= null;
    private Iterator<K> currentBatchKeySetIterator= null;
    private long i = 0;

    public  GeodePdxItemReader(Region<K, PdxInstance> region,int batchSize)
    {
        this.region = region;

        if(this.region == null)
        {
            this.keySet = null;
            this.keyIterator = null;
            return;
        }

        Set<K> keysOnServer = this.region.keySetOnServer();
        if(keysOnServer == null || keysOnServer.isEmpty())
        {
            this.keySet = null;
            this.keyIterator = null;
            return;
        }

        this.keySet = Organizer.toPages(keysOnServer,batchSize);
        this.keyIterator = this.keySet.iterator();

    }




    public Map.Entry<K,PdxInstance> read()
    throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException
    {

        if(currentBatchMap != null &&
                currentBatchKeySet != null  &&
                currentBatchKeySetIterator.hasNext()){
            K key = currentBatchKeySetIterator.next();
            return new MapEntry<>(key,currentBatchMap.get(key));
        }


        if(this.keyIterator == null || !this.keyIterator.hasNext())
            return null;

        currentBatchMap = this.region.getAll(this.keyIterator.next());

        if(currentBatchMap == null)
        {
            currentBatchKeySet = null;
            return null;

        }

        currentBatchKeySet = currentBatchMap.keySet();
        currentBatchKeySetIterator = currentBatchKeySet.iterator();


        K key = currentBatchKeySetIterator.next();
        return new MapEntry<>(key,currentBatchMap.get(key));

    }
}
