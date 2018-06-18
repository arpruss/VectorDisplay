import socket
import sys
import os
import math
from vectordisplay import VectorDisplay
from random import randint
from time import sleep
from bitmap_data import *

if len(sys.argv)>1:
    addr = sys.argv[1]
else:
    addr = '192.168.1.143'
    
port = 7788    

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((addr, port))
print("connected")

w = 100
h = 100
v = VectorDisplay(s.sendall,lowendian=False)
v.initialize(w,h)
v.text(0,0,"Hello, world!")
v.text(0,8,"Hello, world!")
sleep(0.5) # needed on Windows
s.close()
