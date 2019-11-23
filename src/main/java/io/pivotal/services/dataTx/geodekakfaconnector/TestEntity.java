package io.pivotal.services.dataTx.geodekakfaconnector;

import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.data.gemfire.mapping.annotation.ReplicateRegion;

/**
 * @author Gregory Green
 */
@Region("Test")
@EnablePdx(serializerBeanName = "pdxSerializer")
public class TestEntity
{
    @Id
    private String id;

    private String value;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
