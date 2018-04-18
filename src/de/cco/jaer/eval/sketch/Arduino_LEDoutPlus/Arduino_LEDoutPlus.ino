#define BAUDRATE 230400

const int SYNC_ON   = (int)'1'; // jAER logging started
const int SYNC_OFF  = (int)'2'; // jAER logging stopped
const int EVENT_ON  = (int)'A'; // swtich on event
const int EVENT_OFF = (int)'B'; // switch off event
const int FLUSH     = (int)'F'; // clears the buffer and flushes the output
const int CLEAR     = (int)'O'; // clears the buffer
int recv = 0; // byte received on the serial port

const char BSYNC  = 0x01;
const char BEVENT = 0x02;
int offset = 0;

char out = (char)0x00;
char state = (char)0x00;

#if defined (__AVR_ATmega328P__)
// UNO-like
const    int      EVENT = 13;
const    int      SYNC  = 12;
volatile uint8_t *syncport  = PORTB;
volatile uint8_t *eventport = PORTB;
const    uint8_t  MSYNC     = 0x01 << 4; // pin mask for pin 12
const    uint8_t  MEVENT    = 0x01 << 5; // pin mask for pin 13

#elif defined (__AVR_ATmega32U4__)
// Leonardo-like
const    int      EVENT     = 3;
const    int      SYNC      = 4;
volatile uint8_t *syncport  = PORTD;
volatile uint8_t *eventport = PORTD;
const    uint8_t  MSYNC     = 0x01;      // pin 4
const    uint8_t  MEVENT    = 0x10;      // pin 3

#else
// unknown
#error unsupported chip
#endif

void setup() {
  // initialize onboard LED (led) and serial port
  pinMode(EVENT, OUTPUT);
  pinMode(SYNC, OUTPUT);

  Serial.begin(BAUDRATE);
#if defined (__AVR_ATmega328P__)
  // UNO-like
  // let the host know that it is ready (after ~3 sec)
  Serial.println("--Arduino ready.");
#elif defined (__AVR_ATmega32U4__)
  // Leonardo-like
  // just wait until the boot process is done
  // (serial port stays open no matter what is going on with the host)
  while(!Serial);
#else

#error unsupported chip
#endif
  offset = 0;
}

void encode(){
  out = out << 2;
  out |= state;
  offset++;
}

void loop()
{

  if( (recv = Serial.read()) >= 0 ){

    // if '1' (decimal 49) is received, turn SNYC on
    // if '2' (decimal 50) is received, turn SYNC off
    // if 'A' (decimal 101) is received, turn EVENT on
    // if 'B', or any other non registered character is received, turn EVENT off
    switch (recv)
    {
      case EVENT_ON:
        *eventport |= MEVENT;
        state |= BEVENT;
        encode();
        break;
      case EVENT_OFF:
        *eventport &= ~MEVENT;
        state &= ~BEVENT;
        encode();
        break;
      case SYNC_ON:
        *syncport |= MSYNC;
        state |= BSYNC;
        encode();
        break;
      case SYNC_OFF:
        *syncport &= ~MSYNC;
        state &= ~BSYNC;
        encode();
        break;
      case FLUSH:
        if( offset == 0 ){
          return;
        } else {
          for(;offset<4;offset++){
            out = out << 2;
            out &= (char)0xFC;
          }
        }
        offset = 4;
        break;
      case CLEAR:
        *eventport &= ~MEVENT;
        *syncport &= ~MSYNC;
        state  = 0x00;
        offset = 0;
        break;
    }


    if( offset == 4 ){
      // confirm values received in serial monitor window
      Serial.print(out);
      offset = 0;
    }
  }
}
