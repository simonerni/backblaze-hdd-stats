library(survival)
library(survminer)
library(rms)

# Set working directors
this.dir <- dirname(parent.frame(2)$ofile)
setwd(this.dir)

# Read generated CSV file
data = read.csv("data.csv")

# Filter out all models with less than 500 occurencies
model_frequency <- as.data.frame(table(data[["model"]]))
models_with_small_occurrence <- as.data.frame(subset(model_frequency, Freq<500))
filtered_data <- subset(data, !is.element(model,models_with_small_occurrence[[1]]))

generatePlot <- function(data, title, filename, width, height, riskTableHeight, allInOne, filter = NULL) {

  if (!is.null(filter)) {
    data <- subset(data, grepl(filter, model))
  }
  
  status <- as.numeric(data[["death"]])
  time <- as.numeric(data[["life"]])
  if (allInOne) {
    group <- rep(c("All Drives"), times=length(time))
  } else {
    group <- data[["model"]]
  }
  
  os <- data.frame(status=status, time=time, group=group)
  s <- survfit(Surv(time, status == 1) ~ group, data=os)
  
  if (!allInOne) {
    #Remove the group= prefix
    names(s$strata) = gsub("group=", "", names(s$strata))
  } 
  
  
  plot <- ggsurvplot(s,
                     data=os,
                     censor=FALSE,
                     pval=!allInOne,
                     ##main = title,
                     ylab = "Probability of Survival",
                     xlab = "Time From Start of First Install (days)",
                     conf.int = TRUE,
                     break.time.by = 365,
                     #legend.labs = c("OS"),
                     risk.table = TRUE, # Add risk table
                     risk.table.title = "",
                     risk.table.height = riskTableHeight,
                     #xscale = "d_y",
                     ggtheme = theme_light()
  )
  
  
  plot$table <- plot$table + xlab("How many drives are still in use after x days") + ylab(NULL) # Remove the labels from the table
  plot$plot <- plot$plot + theme(legend.title=element_blank()) # Remove the title from the legends
  
  
  png(filename, width = width, height = height, units = "px", res=300, pointsize=12, bg = "white")
  
  
  print(plot)
  
  dev.off()
  
}

generateResidualPlot <- function(data, filter, layout, width, height, filename) {
  
  if (!is.null(filter)) {
    data <- subset(data, grepl(filter, model))
  }
  
  status <- as.numeric(data[["death"]])
  time <- as.numeric(data[["life"]])
  group <- data[["model"]]
  
  fit1 = cph(Surv(time, status) ~ group, x=T,y=T)
  f <- cox.zph(fit1)


  png(filename, width = width, height=height, units = "px", res=300, pointsize=12, bg = "white")
  par(mfrow=layout)
  print(plot(f, resid=F))
  dev.off()
}

# All Hard Drives in One Group

generatePlot(filtered_data, "All HardDrives","figs/all_harddrives_single_group.png", 2200, 1400, 0.3, TRUE)
generatePlot(filtered_data, "Grouped", "figs/all_harddrives_overview.png",4000, 4000, 0.3, FALSE)
 
# HGST
generatePlot(filtered_data, "Grouped", "figs/hgst.png",3000, 2000, 0.2, FALSE, "HGST") 
generateResidualPlot(filtered_data, "HGST", c(1,1), 2000,2000,"figs/hgst_res.png")

## Hitachi
generatePlot(filtered_data, "Grouped", "figs/hitachi.png",3500, 2000, 0.3, FALSE, "Hitachi")
generateResidualPlot(filtered_data, "Hitachi", c(2,2), 4000,4000,"figs/hitachi_res.png")

## Seagate
generatePlot(filtered_data, "Grouped", "figs/seagate.png",3800, 2600, 0.3, FALSE, "Seagate")
generateResidualPlot(filtered_data, "Seagate", c(4,2), 1600,4000,"figs/seagate_res.png")

## WDC
generatePlot(filtered_data, "Grouped", "figs/wdc.png",3000, 1600, 0.3, FALSE, "WDC")
generateResidualPlot(filtered_data, "WDC", c(1,2), 4000,1600,"figs/wdc_res.png")

