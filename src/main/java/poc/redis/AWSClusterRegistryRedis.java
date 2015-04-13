package poc.redis;

import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by remi on 01/04/2015.
 */
public class AWSClusterRegistryRedis implements AWSClusterRegistry{

    public static final String COMMA_TOKEN = ",";

    Jedis jedis = null;

    @Override
    public void connect(String host, String port, String login, String password){
        jedis = new Jedis(host);
        //TODO add redis port
    }

    @Override
    public void flushAll() {
        jedis.flushAll();
    }

    @Override
    public void pushNoeud(String cluster, ClusterNoeud noeud) {

        List<ClusterNoeud> noeuds = getNoeuds(cluster);
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

        //record all cluster in remote redis server
        recordClusterNoeuds(cluster, noeuds);
    }


    @Override
    public boolean removeNoeud(String cluster, ClusterNoeud noeud){

        List<ClusterNoeud> noeuds = getNoeuds(cluster);

        boolean isDeleted = false;

        for (ClusterNoeud clusterNoeud : noeuds){
            if(noeud.equals(clusterNoeud)){
                noeuds.remove(clusterNoeud);
                isDeleted = true;
                break;
            }
        }

        if(isDeleted) {
            //record all cluster in remote redis server
            recordClusterNoeuds(cluster, noeuds);
        }

        return isDeleted;
    }

    @Override
    public List<ClusterNoeud> getNoeuds(String cluster) {

        List<ClusterNoeud> clusterNoeuds = new ArrayList<ClusterNoeud>();

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
        Set<String> clusters =jedis.keys("*");
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
        //record on redis server
        jedis.set(cluster, strBuilder.toString());
    }

    private static boolean isEmpty(String str){
        boolean result = true;
        if(str != null && !str.equals(""))
            result = false;
        return result;
    }


}
