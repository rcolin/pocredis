package poc.redis;

import sun.misc.IOUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by remi on 03/04/2015.
 */
public class POCGetHttp {

    public static void main(String[] args) throws Exception{
        String urlString = "http://localhost:8080/test?toto=titi&toti=lolo";
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String read = br.readLine();

        while(read != null) {
            System.out.println(read);
            read =br.readLine();
        }
    }
}
