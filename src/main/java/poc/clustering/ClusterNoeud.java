package poc.clustering;

/**
 * Created by remi on 10/04/2015.
 */
public class ClusterNoeud {

    public static final String SLASH_TOKEN = "/";

    private String hostname;

    private Integer port;

    private Long lastUpdate;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEquals = false;
        ClusterNoeud cn = (ClusterNoeud)obj;
        if(cn.getHostname().equals(this.getHostname()) && cn.getPort().equals(this.getPort()))
            isEquals = true;
        return isEquals;
    }

    public String exportStrLine(){

        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append(hostname);
        strBuilder.append(SLASH_TOKEN);
        strBuilder.append(port);
        strBuilder.append(SLASH_TOKEN);
        strBuilder.append(lastUpdate);

        return strBuilder.toString();
    }

    public void importStrLine(String line) {
        String arg[] = line.split(SLASH_TOKEN);
        hostname = arg[0];
        port = Integer.parseInt(arg[1]);
        lastUpdate = Long.parseLong(arg[2]);
    }

    @Override
    public String toString() {
        return "ClusterNoeud{" +
                "hostname='" + hostname + '\'' +
                ", port=" + port +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
