package com.opensoc.dataservices.kafkaclient;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

public class Consumer implements Runnable 
{

	private KafkaStream m_stream;
    private int m_threadNumber;
    private RemoteEndpoint remote;
    
    public Consumer( RemoteEndpoint remote, KafkaStream a_stream, int a_threadNumber) 
    {
        this.m_threadNumber = a_threadNumber;
        this.m_stream = a_stream;
        this.remote = remote;
    }
 
    public void run() 
    {
		System.out.println( "calling ConsumerTest.run()" );
		ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
    
		while (it.hasNext())
		{    
			String message = new String(it.next().message());
			try 
			{
				remote.sendString( message );
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
    	
		System.out.println("Shutting down Thread: " + m_threadNumber);
    }
}
