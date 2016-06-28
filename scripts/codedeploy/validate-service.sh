#!/bin/bash

if [[ $(docker inspect --format="{{ .State.Running }}" brian) == "false" ]]; then
  exit 1;
fi
