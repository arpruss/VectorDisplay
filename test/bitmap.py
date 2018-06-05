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

w = 300
h = 400
v = VectorDisplay(s.send,lowendian=False)
v.initialize(w,h)
v.rounded(True)
v.poly([(100,100),(200,100),(150,50)], fill=True)
v.bwBitmap(10,10,dimensions[0],dimensions[1],bw,foreColor=0x7f007F00,backColor=0x7f1f1f1f)
v.grayBitmap(80,80,dimensions[0],dimensions[1],gray)
v.rgb888Bitmap(160,160,dimensions[0],dimensions[1],rgb888)
