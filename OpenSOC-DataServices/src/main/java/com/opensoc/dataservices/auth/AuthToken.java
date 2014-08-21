package com.opensoc.dataservices.auth;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

public class AuthToken {

	
	
	public static String generateToken( final Properties configProps ) throws Exception
	{
		
		KeyStore ks = KeyStore.getInstance("JCEKS");
		String keystoreFile = configProps.getProperty( "keystoreFile" );
		String keystorePassword = configProps.getProperty( "keystorePassword" );
		String keystoreAlias = configProps.getProperty( "authTokenAlias" );
		
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream( keystoreFile );
			ks.load(fis, keystorePassword.toCharArray() );
		}
		finally {
			fis.close();
		}
		
		KeyStore.ProtectionParameter protParam =
		        new KeyStore.PasswordProtection(keystorePassword.toCharArray());
		KeyStore.SecretKeyEntry  secretKeyEntry = (KeyStore.SecretKeyEntry)ks.getEntry(keystoreAlias, protParam);
		
		SecretKey key = secretKeyEntry.getSecretKey();

		
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);		
		
		byte[] encryptedData = cipher.doFinal("OpenSOC_AuthToken".getBytes());	
		
		Base64.Encoder encoder = Base64.getEncoder();
		String base64Token = encoder.encodeToString( encryptedData );
		
		// System.out.println( "base64Token: " + base64Token );
		
		return base64Token;
		
	}
	
	public static boolean validateToken( final Properties configProps, String authToken ) throws Exception
	{
		KeyStore ks = KeyStore.getInstance("JCEKS");
		String keystoreFile = configProps.getProperty( "keystoreFile" );
		String keystorePassword = configProps.getProperty( "keystorePassword" );
		String keystoreAlias = configProps.getProperty( "authTokenAlias" );
		
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream( keystoreFile );
			ks.load(fis, keystorePassword.toCharArray() );
		}
		finally {
			if( fis != null) {
				fis.close();
			}
		}
		
		KeyStore.ProtectionParameter protParam =
		        new KeyStore.PasswordProtection(keystorePassword.toCharArray());
		KeyStore.SecretKeyEntry  secretKeyEntry = (KeyStore.SecretKeyEntry)ks.getEntry(keystoreAlias, protParam);
		
		SecretKey key = secretKeyEntry.getSecretKey();
		
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key);		
		
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] encryptedBytes = decoder.decode(authToken);
		
		byte[] unencryptedBytes = cipher.doFinal(encryptedBytes);
		String clearTextToken = new String( unencryptedBytes );
		
		// System.out.println( "clearTextToken: " + clearTextToken );
		
		if( clearTextToken.equals( "OpenSOC_AuthToken" ))
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public static void main( String[] args ) throws Exception
	{
		
    	Options options = new Options();
    	
    	options.addOption( "keystoreFile", true, "Keystore File" );
    	options.addOption( "keystorePassword", true, "Keystore Password" );
    	options.addOption( "authTokenAlias", true, "");
    	
    	CommandLineParser parser = new GnuParser();
    	CommandLine cmd = parser.parse( options, args);
		
		
		try
		{
			KeyStore ks = KeyStore.getInstance("JCEKS");

			String keystorePassword = cmd.getOptionValue("keystorePassword");
			String keystoreFile = cmd.getOptionValue("keystoreFile");
			String authTokenAlias = cmd.getOptionValue("authTokenAlias");

			ks.load(null, keystorePassword.toCharArray());

			
			// generate a key and store it in the keystore...
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			SecretKey key = keyGen.generateKey();
			
			KeyStore.ProtectionParameter protParam =
			        new KeyStore.PasswordProtection(keystorePassword.toCharArray());
			
			
			KeyStore.SecretKeyEntry skEntry =
			        new KeyStore.SecretKeyEntry(key);
			
			ks.setEntry(authTokenAlias, skEntry, protParam);
			
			java.io.FileOutputStream fos = null;
		    try {
		        
		    	fos = new java.io.FileOutputStream(keystoreFile);
		        ks.store(fos, keystorePassword.toCharArray());
		    } 
		    finally {
		        
		    	if (fos != null) {
		            fos.close();
		        }
		    }
			
		    
		    System.out.println( "done" );
		    
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
