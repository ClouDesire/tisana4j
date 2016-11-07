#!/bin/bash -ex
mvn deploy -Dmaven.test.skip=true -Dgpg.skip=false -Dgpg.publicKeyring=./.travis/public.gpg -Dgpg.secretKeyring=./.travis/private.gpg --settings .travis/settings.xml
