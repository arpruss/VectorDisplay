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
    addr = '192.168.1.110'
    
port = 7788    

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((addr, port))
print("connected")

w = 420
h = 420
v = VectorDisplay(s.sendall,lowendian=False)
v.initialize(w,h)
v.bitmap(10,10,dimensions[0],dimensions[1],bw,foreColor=0x7f007F00,backColor=0x7f1f1f1f,depth=1)
v.bitmap(80,80,dimensions[0],dimensions[1],gray,depth=8)
v.bitmap(160,160,dimensions[0],dimensions[1],rgb888,depth=24)
v.bitmap(220,220,dimensions[0],dimensions[1],rgb8888,depth=32)
v.bitmap(300,300,dimensions[0],dimensions[1],rgb565,depth=16)
sleep(0.5) # needed on Windows
s.close()