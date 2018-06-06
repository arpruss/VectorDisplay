import socket
import sys
import os
from vectordisplay import VectorDisplay
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

v = VectorDisplay(s.send,lowendian=True)
v.initialize()
v.coordinates(200,200);
v.foreColor(0xFFFF0000)
v.roundedRectangle(10,10,100,100,30,fill=False)
v.foreColor(0x3F00FF00)
v.roundedRectangle(80,80,180,180,30,fill=True)
