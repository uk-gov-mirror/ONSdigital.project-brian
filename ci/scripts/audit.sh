#!/bin/bash -eux

pushd project-brian
  mvn ossindex:audit
popd
