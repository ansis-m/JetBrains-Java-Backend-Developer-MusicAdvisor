package advisor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class Main {

    private static final String port = "8000";
    private final static String url = "https://accounts.spotify.com/authorize?client_id=e250b9f5fe2848f08f36f20b1274866a&redirect_uri=http://localhost:" + port + "&response_type=code";
    private static String input = "";


    public static void main(String[] args) throws InterruptedException {


        HttpServer server = startServer(port);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String i = scanner.next();
            if(i.equals("auth")) {

                System.out.println("use this link to request the access code:");
                System.out.println(url);
                System.out.println("waiting for code...");
                while(input.equals("")) {
                    Thread.sleep(100);
                }
                server.stop(0);
                System.out.println("code received");
                System.out.println("making http request for access_token...");
                System.out.println(input);

                System.out.println("---SUCCESS---");
                break;
            }
            else if(i.equals("exit")) {
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
                            exchange.sendResponseHeaders(200, "OK".length());
                            exchange.getResponseBody().write("OK".getBytes());
                            exchange.getResponseBody().close();
                            String response = exchange.getRequestURI().getQuery();
                            if(response != null)
                                input = response;
                            System.out.println("input received at controller: " + input);
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
