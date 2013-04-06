#!/bin/sh
# Configure
JAVA_HOME=/usr/java/latest
MAVEN_BIN=/bos/usr7/ziy/bin/apache-maven-3.0.5/bin
PROJECT_HOME=/bos/tmp4/ziy/treckba-corpus
CORPUS_ROOT=/bos/tmp19/spalakod/kba-stream-corpus-2012
JOB_ROOT=$PROJECT_HOME/job
LOG_ROOT=$PROJECT_HOME/log
DATA_ROOT=$PROJECT_HOME/data
DIR_LIST=$CORPUS_ROOT/dir-list.txt
EMPTYDB=$DATA_ROOT/oaqa-eval.db3
# Execute
for DIR in $(cat $DIR_LIST)
do
cp $EMPTYDB $DATA_ROOT/$DIR.db3
JOB=$JOB_ROOT/$DIR.job
echo "universe = vanilla" > $JOB
echo "executable = $PROJECT_HOME/src/main/sh/condor_executable.sh" >> $JOB
echo "arguments = $JAVA_HOME $MAVEN_BIN $PROJECT_HOME $CORPUS_ROOT $DIR" >> $JOB
echo "transfer_input_files = $DATA_ROOT/$DIR.db3"
echo "log = $LOG_ROOT/$DIR.condor" >> $JOB
echo "output = $LOG_ROOT/$DIR.log" >> $JOB
echo "error = $LOG_ROOT/$DIR.err" >> $JOB
echo "queue" >> $JOB
condor_submit $JOB
done
