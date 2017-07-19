
int LED = 13; // LED connected to digital pin 13
int SYNC_OFF = 48; // jAER logging stopped
int SYNC_ON = 49; // jAER logging started
int LASER_ON = 101; // swtich on optogenetic laser
int LASER_OFF = 102; // switch off optogenetic laser
int recv = 0; // byte received on the serial port

void setup() {
  // initialize onboard LED (led) and serial port
  pinMode(LED, OUTPUT);
  Serial.begin(9600);
  Serial.println("--Arduino ready.");
}

void loop()
{
  // if serial port is available, read incoming bytes
  if (Serial.available() > 0) {
    recv = Serial.read();

    // if 'A' (decimal 101) is received, turn LED on
    // if 'B', or any other non registered character is received, turn LED off
    if (recv == LASER_ON) 
    {
      digitalWrite(LED, HIGH);
    }
    else if (recv == LASER_OFF) 
    {
      digitalWrite(LED, LOW);
    }
    else if (recv == SYNC_ON)
    {
      // TODO
    }
    else if (recv == SYNC_OFF)
    {
      // TODO
    }
    else 
    {
      digitalWrite(LED, LOW);
    }

    // confirm values received in serial monitor window
    Serial.print("--Arduino received: ");
    Serial.println(recv);
  }
}
