from math import floor

class VectorDisplay(object):
    def __init__(self,writer,lowendian=True):
        self._write = writer
        if lowendian:
            self.e16 = self.e16le
            self.e32 = self.e32le
        else:
            self.e16 = self.e16be
            self.e32 = self.e32be            
            
    def write(self,data):
        sent = 0
        n = len(data)
        while sent < n:
            sentNow = self._write(data[sent:])
            if sentNow == 0:
                raise RuntimeError("socket connection failed")
            sent += sentNow
        
    def e16le(self, x):
        x = int(floor(x))
        return (x&0xFF,(x>>8)&0xFF)
    def e16be(self, x):
        x = int(floor(x))
        return ((x>>8)&0xFF,x&0xFF)
    def e32le(self, x):
        x = int(floor(x))
        return (x&0xFF,(x>>8)&0xFF,(x>>16)&0xFF,(x>>24)&0xFF)
    def e32be(self, x):
        x = int(floor(x))
        return ((x>>24)&0xFF, (x>>16)&0xFF, (x>>8)&0xFF, x&0xFF)
        
    def command(self,c,data):
        bc = ord(c)
        self.write(bytearray((bc,bc^0xFF)+data+((0xFF&sum(data))^0xFF,)))
        
    def initialize(self,width=240,height=320):
        self.command('Z',self.e16(0x1234)+self.e16(width)+self.e16(height)+self.e32(65536)+self.e16(0)*3)
        
    def line(self,x1,y1,x2,y2):
        self.command('L',self.e16(x1)+self.e16(y1)+self.e16(x2)+self.e16(y2))
        
    def attr8(self,attr,value):
        self.command('Y',(ord(attr),value))

    def attr16(self,attr,data):
        self.command('A',(ord(attr),)+data)

    def attr32(self,attr,data):
        self.command('B',(ord(attr),)+data)

    def fp32(self,x):
        return int(x*65536.+0.5)
        
    def thickness(self,x):
        self.attr32('t',self.e32(self.fp32(x)))
        
    def fillRectangle(self,x1,y1,x2,y2):
        self.command('R', self.e16(x1)+self.e16(y1)+self.e16(x2)+self.e16(y2))
        
    def coordinates(self,w,h):
        self.attr32('c',self.e16(w)+self.e16(h))
        
    def rounded(self,r):
        self.attr8('n',r)
        
    def rectangle(self,x1,y1,x2,y2,fill=False):
        if fill:
            self.command('R', self.e16(x1)+self.e16(y1)+self.e16(x2)+self.e16(y2))
        else:
            self.line(x1,y1,x2,y1)
            self.line(x2,y1,x2,y2)
            self.line(x2,y2,x1,y2)
            self.line(x1,y2,x1,y1)

    def roundedRectangle(self,x1,y1,x2,y2,r,fill=False):
        self.command('Q', self.e16(x1)+self.e16(y1)+self.e16(x2)+self.e16(y2)+self.e16(r)+(1 if fill else 0,))

    def foreColor(self,c):
        self.attr32('f',self.e32(c))
        
    def point(self,x,y):
        self.command('P',self.e16(x)+self.e16(y))
        
    def arc(self,cx,cy,r,angle1,angle2,fill=False):
        self.command('S',self.e16(cx)+self.e16(cx)+self.e16(r)+self.e32(angle1*65536)+self.e32(angle2*65536)+(1 if fill else 0,))
        
    def poly(self,points,fill=True):
        path = self.e16(len(points))
        for p in points:
            path += self.e16(p[0])+self.e16(p[1])
        self.command('N' if fill else 'O',path)
        
    def bitmap(self,x,y,width,height,data,depth=1,foreColor=0xFFFFFFFF,backColor=0xFF000000):
        if depth==1:
            dataLen = (width*height+7)//8
            outData = ( self.e32(8+dataLen)+(depth,0)+self.e16(x)+self.e16(y)+self.e16(width)+self.e16(height)+
                self.e32(foreColor)+self.e32(backColor)+data[:dataLen] )
        elif depth>=8:            
            dataLen = width*height*(depth//8)
            outData = ( self.e32(dataLen)+(depth,8)+self.e16(x)+self.e16(y)+self.e16(width)+self.e16(height)+
                data[:dataLen] ) 
        else:
            return

        self.command('K',outData)
