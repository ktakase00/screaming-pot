float val;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  long derayPeriod = 1000UL * 60;
  delay(derayPeriod);
  val = analogRead(0);
  Serial.println(val);
}
