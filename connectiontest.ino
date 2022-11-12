#include <SPI.h>
#include <WiFi.h>
#include <WiFiUdp.h>
#include <Ethernet.h>
#include <EthernetUdp.h>

const char* networkName = "don't connect to this";
const char* networkPassword = "wewewert";
const int pin15 = 15;
const int pin2 = 2;
const int pin4 = 4;
const int pin5 = 5;

WiFiUDP Udp;
IPAddress ipAddress;

unsigned int localPort = 2567;
bool connected = false;

char packetBuffer[255];

//MAC Address: C8:F0:9E:A3:BA:F8
void setup()
{
  Serial.begin(115200);
  connectToWifi(networkName, networkPassword);
  pinMode(pin15, OUTPUT);
  pinMode(pin2, OUTPUT);
  pinMode(pin4, OUTPUT);
  pinMode(pin5, OUTPUT);

  digitalWrite(pin15,LOW);
  digitalWrite(pin2,LOW);
  digitalWrite(pin4,LOW);
  digitalWrite(pin5,LOW);
}

void loop()
{
  if(connected)
  {
    receivePacket();
  }
}

void connectToWifi(const char *ssid, const char *password)
{
    Serial.println("Scanning...");
    WiFi.mode(WIFI_STA);
    WiFi.begin(ssid, password);
    
    while (WiFi.status() != WL_CONNECTED) 
    {
          delay(500);
          Serial.println("Connecting to WiFi..");
    }

    connected = true;
    ipAddress = WiFi.localIP();
    //ipAddress = WiFi.gatewayIP();
    Serial.println("Connected to Wifi Network with IP " + IpAddress2String(ipAddress));
    
    Udp.begin(localPort);
};

void receivePacket()
{
    int packetSize = Udp.parsePacket();
    
    if(packetSize)
    {
      int len = Udp.read(packetBuffer, 255);

      if(len > 0)
      {
        packetBuffer[len] = 0;
      }

      Serial.println("Received Packet:");
      Serial.println(packetBuffer);

      transmitData();
    }
}

void transmitData()
{
  if(packetBuffer[0] == 0x31)
  {
    Serial.println("AAAAAAA");
    digitalWrite(pin15, HIGH); //mode (brown)
  }
  else digitalWrite(pin15, LOW);

  if(packetBuffer[1] == 0x31)
  {
    Serial.println("BBBBBBBB");
    digitalWrite(pin2, HIGH); //fan (yellow)
  }
  else digitalWrite(pin2, LOW);

  if(packetBuffer[2] == 0x31)
  {
    Serial.println("CCCCCCC");
     digitalWrite(pin4, HIGH); //light (green)
  }
  else digitalWrite(pin4, LOW);

  if(packetBuffer[3] == 0x31)
  {
    Serial.println("DDDDDD");
    digitalWrite(pin5, HIGH); //buzzer (blue)
  }
  else digitalWrite(pin5, LOW);

  delay(1000);

  digitalWrite(pin15,LOW);
  digitalWrite(pin2,LOW);
  digitalWrite(pin4,LOW);
  digitalWrite(pin5,LOW);

}

String IpAddress2String(const IPAddress& ipAddress)
{
    return String(ipAddress[0]) + String(".") +
           String(ipAddress[1]) + String(".") +
           String(ipAddress[2]) + String(".") +
           String(ipAddress[3]);
}
