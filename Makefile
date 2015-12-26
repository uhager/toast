## Makefile for toast
## author: Ulrike Hager
##
## to create toast.jar run
## $ make
## $ jar cvmf manifest.mf toast.jar *.class

JAVAC = javac
   CLASS_FILES = FakeServer.class \
	GlobalVars.class \
	HistBar.class    \
	HistCircle.class \
	EnergyHistogram.class  \
	OneDHistogramFixed.class \
	OutputPanel.class  \
	Particle.class   \
	Sector.class       \
	TacticViewer.class \
	TwoDHistogramFixed.class \


%.class: %.java
	$(JAVAC) $<

default: $(CLASS_FILES)

clean:
	rm *.class
