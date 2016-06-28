#!/bin/bash

GIT_COMMIT=

if [[ $(docker inspect --format="{{ .State.Running }}" brian-$GIT_COMMIT) == "false" ]]; then
  exit 1;
fi
