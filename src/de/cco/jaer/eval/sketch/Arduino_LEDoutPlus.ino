#define BAUDRATE 115200

const int LASER = 13;
const int SYNC  = 12;

const int SYNC_ON   = (int)'1'; // jAER logging started
const int SYNC_OFF  = (int)'2'; // jAER logging stopped
const int LASER_ON  = (int)'A'; // swtich on optogenetic laser
const int LASER_OFF = (int)'B'; // switch off optogenetic laser
const int FLUSH     = (int)'F'; // flushes the output
int recv = 0; // byte received on the serial port

const char BSYNC  = 0x01;
const char BLASER = 0x02;
int offset = 0;

char out = (char)0x00;
char state = (char)0x00;

void setup() {
  // initialize onboard LED (led) and serial port
  pinMode(LASER, OUTPUT);
  pinMode(SYNC, OUTPUT);
  Serial.begin(BAUDRATE);
  offset = 0;
  Serial.println("--Arduino ready.");
}

void encode(){
  out = out << 2;
  out |= state;
  offset++;
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
        digitalWrite(LASER, HIGH);
        state |= BLASER;
        encode();
        break;
      case LASER_OFF:
        digitalWrite(LASER, LOW);
        state &= ~BLASER;
        encode();
        break;
      case SYNC_ON:
        digitalWrite(SYNC, HIGH);
        state |= BSYNC;
        encode();
        break;
      case SYNC_OFF:
        digitalWrite(SYNC, LOW);
        state &= ~BSYNC;
        encode();
        break;
      case FLUSH:
        out = out << (4 - offset);
        offset = 4;
        break;
    }
    

    if( offset == 4 ){
      // confirm values received in serial monitor window
      Serial.print(out);
      offset = 0;
    }
  }
}
