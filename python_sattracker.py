#!/usr/bin/env python3

import pandas as pd
import re
import sys
import configparser
import time
import signal


cfg = configparser.ConfigParser(inline_comment_prefixes=('#', ';'))
cfg.read('configuration.ini')

SATNOGS_DB_API_KEY = cfg.get('Credentials', 'SATNOGS_DB_API_KEY')

pd.set_option('display.max_rows', None)
pd.set_option('display.max_columns', None)
pd.set_option('display.width', None)
pd.set_option('display.max_colwidth', None)

class Sat:
    def __init__(self,norad_cat_id,name,names,image,status,decayed,launched,deployed,website,operator,countries,telemetries,tle1,tle2):
        self.norad_cat_id = norad_cat_id
        self.name = name
        self.names = names
        self.image = image
        self.status = status
        self.decayed = decayed
        self.launched = launched
        self.deployed = deployed
        self.website = website
        self.countries = countries
        self.operator = operator
        self.telemetries = telemetries
        self.tle1 = tle1
        self.tle2 = tle2

# We select satellite fields in API:
url_sat = 'https://db.satnogs.org/api/satellites/?api_key={}&format=json'.format(SATNOGS_DB_API_KEY)
df_sat = pd.read_json(url_sat)

selection = ['norad_cat_id', 'name', 'names', 'image', 'status', 'decayed','launched','deployed','website','operator','countries','telemetries']
df_sat = df_sat[selection]
data = df_sat.head(3000)

#We select TLE data to add to Sat class:
url_tle= 'https://db.satnogs.org/api/tle/?api_key={}&format=json'.format(SATNOGS_DB_API_KEY)
df_tle = pd.read_json(url_tle)

selection_tle= ['tle1','tle2']
df_tle = df_tle[selection_tle]
data_tle = df_tle.head(3000)

#We only take the satellite 1 for the moment

new_satellite= Sat(data.iloc[1,0],data.iloc[1,1],data.iloc[1,2],data.iloc[1,3],data.iloc[1,4],data.iloc[1,5],data.iloc[1,6],data.iloc[1,7],data.iloc[1,8],data.iloc[1,9],data.iloc[1,10],data.iloc[1,11],data_tle.iloc[1,0],data_tle.iloc[1,1])

#we get the image of the satellite and we put it in the Sat class instead of the url of the image
# from PIL import Image
# import urllib.request
# urllib.request.urlretrieve(new_satellite.image, "file_name")
# img = Image.open("file_name")
# new_satellite.image = img


####Let's display this satellite:####
#Part taken from Albert Morea, may need some more modifications
import ephem
import datetime
from datetime import timezone
TimeNow = datetime.datetime.now()
UTCTimeNow = datetime.datetime.now(timezone.utc)

# We turn a TLE entry into a PyEphem Body
sat = ephem.readtle(new_satellite.name,new_satellite.tle1, new_satellite.tle2)
sat.compute(TimeNow)
print('%s %s' % (sat.sublong, sat.sublat))

# print("\n\n")
# print("Now Local: ",TimeNow)
# print("Now UTC: ",UTCTimeNow)
# print("Now UTC+1: ",UTCTimeNow+datetime.timedelta(minutes=6))
# print("Now UTC+2: ",UTCTimeNow+datetime.timedelta(minutes=12))
# print("\n\n")

times = []
orbit_times = []
orbit_lla = []
ortogonal_projection = []
samples = 128
period = 300 #minutes
fs = period/samples #frequency 

#time of each sample
for i in range(0, samples+1):
    time = fs*i
    print(time)
    times.append(time)

print(times) 

for i in range (0, len(times)):
    time = UTCTimeNow + datetime.timedelta(minutes=(times[i]))
    orbit_times.append(time)

print(orbit_times)

##### PYORBITAL #####
from pyorbital.orbital import Orbital
orb = Orbital(satellite="first",line1 = new_satellite.tle1, line2 = new_satellite.tle2)

for i in range (0, len(orbit_times)):
    lla = orb.get_lonlatalt(orbit_times[i]) # WORKS WITH UTC TIME!!!
    lon = lla[0]
    lat = lla[1]
    height1 = lla[2]*10000
    coords1 = (lon,lat,height1)
    coords2 = (lon, lat, 0)
    orbit_lla.append(coords1)
    ortogonal_projection.append(coords2)

    print("LLA: ", i, lla)

print(orbit_lla)

#KML making
#See kml tutorial
import simplekml


kml = simplekml.Kml()

pnt3 = kml.newpoint(name=new_satellite.name,gxballoonvisibility=1,description= "<![CDATA[ <body style=width:200px; height:200px>Satellite information will be here</body>]]>",coords= [(0,0)])

pnt3.style.iconstyle.icon.href=new_satellite.image

print(pnt3)



pnt = kml.newlinestring(name=new_satellite.name, coords=orbit_lla, altitudemode="relativeToGround", extrude=0)
pnt2 = kml.newlinestring(name=new_satellite.name, coords=ortogonal_projection, altitudemode="relativeToGround", extrude=0)
pnt2.style.linestyle.color = 'ff0000ff'
kml.save("./test_kml.kml")
print(kml.kml())