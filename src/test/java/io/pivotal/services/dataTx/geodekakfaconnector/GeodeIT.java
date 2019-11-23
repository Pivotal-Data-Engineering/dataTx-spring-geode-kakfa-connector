package io.pivotal.services.dataTx.geodekakfaconnector;

import io.pivotal.services.dataTx.geode.client.GeodeClient;
import nyla.solutions.core.security.user.data.UserProfile;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.pdx.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Gregory Green
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GeodeIT.class)
@ClientCacheApplication
@EnablePdx(readSerialized = true)
@Ignore
public class GeodeIT
{

    @Test
    public void test_geode()
            throws Exception
    {

        System.setProperty("LOCATORS","localhost[10000]");

        ClientCache cache = GeodeClient.connect().getClientCache();




        Region<String, PdxInstance> region = GeodeClient.getRegion(cache,"testout");


        String expectedKey = "delete";
        String expectedEmail = "email@pivotal.io";

        String json = "{\"@type\" : \""+UserQaData.class.getName()
                +"\", \"email\" : \""+expectedEmail+"\"}";


        PdxInstance pdxInstance = JSONFormatter.fromJSON(json);


        region.put(expectedKey,pdxInstance);

        UserQaData user = (UserQaData)region.get(expectedKey).getObject();

        Assert.assertEquals(expectedEmail,user.getEmail());




        /*
        PdxInstance pdxInstance = cache.createPdxInstanceFactory(UserProfile.class.getName())
                .writeString("@type",UserProfile.class.getName())
                .writeString("email","ggreen@pivotal.io")
                .create();


        WritablePdxInstance writer = pdxInstance.createWriter();
        writer.setField("@type",null);
        */


    }
}
