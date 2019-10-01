from naoqi import ALProxy
from pdfReader import convert, layout
import arm
import re
import time
import io
# -*- coding: utf-8 -*-
import sys, os
import PDF_Client
sys.path.append('/NAO CODE/books/')
filePath = os.path.abspath(os.path.dirname(__file__))


class Reader:
    """
        This class has two functions. One is to read author of the book, and 
        the other one is to read content
    """
    def __init__(self, authorFileName,contentFileName, tts, tracker,connectionToPdf,IP,book):
        self.authorFileName = authorFileName
        #self.authorFileName = os.path.join(filePath,authorFileName)

        self.contentFileName = contentFileName
        self.tts = tts
        self.tracker = tracker
        
        self.countPage = 0
        self.turnPage = 0
        self.connectionToPdf = connectionToPdf
        self.IP = IP
        self.PORT = 9559
        self.book = book
        self.bookTitle = book[0]
        self.pages = book[1]
        self.dictTxt = layout(True, self.bookTitle,self.pages)
        self.dictImg = layout(False, self.bookTitle,self.pages)
        self.trunPageSpeak = ["Let's look at this sentence",
                        "We are going to next page, Please look at here!",
                        "This is the sentence I'm going to read!"]

    def readAuthor(self):
        """ 
        This method reads title and author of the book. No matter what page that teacher chose,
        the program always read author for readers.
        """
        with open(self.authorFileName) as f:
            lines = f.readlines()
            print lines
            line = lines[0]
            line = line.lower()
            line = re.split("author:",line)
        self.tts.say("Today we are going to read a story book. \\pau=1000\\ Named "+line[0])
        self.tts.say("The author is: "+line[1])
        self.tts.say(" Remember if we read from this author before? ")
    
    def readContent(self,memoryProxy,asr,armMotion,dialog,topic):
        """
        This method reads content for readers by these steps:
            1 Process the script to get sentence that the robot going to read
            2 Locate chidlren's postion and track
            3 Checking if analyze children's attention by memory key
            4 If children is not focused, initlize dialog
            5 If current sentence is page number, do two things:
                5.1 Send turnpage command to PDF Displayer 
                5.2 Point to the sentence/image the robot going to read.
            6 Read the sentence
        """

        gaze = ALProxy("ALGazeAnalysis",self.IP,self.PORT)
        atts = ALProxy("ALAnimatedSpeech",self.IP,self.PORT)
        aup = ALProxy("ALAudioPlayer", self.IP, self.PORT)

        globalSentence = """"""
        count = 0
        globalFace = 9999
        
        with open(self.contentFileName) as f:
            lines = f.readlines()
            for line in lines:
                # For each line, trim both head&tail space
                # Left align the line and append to the string 'globalSentence'
                # Split globalsentence by period to get each sentence of the book.
                line = line.strip()
                line = line.ljust(len(line)+1)
                globalSentence = globalSentence+line
            sentence = re.split("\.",globalSentence)

            gaze.subscribe("ALGazeAnalysis")
            memoryProxy.subscribeToEvent("PeoplePerception/VisiblePeopleList","ALGazeAnalysis",self.IP)
            for sytax in sentence:
                toleranceRange = gaze.getTolerance()
                time.sleep(2)               
                PeopleId = memoryProxy.getData("PeoplePerception/VisiblePeopleList")
                targetPosition = self.tracker.getTargetPosition(0)
                #Checking if the current people from memorylist is same as the last time reading
                #If same, do the gaze analysis
                #If not, skip because it will cause an error that there is no data in memory
                if globalFace != PeopleId:
                    globalFace = PeopleId
                    
                    jump = True
                else:
                    jump = False
                time.sleep(2)
                
                if len(PeopleId) != 0 and not jump:
                    try:
                        visualData = memoryProxy.getData("PeoplePerception/Person/"+str(PeopleId[0])+"/IsLookingAtRobot")

                        LedProxy = ALProxy("ALLeds", self.IP, self.PORT)
                        LedProxy.randomEyes(2)
                        if visualData != 1:
                            #If the visualData is not equal 1, then the child is not looking at the book
                            dialog.subscribe('myModule')
                            dialog.activateTopic(topic)
                            dialog.gotoTopic("ExampleDialog")
                            dialog.deactivateTopic(topic)
                            dialog.unsubscribe('myModule')
                            self.tts.say("Let's continue")
                    except RuntimeError:
                        print"skip the error"
                        pass
                #Get page number from current sentence
                page = re.search("([0-9]+)\/[0-9]+",sytax)
                
                #count the pagenum and call the def locationToPoint to return a location

                #set pointing for first page, or it won't point because no page number reached. 
                if self.countPage == 0 and self.turnPage == 0:
                    pagenum = self.pages[0]
                    if self.dictTxt[pagenum] == "rightbottom":
                        location = self.dictImg[pagenum]
                    else:
                        location = self.dictTxt[pagenum]
                    self.turnPage = 1
                    self.tts.say("Let's look at this picture")
                    armMotion.point(location)
                    time.sleep(1)
                    
                
                if page:    
                    self.connectionToPdf.turnPage()
                    #Send a msg to pdf displayer to turn page
                    self.countPage = self.countPage + 1
                    if self.countPage >= len(self.pages):
                        break
                    pagenum = self.pages[self.countPage]
                    if self.dictTxt[pagenum] == "rightbottom":
                        location = self.dictImg[pagenum]
                    else:
                        location = self.dictTxt[pagenum]
                                  
                    #Various way to say trun page by using % operation to decide which 
                    #predefined sentence to say.
                    self.tts.say(self.trunPageSpeak[count%len(self.trunPageSpeak)])
                    armMotion.point(location)
                    time.sleep(2)
                self.tracker.lookAt(targetPosition,0,0.5,False)
                
                time.sleep(0.5)

                output = re.sub("([0-9]+)\/[0-9]+","",sytax)
            
                output = re.sub("[!@#$-]","",output)
                count += 1
                atts.say(output.lower(),{"bodyLanguageMode":"random"})
        atts.say("Yeah!",{"bodyLanguageMode":"contextual"})
        self.tts.say("We finished a book!")
        atts.say("Hope to see you next time! Bye!",{"bodyLanguageMode":"contextual"})
        gaze.unsubscribe("ALGazeAnalysis")
        
    
