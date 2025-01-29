package com.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public class Server {

    private Server() {
    }

    public static void main(String[] args) throws Exception {
        try {

            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);

            SSLContext sslContext = myServerSSLContext();
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });

            UserAuthenticator authenticator = new UserAuthenticator();
            HttpContext httpContext = server.createContext("/info", new MessageHandler());
            HttpContext topFiveContext = server.createContext("/topfive", new TopFiveHandler());
            HttpContext registrationContext = server.createContext("/registration",
                    new RegistrationHandler(authenticator));

            httpContext.setAuthenticator(authenticator);

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLContext myServerSSLContext() throws Exception {
        char[] passphrase = "perkele".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore.jks"), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

}