#!/bin/bash -ex
mvn deploy -Dmaven.test.skip=true -Dgpg.skip=false -Dgpg.passphrase=$CI_DEPLOY_GPG_SECRET -Dgpg.secretKeyring=./.travis/private.gpg --settings .travis/settings.xml
