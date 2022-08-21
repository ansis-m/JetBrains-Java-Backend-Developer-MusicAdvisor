package advisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;

public class Main {


    private static final String port = "8000"; //when changing the port a new authoriazation has to be made at Spotify
    private static String serverPath = "http://localhost:" + port;
    private final static String clientId = "e250b9f5fe2848f08f36f20b1274866a";
    private final static String clientSecret = "006e08a74dbd4d5caa1b5fdc8d247687";
    private final static String spotify = "https://accounts.spotify.com";
    private final static String defaultAPI = "https://api.spotify.com";
    private static String path;
    private static String APIpath;
    private static String url;
    private static int size = 5;

    private final static String clientIdSecret = clientId + ":" + clientSecret;
    private static String code = "";
    private static String accessToken = "";

    public static void main(String[] args) throws InterruptedException, IOException {


        getServerPath(args);
        Scanner scanner = new Scanner(System.in);
        HttpServer server = startServer(port);

        while (true) {
            String i = scanner.next();
            if(i.equals("auth")) {
                authorize(server);
                if (server != null)
                    server.stop(0);
                System.out.println("---SUCCESS---");
                break;
            }
            else if(i.equals("exit")) {
                if (server != null)
                    server.stop(0);
                scanner.close();
                System.exit(0);
            }
            else
                System.out.println("Please, provide access for application.");
        }
        scanner = new Scanner(System.in);
        HttpResponse<String> response;
        JsonObject jo;
        Pages pages = new Pages(size);
        while(true) {
            String i = scanner.nextLine();
            switch (i) {
                case ("exit"):
                    scanner.close();
                    System.out.println("---GOODBYE!---");
                    System.exit(0);
                case ("new"):
                    try {
                        response = getResponse("/v1/browse/new-releases");
                        jo = JsonParser.parseString(response.body()).getAsJsonObject();
                        pages.clear();

                        //System.out.println(jo);
                        for (JsonElement j : jo.getAsJsonObject("albums").getAsJsonArray("items")) {
                            StringBuilder builder = new StringBuilder();
                            if(j.isJsonObject()){
                                builder.append(j.getAsJsonObject().get("name").getAsString() + "\n");
                                builder.append("[");
                                boolean first = true;
                                for(JsonElement k : j.getAsJsonObject().getAsJsonArray("artists")){
                                    if(k.isJsonObject()) {
                                        if(!first)
                                            builder.append(", ");
                                        builder.append(k.getAsJsonObject().get("name").getAsString());
                                        first = false;
                                    }
                                }
                                builder.append("]\n");
                                builder.append(j.getAsJsonObject().getAsJsonObject("external_urls").get("spotify").getAsString() + "\n");
                                pages.addOutput(builder.toString());
                            }
                        }
                        pages.displayNext();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case ("featured"):
                    try {
                        response = getResponse("/v1/browse/featured-playlists");
                        jo = JsonParser.parseString(response.body()).getAsJsonObject();
                        pages.clear();
                        //System.out.println(jo);
                        for (JsonElement j : jo.getAsJsonObject("playlists").getAsJsonArray("items")) {
                            if(j.isJsonObject()){
                                pages.addOutput(j.getAsJsonObject().get("name").getAsString() + "\n" + j.getAsJsonObject().getAsJsonObject("external_urls").get("spotify").getAsString() + "\n");
                            }
                        }
                        pages.displayNext();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ("categories"):
                    try {
                        response = getResponse("/v1/browse/categories");
                        jo = JsonParser.parseString(response.body()).getAsJsonObject();
                        pages.clear();
                        for (JsonElement j : jo.getAsJsonObject("categories").getAsJsonArray("items")) {
                            pages.addOutput(j.getAsJsonObject().get("name").getAsString());
                        }
                        pages.displayNext();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ("next"):
                    pages.displayNext();
                    break;
                case ("prev"):
                    pages.displayPrev();
                    break;
                default:
                    if(i.startsWith("playlists")) {
                        try {
                            getPlaylist(i, pages);
                            pages.displayNext();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    else {
                            System.out.println("else");
                    }
                    break;
            }
        }
    }

    private static void getPlaylist(String i, Pages pages) {

        String id = null;
        HttpResponse<String> response;

        try {
            String name = i.split(" ", 2)[1];
            response = getResponse("/v1/browse/categories");
            JsonObject jo = JsonParser.parseString(response.body()).getAsJsonObject();

            for (JsonElement j : jo.getAsJsonObject("categories").getAsJsonArray("items")) {

                if (j.getAsJsonObject().get("name").getAsString().equals(name)) {
                    id = j.getAsJsonObject().get("id").getAsString();
                    break;
                }
            }
            if(id == null) {
                System.out.println("Unknown category name.");
                return;
            }
            else {
                response = getResponse("/v1/browse/categories/" + id + "/playlists");

                if(response.statusCode() == 200) {
                    jo = JsonParser.parseString(response.body()).getAsJsonObject();
                    if(jo.has("error")) {
                        System.out.println("ERROR: " + jo.get("error"));
                    }
                    else {
                        pages.clear();
                        for (JsonElement j : jo.getAsJsonObject("playlists").getAsJsonArray("items")) {
                            if (j.isJsonObject()) {
                                pages.addOutput(j.getAsJsonObject().get("name").getAsString() + "\n" + j.getAsJsonObject().getAsJsonObject("external_urls").get("spotify").getAsString() + "\n");
                            }
                        }
                    }
                }
                else {
                    System.out.println("bad status code");
                    System.out.println(response.headers());
                    System.out.println(response.body());
                }

            }

        }
        catch (Exception e) {
            System.out.println("Exception searching for playlist!!!\n");
            e.printStackTrace();
        }

    }

    private static HttpResponse getResponse(String api) throws IOException, InterruptedException {

        System.out.println("\n\nRequested:  " + APIpath + api + "\n\n");
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Content-Type", "application/json", "Authorization", "Bearer " + accessToken)
                .uri(URI.create(APIpath + api))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static void getServerPath(String[] args) {

        boolean api = true;
        boolean pth = true;

        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-access") && i < args.length - 1) {
                //System.out.println("args: " + args[i + 1]);
                path = args[i + 1] + "/api/token";
                url = args[i + 1] + "/authorize?client_id=" + clientId + "&redirect_uri=" + serverPath + "&response_type=code";
                pth = false;
            }
            if(args[i].equals("-resource") && i < args.length - 1) {
                APIpath = args[i + 1];
                api = false;
            }
            if(args[i].equals("-page") && i < args.length - 1) {
                try {
                    size = Integer.parseInt(args[i + 1]);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(pth) {
            path = spotify + "/api/token";
            url = spotify + "/authorize?client_id=" + clientId + "&redirect_uri=" + serverPath + "&response_type=code";
        }
        if(api)
            APIpath = defaultAPI;
    }


    private static void authorize(HttpServer server) throws InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println(url);
        System.out.println("waiting for code...");
        while(code.equals("")) {
            Thread.sleep(100);
        }
        server.stop(0);
        System.out.println("code received");
        System.out.println("making http request for access_token...");
        try{
            getToken();
        }
        catch (Exception e) {
            System.out.println("\n\nSomething went wrong with getting the token\n\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void getToken() throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder()
                .headers("Content-Type", "application/x-www-form-urlencoded", "Authorization", "Basic " + Base64.getEncoder().encodeToString(clientIdSecret.getBytes()))
                .uri(URI.create(path))
                .POST(HttpRequest.BodyPublishers.ofString("client_id=" + clientId +
                        "&client_secret=" + clientSecret +
                        "&grant_type=authorization_code" +
                        "&code=" + code.split("=")[1] +
                        "&redirect_uri=" + serverPath))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(response.body(), Map.class);
        accessToken = map.get("access_token");
        System.out.println("response:");
        System.out.println(response.body());
    }

    private static HttpServer startServer(String port){

        try{
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(Integer.valueOf(port)), 0);
            server.createContext("/",
                    new HttpHandler() {
                        public void handle(HttpExchange exchange) throws IOException {
                            String response = exchange.getRequestURI().getQuery();
                            if(response != null && response.startsWith("code=")) {
                                code = response;
                                exchange.sendResponseHeaders(200, "Got the code. Return back to your program.".length());
                                exchange.getResponseBody().write("Got the code. Return back to your program.".getBytes());
                                exchange.getResponseBody().close();
                                //System.out.println("code received at controller: " + code);
                            }
                            else {
                                exchange.sendResponseHeaders(401, "Authorization code not found. Try again.".length());
                                exchange.getResponseBody().write("Authorization code not found. Try again.".getBytes());
                                exchange.getResponseBody().close();
                            }
                        }
                    }
            );
            server.start();
            return server;
        }
        catch (Exception e) {
            System.out.println("\n\n***Exception when starting the server***\n\n");
            e.printStackTrace();
            System.out.println("\n*******************************\n\n");
        }
        return null;
    }
}
