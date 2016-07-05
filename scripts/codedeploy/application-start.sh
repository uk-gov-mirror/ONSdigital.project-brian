#!/bin/bash

ECR_REPOSITORY_URI=
GIT_COMMIT=

docker run -d      \
  --name=brian     \
  --net=publishing \
  --restart=always \
  $ECR_REPOSITORY_URI/brian:$GIT_COMMIT
