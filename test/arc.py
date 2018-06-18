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

w = 300
h = 300
v = VectorDisplay(s.sendall,lowendian=False)
v.initialize(w,h)
v.arc(100,100,50,45,135)
v.arc(100,100,50,180+45,180+135,fill=True)
sleep(0.5) # needed on Windows
s.close()
