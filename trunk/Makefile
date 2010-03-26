JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        src/classify/Category.java \
        src/classify/GetWordsLynx.java \
        src/classify/SearchResult.java	  
        src/classify/ClassifyMe.java \
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) bin/*.class

