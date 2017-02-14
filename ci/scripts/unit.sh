#!/bin/bash -eux

pushd project-brian
  mvn clean surefire:test
popd
