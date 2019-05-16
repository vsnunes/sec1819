#!/bin/bash
#Convert private keys to PKCS8

mkdir -p java_certs

for i in {1..5}
do
    openssl pkcs8 -topk8 -in user"$i".key -passin file:user"$i"_pass.txt -out java_certs/private_user"$i"_pkcs8.pem -passout file:user"$i"_pass.txt
    openssl pkcs8 -topk8 -in notary"$i".key -passin file:notary"$i"_pass.txt -out java_certs/private_notary"$i"_pkcs8.pem -passout file:notary"$i"_pass.txt

done
