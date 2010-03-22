JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        classify/Category.java \
        classify/ClassifyMe.java \
        classify/GetWordsLynx.java \
        classify/SearchResult.java	  

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) searchme/util/*.class

