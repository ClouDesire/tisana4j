#!/bin/bash -ex
mvn deploy -Dmaven.test.skip=true --settings .travis/settings.xml
