#!/bin/bash -eux

pushd project-brian
  mvn clean package dependency:copy-dependencies -DskipTests=true
popd

cp -r project-brian/target/* target/
