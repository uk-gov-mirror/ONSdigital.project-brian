#!/bin/bash -eux

pushd project-brian
  mvn -DskipTests=true -Dossindex.skip=true clean package dependency:copy-dependencies
  cp -r Dockerfile.concourse target/* ../build/
popd
