package poc.redis;

import java.util.List;
import java.util.Set;

/**
 * Created by remi on 01/04/2015.
 */
public interface AWSClusterRegistry {

        public void connect(String host, String port, String login, String password);

        public void pushNoeud(String cluster, ClusterNoeud clusterNoeud);

        public boolean removeNoeud(String cluster, ClusterNoeud clusterNoeud);

        public List<ClusterNoeud> getNoeuds(String cluster);

        public Set<String> getClusters();

        public void flushAll();
}
