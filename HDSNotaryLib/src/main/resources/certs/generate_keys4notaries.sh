#!/bin/bash
# Generate Keys sign with the RootCA for notaries

for i in {1..5}
do
	#Generate notary's private key
	openssl genrsa -out notary"$i".key
	
	#Obtain the corresponding public key
	openssl rsa -in notary"$i".key -pubout > notary"$i"_public.key

	# Certificate Sigining Request
	openssl req -new -key notary"$i".key -out notary"$i".csr

	# Notary's Key is now be signed using RootCA's key
	openssl x509 -req -days 365 -in notary"$i".csr -CA rootca.crt -CAkey rootca.key -out notary"$i".crt

done
