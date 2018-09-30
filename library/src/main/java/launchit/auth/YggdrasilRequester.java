package launchit.auth;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import launchit.auth.model.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class YggdrasilRequester
{
    private boolean debug = true;

    /**
     * Authenticate an user with his username and password and a previously acquired clientToken
     *
     * @return The appropriated response object
     * @throws IOException
     * @throws YggdrasilError
     */
    public YggdrasilAuthenticateRes authenticate(YggdrasilAgent agent, String username, String password, String clientToken) throws IOException, YggdrasilError {
        YggdrasilAuthenticateReq req = new YggdrasilAuthenticateReq();
        req.setAgent(agent);
        req.setUsername(username);
        req.setPassword(password);
        req.setClientToken(clientToken);

        return request(req, "authenticate", YggdrasilAuthenticateRes.class);
    }

    /**
     * Refresh an access token, provided access token gets invalidated and a new one is returned
     *
     * @return The appropriated response object
     * @throws IOException
     * @throws YggdrasilError
     */
    public YggdrasilRefreshRes refresh(String accessToken, String clientToken) throws IOException, YggdrasilError {
        YggdrasilRefreshReq req = new YggdrasilRefreshReq();
        req.setAccessToken(accessToken);
        req.setClientToken(clientToken);

        return request(req, "refresh", YggdrasilRefreshRes.class);
    }

    /**
     * Internal use only, build request to the yggdrasil authentication server
     *
     * @throws IOException
     * @throws YggdrasilError
     */
    private <T> T request(Object data, String route, Class<T> responseClass) throws IOException, YggdrasilError {
        long currentTime = System.currentTimeMillis();
        Gson gson = new Gson();

        String request = gson.toJson(data);

        if (debug)
            System.out.println(String.format("[%s] request:  %s", currentTime, request));


        URL url = new URL("https://authserver.mojang.com/" + route);
        byte[] postDataByte = request.getBytes();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Content-Length", Integer.toString(postDataByte.length));
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(postDataByte);
        wr.flush();
        wr.close();

        boolean status = String.valueOf(connection.getResponseCode()).startsWith("2");

        BufferedReader br = new BufferedReader(new InputStreamReader(status ? connection.getInputStream() : connection.getErrorStream()));
        String response = br.readLine();

        if (debug)
            System.out.println(String.format("[%s] response: %s", currentTime, response));


        if (responseClass != null && (response == null || response.isEmpty()))
            throw new IOException("Empty response");


        if (status)
            return responseClass == null ? null : gson.fromJson(response, responseClass);
        else
            throw new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(response, YggdrasilError.class);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
