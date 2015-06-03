#!/bin/bash -exu
mvn versions:set -DnewVersion=${VERSION} scm:checkin -Dmessage="Release $VERSION" -DgenerateBackupPoms=false
git push --tags
mvn versions:set -DnewVersion=${NEXT_VERSION}-SNAPSHOT scm:checkin -Dmessage="Preparing for next iteration" -DgenerateBackupPoms=false

