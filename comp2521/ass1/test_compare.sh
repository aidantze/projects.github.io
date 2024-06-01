#!/bin/sh

if test $# -lt 1
then
    echo "Usage: ./test_compare.sh <test_number>"
    exit 1
fi

./testFlightDb "$1" > output
diff "tests/$1.exp" output
rm -rf output