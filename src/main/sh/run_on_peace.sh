#!/bin/sh
# Configure
MAVEN_HOME=/home/elmerg/progs/apache-maven-3.0.4/bin
PROJECT_HOME=/home/ziy/treckba-retrieval
DIR_FILE=/home/ziy/treckba-corpus/corpus/dir-list-test.txt
INDEX_ROOT=/home/ziy/treckba-corpus/index
LOG_ROOT=$PROJECT_HOME/log
START=1325376000
END=1335920400
RELEVANCE_LEVEL=RELEVANCE
# Execute
export MAVEN_OPTS=-Xmx2g
cd $PROJECT_HOME
TIME="$(date +%s)"
$MAVEN_HOME/mvn -Dtreckba-retrieval.collection.start=$START \
                -Dtreckba-retrieval.collection.end=$END \
                -Dtreckba-retrieval.retrieval.dirs-file=$DIR_FILE \
                -Dtreckba-retrieval.retrieval.index-root=$INDEX_ROOT \
                -Dtreckba-retrieval.eval.relevance-level=$RELEVANCE_LEVEL \
                exec:java | tee $LOG_ROOT/$TIME.log