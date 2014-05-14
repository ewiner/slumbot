#  slumbot

### A Tool to Find Livable NYC Apartment Buildings

Looking for a new apartment in New York City?  Slumbot will search through public data to find warning signs for any 
apartment building: noise complaints, building code violations, nearby construction, and more.

A live preview is currently deployed at [http://slumbot.winer.ly/?preview](http://slumbot.winer.ly/?preview).

*This is currently unpolished, unfinished software, very much a work in progress.  I started writing it on May 12, 2014,
 mostly to have something decent to submit to my [Significance Labs](http://significancelabs.org/) application.*

### Data Sources

* Google Maps does the address lookup
* NYC Department of Buildings information is scraped from [BIS](http://a810-bisweb.nyc.gov/bisweb/bispi00.jsp)
* 311 information and building permits information comes from [NYC Open Data](https://data.cityofnewyork.us/)
* Possible future stuff:
    - Property tax records (including number of units, avg. worth of apartments) could come from [this MS Access file](https://data.cityofnewyork.us/Housing-Development/Property-Valuation-and-Assessment-data-Tax-Classes/cqds-77ys)
    - Look up Yelp reviews on building management
    - Apartment turnover info from StreetEasy

### Tech Notes

It's a pretty straightforward Scala Play 2.0 app.  To run locally, you'll need to set your Google API Key in 
````conf/application.conf````.  Then, just type ````./sbt run```` and go to [http://localhost:9000/?preview](http://localhost:9000/?preview).

State of the union:

* **Scala**: reasonably happy with the structure of the code.
* **JavaScript**: using just straight jQuery for simplicity / development speed.  It'll start to get very messy if the 
                  site gets any more complicated
* **Testing**: entirely nonexistent
* **Error Handling**: horrible.  Definitely necessary before release - many of the data sources (especially the BIS web
scraper) have a ton of planned and unplanned downtime, and I'm sure the source data isn't as consistent as my code demands.
* **UI Design and Polish**: pretty crappy still, there's plenty of little tweaks queueing up on my Trello board