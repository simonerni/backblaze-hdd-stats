# Dissecting Backblaze Harddrive data

This repo is split in two parts:
1. A small java program that parses all raw CSV data from https://www.backblaze.com/b2/hard-drive-test-data.html and summarizes it for each hard drive (how long it was known to live and if it died) and puts it in the "data.csv" file. The raw data is not included here, the "data.csv" is.
2. A R script that generates the survival graphs and more from the data.csv

