JAVAC=javac -g
JAVAC_ARGS=

SOURCES=RandomBot.java
CLASSES=$(SOURCES:%.java=%.class)
JAR=MyBot.jar
.PHONY: all clean

all: $(CLASSES) $(JAR)

$(JAR): $(CLASSES)
	jar cvfm $(JAR) mainClass *.class

%.class: %.java
	$(JAVAC) $(JAVAC_ARGS) $<
clean:
	-rm -Rf *.class
