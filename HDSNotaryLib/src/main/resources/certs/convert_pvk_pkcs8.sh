#!/bin/bash
#Convert private keys to PKCS8

mkdir -p java_certs

for i in {1..5}
do
    openssl pkcs8 -in user"$i".key -topk8 -nocrypt -out java_certs/private_user"$i"_pkcs8.pem

done
