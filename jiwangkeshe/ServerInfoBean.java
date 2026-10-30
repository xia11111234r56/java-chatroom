package jiwangkeshe;

import java.io.Serializable;

public class ServerInfoBean implements Serializable {

   private static final long serialVersionUID = 4800399407766443229L;

   private String hostName;
   private String ip;
   private Integer port;
   public String getHostName() {
      return hostName;
   }
   public void setHostName(String hostName) {
      this.hostName = hostName;
   }
   public String getIp() {
      return ip;
   }
   public void setIp(String ip) {
      this.ip = ip;
   }
   public Integer getPort() {
      return port;
   }
   public void setPort(Integer port) {
      this.port = port;
   }

}
