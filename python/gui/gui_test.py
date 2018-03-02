from tkinter import *

class Application(Frame):
    def __init__(self,master=None):
        Frame.__init__(self,master)
        self.pack()
        self.createWidgets()

    def createWidgets(self):
        self.helloLabel = Label(self,text='Hello py')
        self.helloLabel.pack()
        self.quitButton = Button(self,text='Quit',command = self.quit)
        self.quitButton.pack()

if __name__ == '__main__':
    app = Application()
    app.master.title('hello ppp')
    app.mainloop()
