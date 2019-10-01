from naoqi import ALProxy
from pdfReader import convert, layout
import arm
import re
import time
import io
# -*- coding: utf-8 -*-
import sys, os
import PDF_Client
import Reader



IP = "172.20.10.14"
Port = 9559
#bookInfo = 'C:\Users\Christian Lan\OneDrive\\nao_story_read\NAO CODE\\books\\book_pages.txt'
bookInfo = 'C:\Users\Zoe Chai\Desktop\\nao\\nao_story_read\\NAO CODE\\books\\book_pages.txt'
#authorL = 'C:\Users\Christian Lan\OneDrive\\nao_story_read\NAO CODE\outputAuthor.txt'
#contentL = 'C:\Users\Christian Lan\OneDrive\\nao_story_read\NAO CODE\outputContent.txt'
authorL = 'C:/Users/Zoe Chai/Desktop/nao/nao_story_read/NAO CODE/outputAuthor.txt'
contentL = 'C:/Users/Zoe Chai/Desktop/nao/nao_story_read/NAO CODE/outputContent.txt'

dialogFile = "/home/nao/home/nao/qitopics_enu.top"
dialogFile_Begin = "/home/nao/home/nao/begin_enu.top"
    

def pdfConnection(tts,connectionToPdf):
    """ This function checks if the connection to PDF Displayer application is established.
        If it is not connected, the robot will Initilize a dialog and wait for response.
        If the answer from human is in "connected" topic, the program check connection again.
    """
    
    is_Connected = connectionToPdf.connection()
    if not is_Connected:
        tts.say("The PDF Application is not opened.")
        tts.say("Please open the PDF displayer so that I can show the book to you.")
        time.sleep(1)
        tts.say("When the PDF displayer opened, please talk to me")
    else:
        tts.say("Connected to PDF displayer")
    dialog.subscribe('myModule1')
    
    print "initilized dialog"
    while(not is_Connected):
        time.sleep(0.5)
        memoryProxy.removeData("Dialog/Answered")
        memoryProxy.subscribeToEvent("Dialog/Answered","Dialog",IP)
        dialogOutput = memoryProxy.getData("Dialog/Answered")
        while(dialogOutput == None):
            #If no response, keep listening
            time.sleep(1)
            dialogOutput = memoryProxy.getData("Dialog/Answered")
        memoryProxy.unsubscribeToEvent("Dialog/Answered","Dialog")
        if dialogOutput.startswith("Ok , Let me check the connection"):            
            is_Connected = connectionToPdf.connection()
            if not is_Connected:
                tts.say("I still can't find the displayer")
                tts.say("Can you double check the PDF application.")
                tts.say("Thanks a lot!")

    dialog.unsubscribe('myModule1')
        

       
    
def trackChild(tracker,peopleProxy):
    """This method reads a people ID from memory. Use this ID for tracker.
        If the target lost, keep reading from memory if the robot sees a people"""

    targetName = "People"
    peopleProxy.setTimeBeforePersonDisappears(10)
    memoryProxy.removeData("PeoplePerception/PeopleList")
    memoryProxy.subscribeToEvent("PeoplePerception/PeopleList","PeopleTracker",IP)
    tts.say("Hello my friend, please show your face to me if you are ready to read with me")
    PeopleId = memoryProxy.getData("PeoplePerception/PeopleList")
    while(PeopleId == None or len(PeopleId) ==0):
        time.sleep(2)
        PeopleId = memoryProxy.getData("PeoplePerception/PeopleList")

        print PeopleId
    tracker.registerTarget(targetName, PeopleId)
    tracker.track(targetName)
    print "ALTracker successfully started, now show your face to robot!"
    
    try:
        while(tracker.isTargetLost()):
            PeopleId = memoryProxy.getData("PeoplePerception/PeopleList")
            #faceProxy.learnFace("Child")
            while(PeopleId == None or len(PeopleId) ==0):
                time.sleep(2)
                PeopleId = memoryProxy.getData("PeoplePerception/PeopleList")

            print PeopleId
            tracker.registerTarget(targetName, PeopleId)
            time.sleep(1)
            print"looking for target"
            
    except KeyboardInterrupt:
        
        print"Interrupted by user"
    PeopleId = memoryProxy.getData("PeoplePerception/PeopleList")
    memoryProxy.unsubscribeToEvent("PeoplePerception/PeopleList","PeopleTracker")
    print PeopleId
    
    

    
def dialogSetup(topics,is_Topic):
    """This method starts a dialog and make a proposal from topics. 
        After gets response from people, it returns robot output""" 
    dialog.subscribe('myModule1')
    if is_Topic:
        #This is used to determine which proposal should be generated from the topics
        dialog.setFocus(topics)
        dialog.gotoTopic("begin")
    else:
        dialog.setFocus(topics)
    memoryProxy.removeData("Dialog/Answered")
    memoryProxy.subscribeToEvent("Dialog/Answered","Dialog",IP)
    dialogOutput = memoryProxy.getData("Dialog/Answered")
    while(dialogOutput == None):
        time.sleep(1)
        dialogOutput = memoryProxy.getData("Dialog/Answered")
    dialog.unsubscribe('myModule1')
    memoryProxy.unsubscribeToEvent("Dialog/Answered","Dialog")
    
    return dialogOutput

def getBookInfo():
    """This method reads a file which is generated by TeachUI application.
        This method reads the first line as book title and second line as page numbers.
        Then storeing them into a list called "book" and returns this value""" 
    with open(bookInfo) as f:
        lines = f.readlines()
        book = []
        count = 0
        pages = []
        try:
            for line in lines:   
                line = line.rstrip()
                print line
                if count==1:
                   #convert all the numbers from string to int and append to a list
                    line = re.findall("[0-9]+",line)
                    for element in line:
                       pages.append(int(element))
                    line = pages

                book.append(line)
                count += 1
            print book
        except:
            print "Incorrect file format"
    return book
            
            




if __name__ == "__main__":
    """
        Define the sequential process for robot
            1 Initilize necessary proxies for the program
            2 Parse the book information 
            3 Building connection to PDF Displayer through socket
            4 Calibration
            5 Start tracker
            6 Start Reading
        Dialog is usually used to wait for human instructions 
            that if continue to certain processes
    
    """

    #Initialize proxies for this program 
    tts = ALProxy("ALTextToSpeech", IP,Port)
    asr = ALProxy("ALSpeechRecognition", IP, Port)
    memoryProxy = ALProxy("ALMemory", IP, Port)
    motion = ALProxy("ALMotion", IP ,Port)
    postureProxy = ALProxy("ALRobotPosture", IP, Port)
    tracker = ALProxy("ALTracker", IP, Port)
    faceProxy = ALProxy("ALFaceDetection", IP, Port)
    peopleProxy = ALProxy("ALPeoplePerception",IP,Port)
    #Initilize Dialog
    dialog = ALProxy('ALDialog', IP, Port)
    dialog.setLanguage("English")
    dialogFile = dialogFile.decode('utf-8')
    topic = dialog.loadTopic(dialogFile.encode('utf-8'))
    dialogFile_Begin = dialogFile_Begin.decode('utf-8')
    topic2 = dialog.loadTopic(dialogFile_Begin.encode('utf-8'))
    dialog.activateTopic(topic)
    dialog.activateTopic(topic2)
    dialog.setAnimatedSpeechConfiguration({"bodyLanguageMode":"disabled"})
    
    #Getting book Info
    book = getBookInfo()
    #Parse Book by author and content
    convert(book[0],[0])
    convert(book[0],book[1])
    
    #Setup Connection to PDF
    connectionToPdf = PDF_Client.client()
    pdfConnection(tts,connectionToPdf)
    #Initilize Read Instance
    readInstance = Reader.Reader(authorL,contentL,tts,tracker,connectionToPdf,IP,book)
    tts.say("Connection successful")
    tts.say("Now, initializing calibration")

    #Setup Calibration
    armMotion = arm.ArmMotion(motion,memoryProxy,postureProxy,tts)
    armMotion.setupCalibration()
    
    #Starts a dialog to ask if the user want to begin reading
    dialogOutput = dialogSetup(topic2,True)
    while(True):
        #If starts with alright, starting to read
        if dialogOutput.startswith("Alright"):
            #When user wants to begin, starting tracker to tracker user.
            trackChild(tracker,peopleProxy)
            print "begin read author"
            #Start reading the author
            readInstance.readAuthor()
            dialogOutput = dialogSetup(topic,False)
            break
        else:
            time.sleep(1)
            dialogOutput =  dialogSetup(topic2,True)

    #Tell the PDF Displayer to turn to the page that the robot is going to read. 
    connectionToPdf.turnPage()
    #Start reading content of the storybook
    readInstance.readContent(memoryProxy,asr,armMotion,dialog,topic)

    
    #Stop trakcer and set robot to rest.
    tracker.stopTracker()
    tracker.unregisterAllTargets()
    motion.rest()


