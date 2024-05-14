#!/bin/bash -e

# This script runs unit tests using each JVM found in the following directory.
jvm_parent_dir="$HOME/.sdkman/candidates/java/"

find "$jvm_parent_dir" -maxdepth 1 -mindepth 1 -type d | \
while IFS="" read -r p || [ -n "$p" ]
do
  export JAVA_HOME="$p"
  mvn --version
  mvn clean test
done