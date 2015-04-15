package poc.clustering;

/**
 * Created by remi on 01/04/2015.
 */
public class AWSClusterRegistryFactory {

    public static final String REDIS = "REDIS";

    public static final String MEMCACHED = "MEMCACHED";

    private AWSClusterRegistryFactory(){}

    private static AWSClusterRegistryFactory instance = new AWSClusterRegistryFactory();

    public static AWSClusterRegistryFactory getIntance(){
        return instance;
    }

    public AWSClusterRegistry getCache(final String cacheType){
        AWSClusterRegistry result = null;
        if(AWSClusterRegistryFactory.REDIS.equals(cacheType)){
            result = new AWSClusterRegistryRedis();
        }else{
            throw new RuntimeException("Pattern cache type doesn't exist in system : " + cacheType);
        }
        return result;
    }

}
