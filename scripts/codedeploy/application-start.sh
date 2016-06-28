#!/bin/bash

ECR_REPOSITORY_URI=
GIT_COMMIT=

docker run -d --name brian-$GIT_COMMIT \
    --net=publishing                   \
    --restart=always                   \
    $ECR_REPOSITORY_URI/brian:$GIT_COMMIT
