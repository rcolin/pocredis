package poc.clustering;

import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by remi on 01/04/2015.
 */
public class AWSClusterRegistryRedis implements AWSClusterRegistry{

    public static final String COMMA_TOKEN = ",";

    Jedis jedis = null;

    @Override
    public void connect(String host, String port, String login, String password){
        jedis = new Jedis(host);
        //TODO add clustering port
    }

    @Override
    public void flushAll() {
        jedis.flushAll();
    }

    @Override
    public void pushNoeud(String cluster, ClusterNoeud noeud) {

        CopyOnWriteArrayList<ClusterNoeud> noeuds = getNoeuds(cluster);
        boolean isUpdated = false;

        //check if noeud already exists
        for (ClusterNoeud clusterNoeud : noeuds){
            if(noeud.equals(clusterNoeud)){
                clusterNoeud.setLastUpdate(Instant.now().getEpochSecond());
                isUpdated = true;
                break;
            }
        }

        //noeud doesn't exist
        if(!isUpdated){
            noeud.setLastUpdate(Instant.now().getEpochSecond());
            noeuds.add(noeud);
        }

        //record all cluster in remote clustering server
        recordClusterNoeuds(cluster, noeuds);
    }


    @Override
    public boolean removeNoeud(String cluster, ClusterNoeud noeud){

        CopyOnWriteArrayList<ClusterNoeud> noeuds = getNoeuds(cluster);

        boolean isDeleted = false;

        for (ClusterNoeud clusterNoeud : noeuds){
            if(noeud.equals(clusterNoeud)){
                noeuds.remove(clusterNoeud);
                isDeleted = true;
                break;
            }
        }

        if(isDeleted) {
            //record all cluster in remote clustering server
            recordClusterNoeuds(cluster, noeuds);
        }

        return isDeleted;
    }

    @Override
    public CopyOnWriteArrayList<ClusterNoeud> getNoeuds(String cluster) {

        CopyOnWriteArrayList<ClusterNoeud> clusterNoeuds = new CopyOnWriteArrayList<ClusterNoeud>();

        String strCluster = jedis.get(cluster);
        if(!isEmpty(strCluster)) {
            String[] clusterArray = strCluster.split(COMMA_TOKEN);
            if(clusterArray.length >0) {
                for (String strClusterNoeud : clusterArray) {
                    ClusterNoeud cn = new ClusterNoeud();
                    cn.importStrLine(strClusterNoeud);
                    clusterNoeuds.add(cn);
                }
            }
        }
        return clusterNoeuds;
    }

    @Override
    public Set<String> getClusters(){
        Set<String> clusters = jedis.keys("*");
        return clusters;
    }

    private void recordClusterNoeuds(String cluster, List<ClusterNoeud> noeuds){

        //build cluster noeuds string
        StringBuilder strBuilder = new StringBuilder();
        for (ClusterNoeud clusterNoeud : noeuds){

            if(strBuilder.length()!=0) {
                strBuilder.append(COMMA_TOKEN);
            }
            strBuilder.append(clusterNoeud.exportStrLine());
        }
        //record on clustering server
        jedis.set(cluster, strBuilder.toString());
    }

    @Override
    public ConcurrentHashMap<String, CopyOnWriteArrayList<ClusterNoeud>> getFullClusters() {
        ConcurrentHashMap<String, CopyOnWriteArrayList<ClusterNoeud>> directory = new ConcurrentHashMap<String, CopyOnWriteArrayList<ClusterNoeud>>();
        Set<String> clusterSet = getClusters();
        if(clusterSet!=null & clusterSet.size() > 0) {
            for (String cluster : clusterSet) {
                directory.put(cluster, getNoeuds(cluster));
            }
        }
        return directory;
    }

    private static boolean isEmpty(String str){
        boolean result = true;
        if(str != null && !str.equals(""))
            result = false;
        return result;
    }


}
