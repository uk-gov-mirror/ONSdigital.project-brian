# project-brian
Data pipeline for ONS website

This service converts CSDB or CSV files to JSON for use by time series datasets. 

### Context
Zebedee converts uploaded CSDB or CSV files in time series datasets into separate individual time-series at collection
approval stage. It does this by POSTing the csdb or csv file to Brian and receiving a JSON file in return which it then
processes in 
[DataPublication.java](https://github.com/ONSdigital/zebedee/blob/v2.13.0/zebedee-cms/src/main/java/com/github/onsdigital/zebedee/data/processing/DataPublication.java#L85)
(Link correct as at zebedee v2.13.0)

### How to debug locally

First obtain a valid CSDB  (for example the 
or CSV file. For exampl…

- `emp.csdb` - ('structured text') on a time series like
  [Average weekly earnings](https://www.ons.gov.uk/employmentandlabourmarket/peopleinwork/earningsandworkinghours/datasets/averageweeklyearnings)
- `upload-pop.csv` - ('csv') on 
  [Population estimates time series dataset](https://www.ons.gov.uk/peoplepopulationandcommunity/populationandmigration/populationestimates/datasets/populationestimatestimeseriesdataset)

Then with project-brian running locally via the `./run.sh` command, post the downloaded file to the service using.

```
curl -X POST -F "file=@somefile.csdb" http://localhost:8083/Services/ConvertCSDB -o csdb.json
```

or for CSV…

```
curl -X POST -F "file=@upload-pop.csv" http://localhost:8083/Services/ConvertCSV -o csv.json
```

The output will be the JSON that would be sent back to zebedee.
