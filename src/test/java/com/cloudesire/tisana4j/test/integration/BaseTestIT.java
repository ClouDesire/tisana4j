package com.cloudesire.tisana4j.test.integration;

import com.cloudesire.tisana4j.RestClient;
import com.cloudesire.tisana4j.RestClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;

public abstract class BaseTestIT
{
    protected ObjectMapper jsonMapper = new ObjectMapper();
    protected RestClient restClient = RestClientFactory.getDefaultClient();

    @Before
    public void setUp() throws Exception
    {
        restClient.setFailOnUknownField( false );
    }

}
