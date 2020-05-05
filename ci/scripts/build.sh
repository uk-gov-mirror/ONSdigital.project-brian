#!/bin/bash -eux

pushd project-brian
  make build
  cp -r Dockerfile.concourse target/* ../build/
popd
