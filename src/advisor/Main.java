package advisor;

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
import java.util.Scanner;

public class Main {

    private static final String port = "8000"; //when changing the port a new authoriazation has to be made at Spotify
    private static final String serverPath = "http://localhost:" + port;
    private final static String url = "https://accounts.spotify.com/authorize?client_id=e250b9f5fe2848f08f36f20b1274866a&redirect_uri=http://localhost:" + port + "&response_type=code";
    private final static String clientId = "e250b9f5fe2848f08f36f20b1274866a";
    private final static String clientSecret = "006e08a74dbd4d5caa1b5fdc8d247687";
    private final static String clientIdSecret = clientId + ":" + clientSecret;
    private static String code = "";
    private static String accessToken = "";

    public static void main(String[] args) throws InterruptedException, IOException {


        HttpServer server = startServer(port);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String i = scanner.next();
            if(i.equals("auth")) {

                System.out.println("use this link to request the access code:");
                System.out.println(url);
                System.out.println("waiting for code...");
                while(code.equals("")) {
                    Thread.sleep(100);
                }
                server.stop(0);
                System.out.println("code received");
                System.out.println("making http request for access_token...");
                System.out.println(code);
                HttpClient client = HttpClient.newBuilder().build();
                HttpRequest request = HttpRequest.newBuilder()
                        .headers("Content-Type", "application/x-www-form-urlencoded", "Authorization", "Basic " + Base64.getEncoder().encodeToString(clientIdSecret.getBytes()))
                        .uri(URI.create("https://accounts.spotify.com/api/token"))
                        .POST(HttpRequest.BodyPublishers.ofString("client_id=" + clientId +
                                                                    "&client_secret=" + clientSecret +
                                                                    "&grant_type=authorization_code" +
                                                                    "&code=" + code.split("=")[1] +
                                                                    "&redirect_uri=" + serverPath))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("RESPONSE body: " + response.body());
                System.out.println("RESPONSE headers: " + response.headers());
                System.out.println("RESPONSE status code: " + response.statusCode());

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
                                System.out.println("code received at controller: " + code);
                            }
                            else {
                                exchange.sendResponseHeaders(200, "Authorization code not found. Try again.".length());
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
            System.out.println("Stopping the app!");
            System.exit(1);
        }
        return null;
    }
}
