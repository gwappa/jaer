
const int LED = 13; // LED connected to digital pin 13
const int SYNC_OFF = 49; // jAER logging stopped
const int SYNC_ON = 50; // jAER logging started
const int LASER_ON = 101; // swtich on optogenetic laser
const int LASER_OFF = 102; // switch off optogenetic laser
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

    // if '0' (decimal 48) is received, turn SNYC on
    // if '1' (decimal 49) is received, turn SYNC off
    // if 'A' (decimal 101) is received, turn LED on
    // if 'B', or any other non registered character is received, turn LED off
    switch (recv) 
    {
      case LASER_ON:
        digitalWrite(LED, HIGH);
        break;
      case LASER_OFF: 
        digitalWrite(LED, LOW);
        break;
      case SYNC_ON:
        break;
      case SYNC_OFF:
        break;
      default:
        digitalWrite(LED, LOW);
        break;
    }

    // confirm values received in serial monitor window
    Serial.print("--Arduino received: ");
    Serial.println(recv);
  }
}
