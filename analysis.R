library(survival)
library(survminer)
library(scales)

this.dir <- dirname(parent.frame(2)$ofile)
setwd(this.dir)

raw <- read.csv(file="data.csv",head=TRUE,sep=",")

#Filter models with less than 500 harddrives ever in usage
model_frequency <- as.data.frame(table(raw[[6]]))
models_with_small_occurrence <- as.data.frame(subset(model_frequency, Freq<500))
raw <- subset(raw, !is.element(model,models_with_small_occurrence[[1]]))

generateGraph <- function(data, modelFilter, riskTableHeight, width, height, filename, allInOne=FALSE) {
  if (modelFilter != "")
  {data <- subset(data, grepl(modelFilter, model))
  }
  
  status <- as.numeric(data[[5]])
  time <- as.numeric(data[[4]])
  group <- rep(c("All Drives"), times=length(time))
  if (allInOne) {
    group <- rep(c("All Drives"), times=length(time))
  } else {
    group <- data[[6]]
  }
  
  
  os <- data.frame(status=status, time=time, group=group)
  s <- survfit(Surv(time, status == 1) ~ group, data=os)
  
  if (!allInOne) {
    #Remove the group= prefix
    names(s$strata) = gsub("group=", "", names(s$strata))
  }

  plot <- ggsurvplot(s,
                     data=data,
                     censor=FALSE,
                     pval=!allInOne,
                     main = "Overall Survival",
                     ylab = "Probability of Survival",
                     xlab = "Time From Start of First Install (days)",
                     conf.int = TRUE,
                     break.time.by = 365,
                     risk.table = TRUE, # Add risk table
                     risk.table.title = "",
                     risk.table.height = riskTableHeight,
                     ggtheme = theme_light()
  )
  
  
  plot$table <- plot$table + xlab("How many drives are still in use after x days") + ylab(NULL) # Remove the labels from the table
  plot$plot <- plot$plot + theme(legend.title=element_blank()) # Remove the title from the legends
  

  png(filename, width = width, height = height, units = "px", res=300, pointsize=12, bg = "white")
  
  print(plot)
  dev.off()
}

generateGraph(data=raw, modelFilter="", riskTableHeight=0.5, width=2200, height=1400, filename="test.png", allInOne=TRUE)
