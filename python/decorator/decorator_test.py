def log(func):
    def wrapper(*args,**kw):
        print('%s was called'%func.__name__)
        return func(*args,**kw)
    return wrapper

def log2(text):
    def decorator(func):
        def wrapper(*args,**kw):
            print('%s was called'%func.__name__,end='')
            print('input text  = %s'%text)
            return func(*args,**kw)
        return wrapper
    return decorator
