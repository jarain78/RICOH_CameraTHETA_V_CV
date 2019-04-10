# No-Camera OpenCV
Sample application that shows OpenCV running on both a physical 
device RICOH THETA V and an Android Video Device.  You
can develop a plug-in on the emulator and then have it run
on a physical device camera with only minor modifications.

Based on original work by roohii_3 with
modifications by https://github.com/iamagod (kasper on theta360.guide)

## Important Note on App Permissions

You must enable storage permissions in the settings.
If you have using a physical device RICOH THETA, you can
use Vysor to go into Settings and enable storage permissions.


## HTTP Webservice

this web service was developed as a first approach to the challenge, allowing the camera to send the
images for a deeper analysis of it. in this web service analyzes the image and detects the objects present. 
to perform this object detection, we use the YOLO: Real-Time Object Detection(https://pjreddie.com/darknet/yolo/) system. this allows us 
to identify the objects present in the image and return them to the APP.

This web service was developed in python and Flash, this web service can be downloaded from the 
following git (https://github.com/jarain78/RICOH_Http_Server/tree/develop).

to use the web service from Android studio, it is necessary to edit line 247 of the 
MainActivity.java file. In this line we find the following code "String url = "http://YOUR HOST/image";", 
replace YOUR HOST by the host where the web service is located. If this is done from the same computer,
YOUR HOST=the IP address of the machine.

Note that this same host has to be placed on the web server, specifically on line 28 of the HTTP_Server.py 
file (app.run(host="YOUR HOST", port=80)).



[![Web Service](https://youtu.be/8uAtpyOjRA8/0.jpg)](https://youtu.be/8uAtpyOjRA8 "RICOH")
