#!/bin/sh
# Configure
JAVA_HOME=$1
MAVEN_BIN=$2
PROJECT_HOME=$3
CORPUS_ROOT=$4
INDEX_ROOT=$PROJECT_HOME/index
LOG_ROOT=$PROJECT_HOME/log
DATA_ROOT=$PROJECT_HOME/data
DIR=$5
# Execute
export JAVA_HOME=$JAVA_HOME
cd $PROJECT_HOME
$MAVEN_BIN/mvn -Dtreckba-corpus.collection.root=$CORPUS_ROOT \
               -Dtreckba-corpus.collection.dir=$DIR \
               -Dtreckba-corpus.index.root=$INDEX_ROOT \
               -Dtreckba-corpus.index.dir=$DIR \
               -Dtreckba-corpus.persistence-provider.url=jdbc:sqlite:$DATA_ROOT/$DIR.db3 \
               exec:java