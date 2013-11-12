#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>

#define PORT 5555

YunServer server(PORT);

void setup() {
  Serial.begin(115200);
  Bridge.begin();
  
  server.noListenOnLocalhost();
  server.begin();
}

void loop() {
  YunClient client = server.accept();

  if(client.connected()){
    Serial.println("CLIENT CONNECTED!");

    String string = "";

    while(client.connected()){
      if(client.available()){
        String query = client.readString();
        query.trim();
        
        Serial.println(query);
      }
    }
    client.stop();

  } 
  else {
    Serial.println("no client connected, retrying");
  }

  delay(1000);

}
