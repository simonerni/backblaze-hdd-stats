library(survival)
library("survminer")
library(scales)
library(plotly)
library("ggthemes")


this.dir <- dirname(parent.frame(2)$ofile)
setwd(this.dir)

raw <- read.csv(file="data.csv",head=TRUE,sep=",")

#Filter models with less than 500 harddrives ever in usage
model_frequency <- as.data.frame(table(raw[[6]]))
models_with_small_occurrence <- as.data.frame(subset(model_frequency, Freq<500))

raw <- subset(raw, !is.element(model,models_with_small_occurrence[[1]]))
#only hgst drives
raw <- subset(raw, grepl("Hitachi", model))

status <- as.numeric(raw[[5]])
time <- as.numeric(raw[[4]])
#group <- rep(c("All Drives"), times=length(time))
group <- raw[[6]]


os <- data.frame(status=status, time=time, group=group)
s <- survfit(Surv(time, status == 1) ~ group, data=os)

names(s$strata) = gsub("group=", "", names(s$strata))

plot <- ggsurvplot(s,
                   data=raw,
                   censor=FALSE,
                   pval=TRUE,
                   main = "Overall Survival - HardDrives per Model",
                   ylab = "Probability of Survival",
                   xlab = "Time From Start of First Install (days)",
                   conf.int = TRUE,
                   break.time.by = 365,
                   #legend.labs = c("OS"),
                   risk.table = TRUE, # Add risk table
                   risk.table.title = "",
                   risk.table.height = 0.3,
                   #xscale = "d_y",
                   ggtheme = theme_light()
)


plot$table <- plot$table + xlab("How many drives are still in use after x days") + ylab(NULL) # Remove the labels from the table
plot$plot <- plot$plot + theme(legend.title=element_blank()) # Remove the title from the legends

# plot$plot <- plot$plot + expand_limits(x = 0, y = 0) #Move y axis to origin
# plot$plot <- plot$plot + scale_x_continuous(expand = c(0, 0)) + scale_y_continuous(expand = c(0, 0), labels=percent)
# plot$plot <- plot$plot + theme(plot.margin = unit(c(0.5,0.5,0.5,0.5), "cm"))

#htmlwidgets::saveWidget(as.widget(ggplotly(plot$plot)), "graph.html")

png("grouped_all_models_filtered_500_hitachi.png", width = 3000, height = 2000, units = "px", res=300, pointsize=12, bg = "white")

print(plot)
dev.off()
