from LED.LEDDriver import LEDDriver
import time

print("샘플 코드")
LED = LEDDriver()

print("LED 0 - MA1 & MA2에 연결되어 있음")
# LED.run(LED번호, 전류 방향, PWM[높을 수록 밝음])
LED.run(0, 'forward', 100) 
time.sleep(2)
LED.run(0, 'forward', 50)
time.sleep(2)
LED.run(0, 'forward', 20)
time.sleep(2)
LED.run(0, 'forward', 10)
time.sleep(2)
LED.stop(0)

print("LED 1 - MB1 & MB2에 연결되어 있음")
LED.run(1, 'forward', 100)
time.sleep(2)
LED.run(1, 'forward', 50)
time.sleep(2)
LED.run(1, 'forward', 20)
time.sleep(2)
LED.run(1, 'forward', 10)
time.sleep(2)
LED.stop(1) 