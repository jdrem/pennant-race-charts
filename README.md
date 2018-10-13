### Pennat Race Chart Generator
This program generates pennant race charts for Major League Baseball seasons.
The chart displays games aboe .500 in the y-axis and the date in the
y-axis:

![AL East 1978](images/al_east_1978.png)

#### Building
Build using Maven:

'''
mvn clean install copyDependencies
'''

All the depdencies are in Maven Central

#### Running
You will need a source of data for the seasons you want to graph. 
This program uses Retrosheets game logs which are available 
[here](https://www.retrosheet.org/gamelogs/index.html).  Download
the years you're interested in and unzip them.  The default location
is in the gamelogs sub directory of the project.  

Run the script with the year and optinally leagues or divisions you wnat:

'''
runPennantRaceCharts.sh Year [Division]...
'''

Divisions/Leagues can be AL, NL, ALE, ALC, ALW, NLE, NLC, NLW.  Note
that NL and AL only work for 1968, before divisions were created.

The charts will written to the top level project directory.
