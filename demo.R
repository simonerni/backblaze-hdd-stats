library(survival)
library(survminer)
library(scales)

this.dir <- dirname(parent.frame(2)$ofile)
setwd(this.dir)

data <- read.csv(file="demo.csv",head=TRUE,sep=",")

status <- as.numeric(data[[5]])
time <- as.numeric(data[[4]])
group <- rep(c("All Drives"), times=length(time))

os <- data.frame(status=status, time=time, group=group)
s <- survfit(Surv(time, status == 1) ~ group, data=os)

plot <- ggsurvplot(s,
                   data=os,
                   censor=TRUE,
                   pval=FALSE,
                   main = "Overall Survival",
                   ylab = "Probability of Survival",
                   xlab = "Time From Start of First Install (days)",
                   conf.int = FALSE,
                   break.time.by = 365,
                   risk.table = TRUE, # Add risk table
                   risk.table.title = "",
                   risk.table.height = 0.25,
                   ggtheme = theme_light()
)

plot$table <- plot$table + xlab("How many drives are still in use after x days") + ylab(NULL) # Remove the labels from the table
plot$plot <- plot$plot + theme(legend.title=element_blank()) # Remove the title from the legends


png("demo.png", width = 2200, height = 1400, units = "px", res=300, pointsize=12, bg = "white")

print(plot)
dev.off()