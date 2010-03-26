JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        classify/Category.java \
        classify/GetWordsLynx.java \
        classify/SearchResult.java	  
        classify/ClassifyMe.java \
default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) classify/*.class

