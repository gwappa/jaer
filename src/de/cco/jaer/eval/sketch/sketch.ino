
const int YELLOW = 13;
const int RED = 12;
const int GREEN = 11;
const int LED = 13; // LED connected to digital pin 13
const int SYNC_OFF = 49; // jAER logging stopped
const int SYNC_ON = 50; // jAER logging started
const int LASER_ON = 65; // swtich on optogenetic laser
const int LASER_OFF = 66; // switch off optogenetic laser
int recv = 0; // byte received on the serial port

void setup() {
  // initialize onboard LED (led) and serial port
  pinMode(YELLOW, OUTPUT);
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  Serial.begin(9600);
  Serial.println("--Arduino ready.");
}

void loop()
{
  // if serial port is available, read incoming bytes
  if (Serial.available() > 0) {
    recv = Serial.read();

    // if '1' (decimal 49) is received, turn SNYC on
    // if '2' (decimal 50) is received, turn SYNC off
    // if 'A' (decimal 101) is received, turn LED on
    // if 'B', or any other non registered character is received, turn LED off
    switch (recv) 
    {
      case LASER_ON:
        digitalWrite(GREEN, HIGH);
        digitalWrite(RED, LOW);
        digitalWrite(YELLOW, LOW);
        break;
      case LASER_OFF: 
        digitalWrite(GREEN, LOW);
        digitalWrite(RED, HIGH);
        digitalWrite(YELLOW, LOW);
        break;
      case SYNC_ON:
        digitalWrite(GREEN, LOW);
        digitalWrite(RED, LOW);
        digitalWrite(YELLOW, HIGH);
        break;
      case SYNC_OFF:
        digitalWrite(GREEN, LOW);
        digitalWrite(RED, LOW);
        digitalWrite(YELLOW, LOW);
        break;
    }

    // confirm values received in serial monitor window
    Serial.print("--Arduino received: ");
    Serial.println(recv);
  }
}
