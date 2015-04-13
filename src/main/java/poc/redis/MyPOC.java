package poc.redis;

import redis.clients.jedis.Jedis;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by remi on 01/04/2015.
 */
public class MyPOC {

    public static final String COMMA_TOKEN = ",";

    public static void main(String[] args) {

        //Connecting to Redis server on localhost
        Jedis jedis = new Jedis("localhost");
        System.out.println("Connection to server sucessfully");
        //check whether server is running or not
        System.out.println("Server is running: "+jedis.ping());

        jedis.del("cluster A");

        addValue(jedis, "cluster A", "server1");
        addValue(jedis, "cluster A", "server2");
        addValue(jedis, "cluster A", "server3");
        addValue(jedis, "cluster A", "server2");
        addValue(jedis, "cluster A", "server1");
        addValue(jedis, "cluster A", "server3");
        addValue(jedis, "cluster A", "server4");

        addValue(jedis, "cluster B", "server11");
        addValue(jedis, "cluster B", "server12");

        printAllKeysAndValues(jedis);

    }

    private static void printAllKeysAndValues(Jedis jedisClient){
        Set<String> names=jedisClient.keys("*");

        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String s = it.next();
            System.out.println("cluster " + s + "  : " + jedisClient.get(s));
        }
    }

    private static void addValue(Jedis jedisClient, String key, String strToAppend){
        if(!isEmpty(key)){
            String values = jedisClient.get(key);
            values = appendValueInArray(values, strToAppend);
            jedisClient.set(key, values);
        }
    }

    private static String appendValueInArray(String values, String newValue){

        if(!isEmpty(values)){
            String[] valuesArray = values.split(COMMA_TOKEN);
            for(String value : valuesArray){
                if(value.equals(newValue))
                    return values;
            }
            values += COMMA_TOKEN + newValue;
        }else{
            values = newValue;
        }

        return values;
    }

    private static boolean isEmpty(String str){
        boolean result = true;
        if(str != null && !str.equals(""))
            result = false;
        return result;
    }
}
