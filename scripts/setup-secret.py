import os
import binascii

secret_dir = os.environ['TRACE_DIR']+'/.secret/'
key = binascii.hexlify(os.urandom(32))
secret = open(secret_dir+"key", 'w')
secret.write(key)
