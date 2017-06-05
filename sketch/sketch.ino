
    int led = 13; // LED connected to digital pin 13
    int recv = 0; // byte received on the serial port

    void setup() {
      // initialize onboard LED (led) and serial port
      pinMode(led, OUTPUT);
      Serial.begin(9600);
    }

    void loop()
    {
      // if serial port is available, read incoming bytes
      if (Serial.available() > 0) {
        recv = Serial.read();

        // if 'x' (decimal 120) is received, turn LED on
        // anything other than 120 is received, turn LED off
        if (recv == 120){
          digitalWrite(led, HIGH);
        } else {
          digitalWrite(led, LOW);
        }
        
        // confirm values received in serial monitor window
        Serial.print("--Arduino received: ");
        Serial.println(recv);
      }
    }
