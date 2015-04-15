package poc.clustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Created by remi on 15/04/2015.
 */
public class SimuHttpServer {

    public static final int CALL_HTTP_SLEEP = 5000;
    public static final int HEARTBEAT_SLEEP = 3000;
    private static final int REFRESHREPO_SLEEP = 5000;

    private static volatile boolean isEnable = true;

    private static String cluster;

    private static String port = "8080";

    private static AWSClusterRegistry awsClusterRegistry = AWSClusterRegistryFactory.getIntance().getCache(AWSClusterRegistryFactory.REDIS);

    private static ConcurrentHashMap<String, CopyOnWriteArrayList<ClusterNoeud>> clusterRepo= new ConcurrentHashMap<String, CopyOnWriteArrayList<ClusterNoeud>>();

    public static void main(String[] args) {

        String hostCache = "localhost";

        //Param port and server cache
        if(args.length != 0) {
            port = args[0];
            hostCache = args[1];
        }

        //connect to server cache
        awsClusterRegistry.connect(hostCache, null, null, null);

        //get the cluster for this process
        cluster = getRandomAcessCluster();

        //launch services
        launchHttpServer();
        launchHeartbeatThread();
        launchRefreshRepoThread();
        launchCallHttpThread();

    }

    private static void launchCallHttpThread() {



        new Thread() {

            @Override
            public void run() {
                while (true) {
                    if (isEnable) {
                        try {
                            final ClusterNoeud clusterNoeud = getRandomClusterNode();
                            if(clusterNoeud != null) {
                                String urlString = "http://" + clusterNoeud.getHostname() + ":" + clusterNoeud.getPort() + "/status";

                                InputStream is = null;
                                BufferedReader br = null;
                                URLConnection conn = null;

                                URL url = new URL(urlString);

                                conn = url.openConnection();
                                is = conn.getInputStream();

                                br = new BufferedReader(new InputStreamReader(is));

                                String status = br.readLine();

                                if ("Service up and running".equals(status) || "Service stopped ".equals(status)) {
                                    System.out.printf("Call HTTP on %s %s is OK\n", clusterNoeud.getHostname(), clusterNoeud.getPort());
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //each 5 seconds call http
                    try {
                        Thread.sleep(CALL_HTTP_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }


    private static void launchRefreshRepoThread() {
        new Thread(){
            @Override
            public void run() {
                while (true) {
                    if(isEnable) {
                        clusterRepo = awsClusterRegistry.getFullClusters();
                        System.out.println("Refresh cluster repo done : \n" + clusterRepo);
                    }
                    try {
                        Thread.sleep(REFRESHREPO_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                }
            }.start();
    }

    private static void launchHeartbeatThread() {

        new Thread(){
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(HEARTBEAT_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(isEnable)
                    {
                        ClusterNoeud cn = new ClusterNoeud();
                        cn.setPort(Integer.parseInt(port));
                        cn.setHostname(getLocalServerName());
                        awsClusterRegistry.pushNoeud(cluster, cn);
                        System.out.printf("Heartbeat get on : %s %s\n", cluster, cn.toString());
                    }
                }
            }
        }.start();
    }


    private static String getRandomAcessCluster(){
        int i = new Random().nextInt(3)+1;
        return "cluster"+i;
    }

    private static void launchHttpServer(){
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);

            server.createContext("/status", new StatusHandler());
            server.createContext("/stop", new StopHandler());
            server.createContext("/start", new StartHandler());

            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    static class StatusHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "Service up and running";
            if(isEnable == false)
                response = "Service stopped";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println(response);
        }
    }

    static class StopHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            isEnable = false;
            String response = "Service stopped";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println(response);
        }
    }

    static class StartHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            isEnable = true;
            String response = "Service up and running";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println(response);
        }
    }

    private static  String getLocalServerName(){
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostname;
    }

    private static ClusterNoeud getRandomClusterNode(){
        ClusterNoeud result = null;

        if(clusterRepo.keySet() != null && clusterRepo.keySet().size() > 0) {
            int clusterIndex =  new Random().nextInt(clusterRepo.keySet().size());
            Object[] keysArray = clusterRepo.keySet().toArray();

            String clusterName = (String)keysArray[clusterIndex];

            if(clusterRepo.get(clusterName).size() > 0) {
                int clusterNoeudIndex = new Random().nextInt(clusterRepo.get(clusterName).size());
                result = clusterRepo.get(clusterName).get(clusterNoeudIndex);
            }
        }

        return result;
    }
}
