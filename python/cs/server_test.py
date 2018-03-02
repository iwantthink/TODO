import socket
import threading
import time

s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)

s.bind(('127.0.0.1',9999))

s.listen(5)

print('waiting for connection')


def tcplink(sock,addr):
    print('accept new connection from %s:%s'%addr)

    sock.send(b'Welcom')

    while True:
        data = sock.recv(1024)

        time.sleep(1)

        if not data or data.decode('utf-8')=='exit':
            break

        sock.send(('Hello,%s!'%data.decode('utf-8')).encode('utf-8'))

    socket.close()
    print('Connection from %s is closed'%addr)



while True:
    
    sock,addr = s.accept()

    t = threading.Thread(target = tcplink,args = (sock,addr))

    t.start()

