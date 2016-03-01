import os
import binascii

secret_dir = os.environ['TRACE_DIR']+'/.secret/'

if not os.path.exists(secret_dir):
	os.makedirs(secret_dir)

key = binascii.hexlify(os.urandom(32))
secret = open(secret_dir+"key", 'w')
secret.write(key)
