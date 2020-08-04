#include <Servo.h>
#include <avr/interrupt.h>

#define HIT_TIME_LOW 15
#define HIT_TIME_HIGH 20

long last_millis0 = 0;
long last_millis2 = 0;
int period0 = 0;
int period2 = 0;

boolean front_hit = false;
boolean side_hit = false;

Servo pan;
Servo tilt;
uint8_t pan_degree = 45;
uint8_t tilt_degree = 45;

byte serial_response = 0x00;

void setup() {
  cli();  //Global interrupt disable

  //38kHz %50 duty cycle PWM Calibration
  TCCR2A = _BV(COM2B1) | _BV(WGM20);
  TCCR2B = _BV(WGM22) | _BV(CS20);
  OCR2A = 210;  //Frequency Adjustment
  OCR2B = 105;  //Duty Cycle

  //Pin Change Interrupts
  DDRD &= ~_BV(DDD4);   //PD4 as input (digital pin 4)
  PCMSK2 |= _BV(PCINT20); //Connect PD4 to pin change interrupt 2
  PCICR |= _BV(PCIE2);  //Enable pin change interrupt 2

  DDRB &= ~_BV(DDB0);   //PB0 as input (digital pin 8)
  PCMSK0 |= _BV(PCINT0);  //Connect PB0 to pin change interrupt 0
  PCICR |= _BV(PCIE0);  //Enable pin change interrupt 0

  sei();  //Global interrupt enable

  Serial.begin(115200);

  //servo init
  pan.attach(9);
  tilt.attach(10);
  pan.write(pan_degree);
  tilt.write(tilt_degree);

  //Disgard serial buffer values
  while (Serial.available() != 0) {
    Serial.read();
  }

  //sanity check (Reset posibility)
  Serial.println("YO");
}

void loop() {

  //taking servo movement from serial
  if (Serial.available() > 0) {
    serial_response = Serial.read();
  }

  //If it feels unresponsive or if it doesn't work
  if (serial_response && 0x01) {  //isolate 1st bit
    pan_degree++;
  }
  else if (serial_response && 0x03) {
    pan_degree--;
  }
  if (serial_response && 0x02) {  //isolate 2nd bit
    tilt_degree++;
  }
  else if (serial_response && 0x04) {
    tilt_degree--;
  }

  //update servos
  pan.write(pan_degree);
  tilt.write(pan_degree);

  //send hit data
  if(front_hit && side_hit) {
    Serial.println((uint8_t)(0b11000000));
  }
  else if (front_hit && !side_hit) {
    Serial.println((uint8_t)(0b10000000));
  }
  else if(!front_hit && side_hit) {
    Serial.println((uint8_t)(0b01000000));
  }
  //reset hit flags
  front_hit = false;
  side_hit = false;
}

/**
 * When pin state changes interrupt occur and find pulse or non pulse time
 * Possible improvement remove non pulse time but seems adequate for now. Doesn't trigger with NEC protochol and Toshiba TV remote
 */
ISR(PCINT2_vect) {
  period2 = (int)(millis() - last_millis2);
  front_hit = ((period2 >= HIT_TIME_LOW) && (period2 <= HIT_TIME_HIGH));
  last_millis2 = millis();
}

/**
 * Same thing but for different source
 */
ISR(PCINT0_vect) {
  period0 = (int)(millis() - last_millis0);
  side_hit = ((period0 >= HIT_TIME_LOW) && (period0 <= HIT_TIME_HIGH));
  last_millis0 = millis();
}
