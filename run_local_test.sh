#!/bin/bash
./gradlew build
java -jar NATT.jar -nc https://gitlab.com/ap5vs/test-config/telnet-client-config/-/raw/main/test-config-2.yaml?ref_type=heads
