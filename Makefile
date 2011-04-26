#
# Making the VNC applet.
#

CP = cp
JC = javac
JCFLAGS = -source 1.4 -target 1.4
JAR = jar
ARCHIVE = BRHConsole.jar
SOURCE_ARCHIVE = BRHConsoleSource.jar
MANIFEST = MANIFEST.MF
PAGES = index.vnc
INSTALL_DIR = ../../website/wwwroot/jars

CLASSES = VncViewer.class RfbProto.class AuthPanel.class VncCanvas.class \
	  VncCanvas2.class \
	  OptionsFrame.class ClipboardFrame.class ButtonPanel.class \
	  DesCipher.class CapabilityInfo.class CapsContainer.class \
	  RecordingFrame.class SessionRecorder.class \
	  SocketFactory.class HTTPConnectSocketFactory.class \
	  HTTPConnectSocket.class ReloginPanel.class \
	  BRHInit.class BRHSocketFactory.class \
	  InStream.class MemInStream.class ZlibInStream.class \
	  org/json/JSONArray.class org/json/JSONException.class \
	  org/json/JSONObject.class org/json/JSONTokener.class \
	  org/json/JSONWriter.class org/json/JSONString.class \
	  'org/json/JSONObject$$1.class' 'org/json/JSONObject$$Null.class'

SOURCES = VncViewer.java RfbProto.java AuthPanel.java VncCanvas.java \
	  VncCanvas2.java \
	  OptionsFrame.java ClipboardFrame.java ButtonPanel.java \
	  DesCipher.java CapabilityInfo.java CapsContainer.java \
	  RecordingFrame.java SessionRecorder.java \
	  SocketFactory.java HTTPConnectSocketFactory.java \
	  HTTPConnectSocket.java ReloginPanel.java \
	  BRHInit.java BRHSocketFactory.java \
	  InStream.java MemInStream.java ZlibInStream.java \
	  org/json/JSONArray.java org/json/JSONException.java \
	  org/json/JSONObject.java org/json/JSONTokener.java \
	  org/json/JSONWriter.java org/json/JSONString.java


all: $(CLASSES) $(ARCHIVE) $(SOURCE_ARCHIVE)

$(CLASSES): $(SOURCES)
	$(JC) $(JCFLAGS) -O $(SOURCES)

$(ARCHIVE): $(CLASSES) $(MANIFEST) cacerts
	$(JAR) cfm $(ARCHIVE) $(MANIFEST) $(CLASSES) cacerts

$(SOURCE_ARCHIVE): $(SOURCES) LICENCE.TXT Makefile MANIFEST.MF README
	$(JAR) cf $(SOURCE_ARCHIVE) $^

install: $(ARCHIVE) $(SOURCE_ARCHIVE)
	mkdir -p $(INSTALL_DIR)
	$(CP) $(ARCHIVE) $(INSTALL_DIR)
	$(CP) $(SOURCE_ARCHIVE) $(INSTALL_DIR)

export:: $(CLASSES) $(ARCHIVE) $(PAGES)
	@$(ExportJavaClasses)

clean::
	$(RM) *.class *.jar
