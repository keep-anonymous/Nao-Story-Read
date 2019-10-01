import socket
import time
HOST = '127.0.0.1'  # The server's hostname or IP address
PORT = 5555        # The port used by the server
"""
    Xinjie wrote this part
"""
class client:
    """
        This class initialzed a socket to connect to PDF displayer. 
        This class has two methods. One is to build connection, and 
        the other one is to send turn page command to PDF displayer.
    """
    def __init__(self):
        self.clientSocket = socket.socket(
            socket.AF_INET, socket.SOCK_STREAM)

    def connection(self):
        
        #now connect to the web server on port 5555
        try:
            self.clientSocket.connect((HOST, PORT))
            print "connection established"
            return True
        except socket.error:
            return False
    def turnPage(self):

        self.clientSocket.sendall('Turn\n')
        print "Turn Page command sent"