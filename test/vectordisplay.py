class VectorDisplay(object):
    def __init__(self,writer,lowendian=True):
        self.write = writer
        if lowendian:
            self.e16 = self.e16le
            self.e32 = self.e32le
        else:
            self.e16 = self.e16be
            self.e32 = self.e32be            
        
    def e16le(self, x):
        return (x&0xFF,(x>>8)&0xFF)
    def e16be(self, x):
        return ((x>>8)&0xFF,x&0xFF)
    def e32le(self, x):
        return (x&0xFF,(x>>8)&0xFF,(x>>16)&0xFF,(x>>24)&0xFF)
    def e32be(self, x):
        return ((x>>24)&0xFF, (x>>16)&0xFF, (x>>8)&0xFF, x&0xFF)
        
    def command(self,c,data):
        bc = ord(c)
        self.write(bytearray((bc,bc^0xFF)+data+((0xFF&sum(data))^0xFF,)))
        
    def initialize(self):
        self.command('H',self.e16(0x1234)+self.e16(0))
        
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
        
    def rectangle(self,x1,y1,x2,y2):
        self.line(x1,y1,x2,y1)
        self.line(x2,y1,x2,y2)
        self.line(x2,y2,x1,y2)
        self.line(x1,y2,x1,y1)

    def foreColor(self,c):
        self.attr32('f',self.e32(c))
        
    def point(self,x,y):
        self.command('P',self.e16(x)+self.e16(y))