int delayTime = 1000;

// Run once
void setup() {
  pinMode(13, OUTPUT);  // Initialize digital pin 13 as an output
  Serial.begin(9600); // Inicia la comunicacion serial a 9600 baudios (tasa de bits por segundo)
  Serial.println("Started!"); // Serial.print("Starting...\n");
}

// Run repeatedly
void loop() {
  // digitalWrite(13, HIGH); // turn the LED on (HIGH is the voltage level)
  delay(delayTime);  // wait for a second
  Serial.println("Temperatura aqui"); // Arduino guarda en memoria el mensaje
  // digitalWrite(13, LOW);  // turn the LED off by making the voltage LOW
  // delay(delayTime);  // wait for a second
}
