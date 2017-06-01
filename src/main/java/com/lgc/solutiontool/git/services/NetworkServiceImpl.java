package com.lgc.solutiontool.git.services;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.function.Consumer;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.lgc.solutiontool.git.util.RequestType;
import com.lgc.solutiontool.git.util.URLManager;

public class NetworkServiceImpl implements NetworkService {

    private static final Logger logger = LogManager.getLogger(NetworkServiceImpl.class);

    private final int WRONG_RESPONSE = -1;
    private final int TIMEOUT = 10000; // timeout after 10 seconds
    private final String URL_SUFFIX = "/user";

    @Override
    public void runURLVerification(String inputURL, Consumer<Integer> handler) {
        Runnable runnable = () -> {
            String url = URLManager.trimServerURL(inputURL);
            int responseCode = getServerResponseCode(url);
            handler.accept(responseCode);
        };
        new Thread(runnable).start();
    }

    private int getServerResponseCode(String serverURL) {
        int responseCode = WRONG_RESPONSE;
        try {
            // handshake
            URL obj = new URL(URLManager.completeServerURL(serverURL) + URL_SUFFIX);
            HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestMethod(RequestType.GET.toString());
            responseCode = connection.getResponseCode();
        } catch (SocketTimeoutException e1) {
            logger.error(e1.getMessage()); // no need to log stackTrace here
            return HttpStatus.SC_GATEWAY_TIMEOUT;
        } catch (SocketException e2) {
            logger.error(e2.getMessage()); // no need to log stackTrace here
            return HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } catch (Exception e3) {
            logger.error(e3.getMessage()); // no need to log stackTrace here
            return WRONG_RESPONSE;
        }
        return responseCode;
    }

}
