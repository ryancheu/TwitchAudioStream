#Quick script to setup stream, intending to modify it soon to do
#just audio so auto selects lowest stream quality

#Starts stream on localhost, use ifconfig to get IP address if local,
#or use port forwarding if remote

#Requires rtmpdump and vlc to be installed
#Will only work on OSX and linux, probably not that difficult
#to port to windows as well

#Takes in a username
#Example usage: python r1chScript.py liquidhero

#Thanks to R1CH 
#http://r1ch.net/9-linux/2-transcoding-twitch-streams-to-my-android-phone

import urllib2;
import sys;
import json;
import re;
import os;

#Open the json of the stream
username = sys.argv[1];
url = "http://usher.justin.tv/find/" + username + ".json?type=any";
jsonRaw = urllib2.urlopen(url);
json = json.loads(jsonRaw.read());

#Find the lowest quality stream
lowest = 2024;
lowestIndex = 0;
for i in range(len(json)):
    quality = re.search('[0-9]+',json[i]['display']);
    test = int(quality.group(0));
    if test < lowest :
        lowest = test;
        lowestIndex = i;

jsonLowest = json[lowestIndex];
token = jsonLowest['token'];
connect = jsonLowest['connect'];
play = jsonLowest['play'];

#build the command
command = "rtmpdump --live -r \'";
command = command + connect;
command = command + "\' -W \'http://www-cdn.jtvnw.net/widgets/live_site_player.swf\'";
command = command + " -p \'http://www.twitch.tv/\' --jtv \'";
command = command + token;
command = command + "\' --playpath \'"
command = command + play;
command = command + "\' --quiet --flv \'-\'"
command = command + "| vlc --intf=dummy --rc-fake-tty -vvv - --sout \'#transcode{vcodec=h264,vb=800k,acodec=aac,ab=72k}:standard{access=http,mux=ts,dst=:8080}\'"

os.system(command);
