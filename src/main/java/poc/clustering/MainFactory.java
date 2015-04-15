package poc.clustering;

/**
 * Created by remi on 13/04/2015.
 */
public class MainFactory {

    public static void main(String[] args) {
        AWSClusterRegistry clusterRegistry = AWSClusterRegistryFactory.getIntance().getCache(AWSClusterRegistryFactory.REDIS);
        clusterRegistry.connect("localhost", "6379", null, null);

        //clusterRegistry.flushAll();

        ClusterNoeud cn1 = new ClusterNoeud();
        cn1.setHostname("orange");
        cn1.setPort(2000);
        clusterRegistry.pushNoeud("cluster1" , cn1);

        ClusterNoeud cn2 = new ClusterNoeud();
        cn2.setHostname("bleu");
        cn2.setPort(2000);
        clusterRegistry.pushNoeud("cluster1" , cn2);

        ClusterNoeud cn3 = new ClusterNoeud();
        cn3.setHostname("rouge");
        cn3.setPort(2000);
        clusterRegistry.pushNoeud("cluster2" , cn3);

        try{
            Thread.sleep(2000);
        }catch (Exception ex){

        }

        ClusterNoeud cn4 = new ClusterNoeud();
        cn4.setHostname("orange");
        cn4.setPort(2000);
        clusterRegistry.pushNoeud("cluster1" , cn4);

        ClusterNoeud cn5 = new ClusterNoeud();
        cn5.setHostname("bleu");
        cn5.setPort(2000);
        clusterRegistry.removeNoeud("cluster1" , cn5);

        System.out.println(clusterRegistry.getClusters());
        System.out.println(clusterRegistry.getNoeuds("cluster1"));
        System.out.println(clusterRegistry.getNoeuds("cluster2"));

    }
}
