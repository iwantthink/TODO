class Stu(object):
    def __init__(self):
        self.name = 'jack'
        self.__age = 28

i = 1
i = i+1
s = Stu()
print(s.name)
print(s._Stu__age)
