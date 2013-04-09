#!/bin/sh
# Configure
JAVA_HOME=/usr/java/latest/
MAVEN_HOME=/bos/usr7/ziy/bin/apache-maven-3.0.5/bin
PROJECT_HOME=/bos/tmp4/ziy/treckba-retrieval
QUERIES_DIR=$PROJECT_HOME/retrieval-cache/queries
DB_FILE=$PROJECT_HOME/retrieval-cache/cache.db3
DIR_FILE=/bos/tmp4/ziy/treckba-corpus/corpus/dir-list.txt
INDEX_ROOT=/bos/tmp4/ziy/treckba-corpus/index
HOME=/bos/usr7/ziy
# Execute
export JAVA_HOME=$JAVA_HOME
export MAVEN_OPTS=-Xmx1g
cd $PROJECT_HOME
TIME="$(date +%s)"
$MAVEN_HOME/mvn clean package exec:java -Dgenerate-cache-condor \
                -Dexec.args="$DB_FILE $QUERIES_DIR $DIR_FILE $INDEX_ROOT $HOME $PROJECT_HOME"
