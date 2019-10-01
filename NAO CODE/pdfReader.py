from StringIO import StringIO
from pdfminer.pdfinterp import PDFResourceManager, PDFPageInterpreter
from pdfminer.converter import TextConverter,PDFPageAggregator
from pdfminer.layout import LAParams,LTTextBoxHorizontal, LAParams
from pdfminer.pdfpage import PDFPage
from pdfminer.pdfdocument import PDFDocument
from pdfminer.pdfparser import PDFParser
from pdfminer.layout import LTTextBox, LTTextLine, LTFigure, LTImage,LTPage
import re
import sys, os
sys.path.append('/NAO CODE/books/')
"""
    In order to generate scripts from the book and get 
    the location of the scripts, we used a library called 
    'PDFMiner'. We refered the examples from https://www.binpress.com/manipulate-pdf-python/

    Xinjie implemented convert function, and
    Liuyi implemented getlocation, layout and parse_layout function
"""

locationImg = []
locationTxt = []
def convert(fname,pages=None):
    """
        This function convert the pdf file to txt file that contains 
        all the sentences for each page of the pdf
    """
    
    
    if not pages:
        pagenums = set()
    else:
        if pages == [0]:
            outputName = "outputAuthor.txt"
        else:
            outputName = "outputContent.txt"
        pagenums = set(pages)

    output = StringIO()
    manager = PDFResourceManager()
    converter = TextConverter(manager, output, laparams=LAParams())
    interpreter = PDFPageInterpreter(manager, converter)
    infile = file(os.path.join(sys.path[0]+"\\books",fname), 'rb')
    for page in PDFPage.get_pages(infile, pagenums):
        interpreter.process_page(page)
    infile.close()
    converter.close()
    text = output.getvalue()
    output.close
    # write Content to .txt
    text_file = open(os.path.join(sys.path[0],outputName), "w")
    text = re.sub("\s\s+", " ", text)
    text_file.write("%s" % text)
    text_file.close()


def getloction (pagesize, bbox):
    """
        This function returns location of the object
    """
    x = pagesize[2]/2
    y = pagesize[3]/2
    x0 = (bbox[2]+bbox[0])/2
    y0 = (bbox[3]+bbox[1])/2
   
    if (abs(x0-x)<=30) and (abs(y0-y)<=30):
        return "middle"
    elif (x0 < x) and (y0 < y):
        return "leftbottom"
    elif (x0 > x) and (y0 > y):
        return "righttop"
    elif (x0 < x) and (y0 > y):
        return "lefttop"
    else:
        return "rightbottom"
    


"""Function to parse the layout tree."""
def parse_layout(pagesize, layout):
    
    locations_image = []
    locations_text = []
    
    for lt_obj in layout:   
        if isinstance(lt_obj,LTFigure):
            parse_layout(pagesize, lt_obj)
        if isinstance(lt_obj, LTImage):
         
            location_img = getloction(pagesize, lt_obj.bbox)
            locations_image.append(location_img)
            locationImg.append(locations_image[0])
            break
    for lt_obj in layout:  
        if isinstance(lt_obj, LTTextBox) or isinstance(lt_obj, LTTextLine):
            text = (lt_obj.get_text()).encode('utf-8')
            
            if not (text == "([0-9]+)\/([0-9]+)"):
                location_txt = getloction(pagesize, lt_obj.bbox)           
                locations_text.append(location_txt)
                locationTxt.append(locations_text[0])
                break
                
    
    
        
def layout(isText, fname, pages=None):
    """
        This function returns two dictionaries that 
        contains location of wordings and images respectively
    """
    
    if not pages:
        pagenums = set()
    else:
        pagenums = set(pages)

    rsrcmgr = PDFResourceManager()
    laparams = LAParams()
    device = PDFPageAggregator(rsrcmgr, laparams=laparams)
    interpreter = PDFPageInterpreter(rsrcmgr, device)

    infile = file(os.path.join(sys.path[0]+"\\books",fname), 'rb')
    
    #get the page zise
    parser = PDFParser(open(os.path.join(sys.path[0]+"\\books",fname), 'rb')) 
    doc = PDFDocument(parser)
    pagelist = []
    for page in PDFPage.create_pages(doc):
        pagelist.append(page)
    pagesize = pagelist[1].mediabox        

    for page in PDFPage.get_pages(infile, pagenums):
        interpreter.process_page(page)
        layout = device.get_result()
        parse_layout(pagesize, layout)
    infile.close()
   
    dict_img = dict(zip(pages,locationImg))
    dict_txt = dict(zip(pages,locationTxt))

    if isText == True:
        return dict_txt
    else:
        return dict_img


