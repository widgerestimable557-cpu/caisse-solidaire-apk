#!/bin/sh
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
APP_BASE_NAME=$(basename "$0")
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

die() { echo "$*"; exit 1; }

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

[ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ] || die "gradle-wrapper.jar not found"

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain "$@"
