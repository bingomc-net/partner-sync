package net.bingomc.partnersync;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.leangen.geantyref.TypeToken;
import net.bingomc.partnersync.exception.OutsideQuotaException;
import net.bingomc.partnersync.model.Server;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;

public class RestUtils {
    public static Server sendServerPostRequest()  {
        Gson gson = new Gson();

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s", PartnerSyncPlugin.getApiUrl(), "servers")))
                .header("Api-Key", PartnerSyncPlugin.getApiKey())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = null;
        try {
            postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Something went wrong");
        }

        if (postResponse.statusCode() == 400) {
            throw new OutsideQuotaException("There's no more servers in the account's quota");
        }

        try {
            return gson.fromJson(postResponse.body(), Server.class);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Something went wrong");
        }
    }

    public static List<Server> sendServerGetRequest() throws IOException, InterruptedException {
        Type collectionType = new TypeToken<Collection<Server>>() {
        }.getType();
        Gson gson = new Gson();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s", PartnerSyncPlugin.getApiUrl(), "servers")))
                .header("Api-Key", PartnerSyncPlugin.getApiKey())
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpResponse<String> getResponse = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(getResponse.body(), collectionType);
    }

    public static void sendServerDeleteRequest(int serverId)  {
        Gson gson = new Gson();

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s", PartnerSyncPlugin.getApiUrl(), "servers/" + serverId)))
                .header("Api-Key", PartnerSyncPlugin.getApiKey())
                .DELETE()
                .build();

        HttpClient httpClient = HttpClient.newHttpClient();
        try {
            httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Something went wrong");
        }
    }
}