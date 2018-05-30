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

w = 3
h = 4
v = VectorDisplay(s.send,lowendian=False)
v.initialize()
v.coordinates(w,h)
v.rounded(True)
v.foreColor(0xFFFF0000)
v.rectangle(0,0,w-1,h-1)
v.foreColor(0x3F00FF00)
v.fillRectangle(0,0,w-1,h-1)
v.foreColor(0x7F0000FF)
v.fillRectangle(0,0,1,1)
v.foreColor(0x7f00007F)
v.fillRectangle(1,0,2,1)