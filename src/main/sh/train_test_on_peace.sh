#!/bin/sh
# Configure
MAVEN_HOME=/home/elmerg/progs/apache-maven-3.0.4/bin
PROJECT_HOME=/home/ziy/treckba-retrieval
LOG_ROOT=$PROJECT_HOME/log
RELEVANCE_LEVEL=RELEVANCE
# Execute
export MAVEN_OPTS=-Xmx8g
cd $PROJECT_HOME
TIME="$(date +%s)"
$MAVEN_HOME/mvn clean package \
                -Dtreckba-retrieval.eval.relevance-level=$RELEVANCE_LEVEL \
                exec:java -Dtrain | tee $LOG_ROOT/$TIME.train.log
$MAVEN_HOME/mvn clean package \
                -Dtreckba-retrieval.eval.relevance-level=$RELEVANCE_LEVEL \
                exec:java | tee $LOG_ROOT/$TIME.test.log