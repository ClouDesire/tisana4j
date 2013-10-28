package com.cloudesire.tisana4j;

import java.io.InputStream;

public interface ExceptionTranslator
{
	public Exception translateError ( int responseCode, String responseMessage, InputStream errorStream );
}
