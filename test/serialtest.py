import serial
from random import randint
from time import sleep
import sys

if len(sys.argv)>1:
    addr = sys.argv[1]
else:
    addr = 'COM29'
    
s = serial.Serial(addr,baudrate=115200)
print("connected")

def sendCommand(c, args):
    command = [ ord(c), ord(c)^0xff ] + args + [ 0xff^sum(bytearray(args)) ]
    s.write(bytearray(c&0xFF for c in command))
    s.flush()

ack = False    
    
while not ack:
    print("Init")
    sendCommand('H', [0x12, 0x34, 0, 0]) # big endian
    while s.inWaiting()>0:
        r = s.read()
        if b'A' == r:
            ack = True
            break
    if not ack:
        sleep(1)
print("Ready")
while 1:
    x1 = randint(0,239)
    y1 = randint(0,319)
    x2 = randint(0,239)
    y2 = randint(0,319)
    sendCommand('L', [x1>>8,x1&0xFF, y1>>8,y1&0xFF, x2>>8,x2&0xff, y2>>8,y2&0xFF ])
    sleep(0.1)
    