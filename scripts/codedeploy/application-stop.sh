#!/bin/bash

CONTAINER_ID=$(docker ps | grep 'brian:[[:alnum:]]\{7\}' | awk '{print $1}')

if [[ -n $CONTAINER_ID ]]; then
  docker stop $CONTAINER_ID
fi
