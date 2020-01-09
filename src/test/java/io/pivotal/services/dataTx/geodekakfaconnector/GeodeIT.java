package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.client.GeodeClient;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Gregory Green
 */
@SpringBootTest(classes = GeodeIT.class)
@Disabled
public class GeodeIT
{

    @Test
    public void test_geode()
            throws Exception
    {

        System.setProperty("LOCATORS","localhost[10000]");

        ClientCache cache = GeodeClient.connect().getClientCache();

        Region<String, PdxInstance> region = GeodeClient
                                            .getRegion(
                                                    cache,
                                                    "testout");

        String expectedKey = "delete";
        String expectedEmail = "email@pivotal.io";

        String json = "{\"@type\" : \""+UserQaData.class.getName()
                +"\", \"email\" : \""+expectedEmail+"\"}";


        PdxInstance pdxInstance = JSONFormatter.fromJSON(json);


        region.put(expectedKey,pdxInstance);

        UserQaData user = (UserQaData)region.get(expectedKey).getObject();

        Assertions.assertEquals(expectedEmail,user.getEmail());


    }//-------------------------------------------
}
