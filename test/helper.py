import socket
import sys
import os
import math
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

w = 160
h = 160
v = VectorDisplay(s.send,lowendian=False)
v.initialize(w,h)
v.rounded(True)
v.foreColor(0xFFFF0000)
v.fillCircleHelper(60,55,50,2,50)
v.foreColor(0xFF00FF00)
v.fillCircleHelper(80,55,50,1,50)
