package com.telenor.qlik;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SenseTicket {
	public static void main(String[] args) {
		// create a scanner so we can read the command-line input
	    Scanner scanner = new Scanner(System.in);

	    System.out.print("Enter Qlik Sense Server URL: ");
	    String host = scanner.next().trim();
	    
	    System.out.print("Enter Qlik Sense Virtual Proxy: ");
	    String vproxy = scanner.next().trim();
	    
	    System.out.print("Enter Qlik Certificates Directory: ");
	    String certFolder = scanner.next().trim();
	    
		String xrfkey = "7rBHABt65vFflaZ7"; // Xrfkey to prevent cross-site issues
												// Step 1
		try {

			/************** BEGIN Certificate Acquisition **************/
													// for securing ReST communication
			String proxyCert = certFolder + "client.jks"; // Reference to the client jks file which includes the client
															// certificate with private key
			String proxyCertPass = "Welcome1"; // This is the password to access the Java Key Store information
			String rootCert = certFolder + "root.jks"; // Reference to the root certificate for the client cert.
														// Required in this example because Qlik Sense certs are used.
			String rootCertPass = "Welcome1"; // This is the password to access the Java Key Store information
			/************** END Certificate Acquisition **************/

			/**************
			 * BEGIN Certificate configuration for use in connection
			 **************/
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(new File(proxyCert)), proxyCertPass.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, proxyCertPass.toCharArray());
			SSLContext context = SSLContext.getInstance("SSL");
			KeyStore ksTrust = KeyStore.getInstance("JKS");
			ksTrust.load(new FileInputStream(rootCert), rootCertPass.toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ksTrust);
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLSocketFactory sslSocketFactory = context.getSocketFactory();
			/**************
			 * END Certificate configuration for use in connection
			 **************/

			/************** BEGIN HTTPS Connection **************/
			System.out
					.println("Browsing to: " + "https://" + host + ":4243/qps/" + vproxy + "/ticket?xrfkey=" + xrfkey);
			URL url = new URL("https://" + host + ":4243/qps/" + vproxy + "/ticket?xrfkey=" + xrfkey);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setSSLSocketFactory(sslSocketFactory);
			connection.setRequestProperty("x-qlik-xrfkey", xrfkey);
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestMethod("POST");
			/************** BEGIN JSON Message to Qlik Sense Proxy API **************/

			String body = "{ 'UserId':'" + "ali" + "','UserDirectory':'" + "A0310TDAASSRV2" + "',";
			body += "'Attributes': [],";
			body += "}";
			System.out.println("Payload: " + body);
			/************** END JSON Message to Qlik Sense Proxy API **************/
			
			
			// Send post request
			connection.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
			wr.write(body);
			wr.flush(); // Get the response from the QPS BufferedReader
			wr.close();
			
			int responseCode = connection.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);


			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				builder.append(inputLine);
			}
			in.close();
			String data = builder.toString();
			System.out.println("The response from the server is: " + data);
			/************** END HTTPS Connection **************/
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}
}