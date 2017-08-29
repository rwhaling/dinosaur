#!/usr/bin/env python3
import os, cgi, sys

if __name__ == "__main__":
    fields = cgi.FieldStorage()
    path_info = os.environ.get("PATH_INFO","")
    path_components = path_info.split("/")
    sys.stdout.write("Content-type: text/html\r\n\r\n")
    print("hello\n")
    print(fields)
    print(path_components)