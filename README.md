### Pennat Race Chart Generator
This program generates pennant race charts for Major League Baseball seasons.
The chart displays games aboe .500 in the y-axis and the date in the
y-axis:

![AL East 1978](images/al_east_1978.png)

#### Building
Build using Maven:

```
mvn clean install  dependency:copy-dependencies
```

All the depdencies are in Maven Central

#### Running
You will need a source of data for the seasons you want to graph. 
This program uses Retrosheets game logs which are available 
[here](https://www.retrosheet.org/gamelogs/index.html).  Download
the years you're interested in and unzip them.  The default location
is in the gamelogs sub directory of the project.  
                                                                                    
You can:
* Run the script with just the year and get all leagues or divisions:
```
runPennantRaceCharts.sh 2021
```
* Run the script with the year and a comma separated list of league or division abbreviations:
```
runPennantRaceCharts.sh 1978 ALE,ALW
```
* Run the script with the year and a comma separted list of team names. You must specify a title and file name:
```
runPennantRaceCharts.sh 2021 BOS,NYA,TOR,SEA --title="AL Wildcard" --file.name=al_wildcard_
```

Divisions/Leagues can be AL, NL, ALE, ALC, ALW, NLE, NLC, NLW.  NL and AL only work for 1968 and earlier, before divisions 
were created. NLC and ALC work only after 1993 when the central divisions were created.

The charts will written to the top level project directory.
