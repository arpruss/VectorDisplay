import socket
import sys
import os
from vectordisplay import VectorDisplay
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

def dec16(a,b):
    return (a<<8) | (b&0xFF);

def reader():    
    while 1:
        try:
            data = s.recv(8)
            print(" ".join("%02X" % b for b in data))
            c = chr(data[0])
            if c=='U' or c=='D' or c=='M':
                print ( chr(data[0])+":%d,%d" % (dec16(data[2],data[3]),dec16(data[4],data[5])) )
        except:
            pass
    
thread = Thread(target = reader, args  = ())
thread.start()

out = VectorDisplay(s.send,lowendian=False)

out.initialize()

sleep(1)
while 1:
    x1 = randint(0,239)
    y1 = randint(0,319)
    x2 = randint(0,239)
    y2 = randint(0,319)
    out.line(x1,y1,x2,y2)
    sleep(1)
    