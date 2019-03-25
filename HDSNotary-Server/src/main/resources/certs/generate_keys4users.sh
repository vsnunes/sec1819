#!/bin/bash
# Generate Keys sign with the RootCA for users

for i in {1..5}
do
	#Generate user's private key
	openssl genrsa -out user"$i".key
	
	#Obtain the corresponding public key
	openssl rsa -in user"$i".key -pubout > user"$i"_public.key

	# Certificate Sigining Request
	openssl req -new -key user"$i".key -out user"$i".csr

	# User's Key is now be signed using RootCA's key
	openssl x509 -req -days 365 -in user"$i".csr -CA rootca.crt -CAkey rootca.key -out user"$i".crt

done
