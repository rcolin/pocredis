package poc.clustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by remi on 14/04/2015.
 */
public class AdminConsole {

    private static AWSClusterRegistry clusterRegistry = null;

    public static void main(String[] args) throws IOException {

        //default clustering conf
        String clusterCacheServer = "localhost";
        String clusterCachePort = "6379";

        //dynamic clustering conf
        if(args.length == 2){
            clusterCacheServer = args[0];
            clusterCachePort = args[1];
        }

        //connect to clusterRegistry server
        clusterRegistry = AWSClusterRegistryFactory.getIntance().getCache(AWSClusterRegistryFactory.REDIS);
        clusterRegistry.connect(clusterCacheServer, clusterCachePort, null, null);

        //select choice
        String line = "";
        Scanner scanner = new Scanner(System.in);

        while(!line.equals("11")) {
            printWelcome();
            System.out.println("");
            System.out.println("[1]  List all cluster");
            System.out.println("[2]  List all noeuds from cluster");
            System.out.println("[3]  Show status of an instance");
            System.out.println("[4]  Disable an instance");
            System.out.println("[5]  Enable an instance");
            System.out.println("[6]  Disable all instances");
            System.out.println("[7]  Enable all instances");
            System.out.println("[8]  Disable instances from cluster");
            System.out.println("[9]  Disable instances from cluster");
            System.out.println("[10] Flush clustering cache");
            System.out.println("[11] Exit");
            System.out.println("\nInput your choice :");
            line = scanner.nextLine();
            Integer choice = Integer.parseInt(line);

            switch (choice){
                case 1 :
                    showAllCluster();
                    break;
                case 2 :
                    showAllNoeuds();
                    break;
                case 3 :
                    showInstanceStatus();
                    break;
                case 4 :
                    disableInstance();
                    break;
                case 5 :
                    enableInstance();
                    break;
                case 6 :
                    disableAllInstances();
                    break;
                case 7 :
                    enableAllInstances();
                    break;
                case 8 :
                    disableInstancesFromCluster();
                    break;
                case 9 :
                    enableInstancesFromCluster();
                    break;
                case 10 :
                    flushCache();
                    break;
                case 11 :
                    System.out.println("Exit !");
                    break;
                default :
                    System.out.println("Option doesn't exist");
                    break;

            }
            if(choice != 11) {
                System.out.println("\n\n\nPresse a key to continuous...");
                scanner.nextLine();
            }
        }
    }

    private static void disableInstancesFromCluster() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Input cluster name : ");
        String clusterName = scanner.nextLine();
        List<ClusterNoeud> clusterNoeuds = clusterRegistry.getNoeuds(clusterName);
        for(ClusterNoeud clusterNoeud : clusterNoeuds){
            changeStatus("stop", clusterNoeud.getHostname(), clusterNoeud.getPort().toString());
        }
    }

    private static void enableInstancesFromCluster() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Input cluster name : ");
        String clusterName = scanner.nextLine();
        List<ClusterNoeud> clusterNoeuds = clusterRegistry.getNoeuds(clusterName);
        for(ClusterNoeud clusterNoeud : clusterNoeuds){
            changeStatus("start", clusterNoeud.getHostname(), clusterNoeud.getPort().toString());
        }
    }

    private static void disableAllInstances(){
        actionOnAllInstances("stop");
    }

    private static void enableAllInstances(){
        actionOnAllInstances("start");
    }

    private static void actionOnAllInstances(String action){
        Set<String> clusterNameList = clusterRegistry.getClusters();
        for(String clusterName : clusterNameList){
            List<ClusterNoeud> clusterNoeuds = clusterRegistry.getNoeuds(clusterName);
            for(ClusterNoeud clusterNoeud : clusterNoeuds){
                changeStatus(action, clusterNoeud.getHostname(), clusterNoeud.getPort().toString());
            }
        }
    }

    private static void disableInstance() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Input server name(IP) : ");
        String server = scanner.nextLine();

        System.out.println("Input port : ");
        String port = scanner.nextLine();

        changeStatus("stop", server, port);

    }




    private static void enableInstance() {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Input server name(IP) : ");
        String server = scanner.nextLine();

        System.out.println("Input port : ");
        String port = scanner.nextLine();

        changeStatus("start", server, port);

    }

    private static void changeStatus(String action, String server, String port) {

        String urlString = "http://" + server + ":" + port + "/" + action;

        InputStream is = null;
        BufferedReader br = null;
        URLConnection conn = null;
        try {
            URL url = new URL(urlString);

            conn = url.openConnection();

            is = conn.getInputStream();

            br = new BufferedReader(new InputStreamReader(is));

            String status = br.readLine();
            System.out.printf("Action to %s on %s:%s with remote message : %s\n", action, server, port, status);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static void flushCache() {
        clusterRegistry.flushAll();
        System.out.println("All entries of cluster cache are flushed");
    }

    private static void printWelcome() {
        System.out.println("");
        System.out.println("");
        System.out.println("CONSOLE CLUSTERING HANDLER");
        System.out.println("");
        System.out.println("");

    }

    private static void showAllCluster(){
        Set<String> clusterNameList = clusterRegistry.getClusters();
        for(String clusterName : clusterNameList){
           List<ClusterNoeud> clusterNoeuds = clusterRegistry.getNoeuds(clusterName);
            System.out.printf("Cluster : %s / # noeuds : %d \n", clusterName, clusterNoeuds.size());
        }
    }

    private static void showAllNoeuds(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Input cluster name : ");
        String clusterName = scanner.nextLine();
        List<ClusterNoeud> clusterNoeuds = clusterRegistry.getNoeuds(clusterName);
        for(ClusterNoeud clusterNoeud : clusterNoeuds) {
            System.out.println(clusterNoeud);
        }
    }

    private static void showInstanceStatus(){
        Scanner scanner = new Scanner(System.in);

        System.out.println("Input server name(IP) : ");
        String server = scanner.nextLine();

        System.out.println("Input port : ");

        String port = scanner.nextLine();
        boolean isUpAndRunning = checkStatusWithHttpGET(server,port);

        if(isUpAndRunning){
            System.out.printf("Server %s %s is OK", server, port);
        }else
            System.out.printf("Server %s %s is KO", server, port);
    }


    private static boolean checkStatusWithHttpGET(String server, String port) {

        boolean isUpAndRunning = false;

        String urlString = "http://" + server + ":" + port + "/status";

        InputStream is = null;
        BufferedReader br = null;
        URLConnection conn = null;
        try {
            URL url = new URL(urlString);

            conn = url.openConnection();
            is = conn.getInputStream();

            br = new BufferedReader(new InputStreamReader(is));

            String status = br.readLine();

            if("Service up and running".equals(status)){
                isUpAndRunning = true;
            }else if("Service stopped ".equals(status)){
                isUpAndRunning = false;
            }else
                isUpAndRunning = false;

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
                br.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }
        return isUpAndRunning;
    }
}
