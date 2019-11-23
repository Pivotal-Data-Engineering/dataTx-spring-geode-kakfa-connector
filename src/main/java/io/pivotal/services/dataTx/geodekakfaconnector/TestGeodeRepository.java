package io.pivotal.services.dataTx.geodekakfaconnector;

import org.springframework.context.annotation.Scope;
import org.springframework.data.gemfire.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

/**
 * @author Gregory Green
 */
//@Service

public interface TestGeodeRepository extends CrudRepository<TestEntity,String>
{
    @Query("select email from /USERS u, /Test t ")
    public String findIdEmail();
}
