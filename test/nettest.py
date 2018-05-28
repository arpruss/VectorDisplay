import socket
import sys
import os
from threading import Thread
from random import randint
from time import sleep

if len(sys.argv)>1:
    addr = sys.argv[1]
else:
    addr = '192.168.1.110'
    
port = 7788    

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((addr, port))
print("connected")

def sendCommand(c, args):
    command = [ ord(c), ord(c)^0xff ] + args + [ 0xff^sum(bytearray(args)) ]
    print(command)
    s.send(bytearray(c&0xFF for c in command))

def reader():    
    while 1:
        try:
            data = s.recv(8)
            print(" ".join("%02X" % b for b in data))
        except:
            pass
    
thread = Thread(target = reader, args  = ())
thread.start()

sendCommand('H', [0x12, 0x34, 0, 0]) # big endian
sleep(1)
while 1:
    x1 = randint(0,239)
    y1 = randint(0,319)
    x2 = randint(0,239)
    y2 = randint(0,319)
    sendCommand('L', [x1>>8,x1&0xFF, y1>>8,y1&0xFF, x2>>8,x2&0xff, y2>>8,y2&0xFF ])
    sleep(1)