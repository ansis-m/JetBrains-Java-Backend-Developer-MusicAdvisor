package advisor;

import com.fasterxml.jackson.databind.ObjectMapper;
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


    private static final String port = "8080"; //when changing the port a new authoriazation has to be made at Spotify
    private static String serverPath = "http://localhost:" + port;
    private final static String clientId = "e250b9f5fe2848f08f36f20b1274866a";
    private final static String clientSecret = "006e08a74dbd4d5caa1b5fdc8d247687";
    private final static String spotify = "https://accounts.spotify.com";
    private static String path;
    private static String url;

    private final static String clientIdSecret = clientId + ":" + clientSecret;
    private static String code = "";
    private static String accessToken = "";

    public static void main(String[] args) throws InterruptedException {


        getServerPath(args);
        Scanner scanner = new Scanner(System.in);
        HttpServer server = startServer(port);

        while (true) {
            String i = scanner.next();
            if(i.equals("auth")) {
                authorize(server);
                System.out.println("---SUCCESS---");
                break;
            }
            else if(i.equals("exit")) {
                scanner.close();
                System.exit(0);
            }
            else
                System.out.println("Please, provide access for application.");
        }

        while(true) {
            String i = scanner.next();
            switch (i) {
                case ("exit"):
                    scanner.close();
                    System.out.println("---GOODBYE!---");
                    System.exit(0);
                case ("new"):
                    System.out.println("---NEW RELEASES---\n" +
                            "Mountains [Sia, Diplo, Labrinth]\n" +
                            "Runaway [Lil Peep]\n" +
                            "The Greatest Show [Panic! At The Disco]\n" +
                            "All Out Life [Slipknot]");
                    break;
                case ("featured"):
                    System.out.println("---FEATURED---\n" +
                            "Mellow Morning\n" +
                            "Wake Up and Smell the Coffee\n" +
                            "Monday Motivation\n" +
                            "Songs to Sing in the Shower");
                    break;
                case ("categories"):
                    System.out.println("---CATEGORIES---\n" +
                            "Top Lists\n" +
                            "Pop\n" +
                            "Mood\n" +
                            "Latin");
                    break;
                case ("playlists Mood"):
                    System.out.println("---MOOD PLAYLISTS---\n" +
                            "Walk Like A Badass  \n" +
                            "Rage Beats  \n" +
                            "Arab Mood Booster  \n" +
                            "Sunday Stroll");
                    break;
            }
        }


    }

    private static void getServerPath(String[] args) {

        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-access") && i < args.length - 1) {
                //System.out.println("args: " + args[i + 1]);
                path = args[i + 1] + "/api/token";
                url = args[i + 1] + "/authorize?client_id=" + clientId + "&redirect_uri=" + serverPath + "&response_type=code";

                return;
            }
        }
        path = spotify + "/api/token";
        url = spotify + "/authorize?client_id=" + clientId + "&redirect_uri=" + serverPath + "&response_type=code";
    }


    private static void authorize(HttpServer server) throws InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.println(url);
        System.out.println("waiting for code...");
        while(code.equals("")) {
            Thread.sleep(100);
        }
        server.stop(1);
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
        //ObjectMapper mapper = new ObjectMapper();
        //Map<String, String> map = mapper.readValue(response.body(), Map.class);
        //accessToken = map.get("access_token");
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
