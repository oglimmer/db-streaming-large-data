#!/usr/bin/env bash

set -eu

TOTAL=${1:-1}

INITIAL_ROWS=$(MYSQL_PWD=root mysql -u root --protocol=tcp -N -s -e 'select count(*) from test.person')

curl http://localhost:8080 -d $TOTAL -H 'Content-Type: application/json'

function bar()
{
    CHAR=$1
    COUNT=$2
    if [ "$COUNT" = "0" ]; then
        echo "";
    else
        BAR="$(printf "$CHAR"'%.0s' $(seq 1 $COUNT))"
        echo "$BAR"
    fi
}

function progressbar()
{
    CUR_VAL=$1
    MAX_VAL=$2
    PERC=$(bc <<< "100*$CUR_VAL/$MAX_VAL")
    NOT_PERC="$((100-PERC))"
    BAR="$(bar '*' $PERC)$(bar ' ' $NOT_PERC)"
    echo -ne "$BAR ($PERC%)     \r"
}

ROWS=0
while [ "$ROWS" -lt "$(( 100*TOTAL ))" ]; do
    ROWS=$(MYSQL_PWD=root mysql -u root --protocol=tcp -N -s -e 'select count(*) from test.person')
    ROWS=$((ROWS - INITIAL_ROWS))
    progressbar "$ROWS" "$(( 100*TOTAL ))"
    sleep 1
done

echo -ne "All $ROWS rows are created."
