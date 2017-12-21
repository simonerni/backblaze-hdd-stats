
this.dir <- dirname(parent.frame(2)$ofile)
setwd(this.dir)

data = read.csv("data/2013-04-10.csv")  # read csv file 
