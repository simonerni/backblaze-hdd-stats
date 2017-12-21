library(XLConnect)
library(survival)
library("survminer")
library(scales)

this.dir <- dirname(parent.frame(2)$ofile)
setwd(this.dir)

raw <- readWorksheetFromFile("data.xlsx", sheet = 1)
raw = raw[-1,]
raw = raw[-1,]

gruppe_hochdosis <- subset(raw, Gruppe == "1")
gruppe_ohne <- subset(raw, Gruppe == "2")

# Progression Free Survival Ganze Kohorte

status_pfs <- as.numeric(raw[[361]]) # Spalte 361 ist Status (1 / 0)
time_pfs <- as.numeric(gsub(",", ".", raw[[363]])) # Spalte 363 ist Zeit in Monaten
group_pfs <- rep(c("PFS"), times=length(time))

pfs <- data.frame(status=status_pfs, time=time_pfs, group=group_pfs)

# Overall Survival Ganze Kohorte

status_os <- as.numeric(raw[[367]])
time_os <- as.numeric(gsub(",", ".", raw[[369]]))
group_os <- rep(c("OS"), times=length(time))

os <- data.frame(status=status_os, time=time_os, group=group_os)

# Kombiniere Data Frames

all_data <- rbind(pfs, os)

survival_fit <- survfit(Surv(time, status == 1) ~ group, data=all_data) 

survival_fit_pfs <- survfit(Surv(time, status == 1) ~ group, data=pfs) 
survival_fit_os <- survfit(Surv(time, status == 1) ~ group, data=os) 


#Plot it

plot <- ggsurvplot(survival_fit, 
                 pval=TRUE,
                 main = "Overall Survival & Progression Free Survival - All Patients",
                 ylab = "Percentage",
                 xlab = "Time From Start of Relapse Treatment (months)",
                 conf.int = FALSE,
                 legend.labs = c("PFS", "OS"),
                 risk.table = TRUE, # Add risk table
                 risk.table.title = "",
                 surv.scale = "percent",
                 #ggtheme = theme_bw(), # Change ggplot2 theme,
                 xlim = c(0, 150)
                 )

plot[[1]] <- plot[[1]] + xlab(NULL) + ylab(NULL) # Remove the labels from the table

plot[[2]] <- plot[[2]] + theme(legend.title=element_blank()) # Remove the title from the legends

plot[[2]] <- plot[[2]] + expand_limits(x = 0, y = 0) #Move y axis to origin
plot[[2]] <- plot[[2]] + scale_x_continuous(expand = c(0, 0)) + scale_y_continuous(expand = c(0, 0), labels=percent)
plot[[2]] <- plot[[2]] + theme(plot.margin = unit(c(0.5,0.5,0.5,0.5), "cm"))

print(plot)

png("figure1.png", width = 1000, height = 600, units = "px", res=100, pointsize=12, bg = "white")
print(plot)
dev.off()



# Plot with CI
plot <- ggsurvplot(survival_fit, 
                   pval=TRUE,
                   main = "Overall Survival & Progression Free Survival - All Patients",
                   ylab = "Percentage",
                   xlab = "Time From Start of Relapse Treatment (months)",
                   conf.int = TRUE,
                   legend.labs = c("PFS", "OS"),
                   risk.table = TRUE, # Add risk table
                   risk.table.title = "",
                   #ggtheme = theme_bw(), # Change ggplot2 theme,
                   xlim = c(0, 150)
)

plot[[1]] <- plot[[1]] + xlab(NULL) + ylab(NULL) # Remove the labels from the table
plot[[2]] <- plot[[2]] + theme(legend.title=element_blank()) # Remove the title from the legends

plot[[2]] <- plot[[2]] + expand_limits(x = 0, y = 0) #Move y axis to origin
plot[[2]] <- plot[[2]] + scale_x_continuous(expand = c(0, 0)) + scale_y_continuous(expand = c(0, 0), labels=percent)
plot[[2]] <- plot[[2]] + theme(plot.margin = unit(c(0.5,0.5,0.5,0.5), "cm"))


print(plot)

png("figure1_ci.png", width = 1000, height = 600, units = "px", res=100, pointsize=12, bg = "white")
print(plot)
dev.off()



# Plot PFS
plot <- ggsurvplot(survival_fit_pfs, 
                   pval=TRUE,
                   main = "Progression Free Survival - All Patients",
                   ylab = "Percentage",
                   xlab = "Time From Start of Relapse Treatment (months)",
                   conf.int = FALSE,
                   legend.labs = c("PFS"),
                   risk.table = TRUE, # Add risk table
                   risk.table.title = "",
                   color = "#2E9FDF",
                   #ggtheme = theme_bw(), # Change ggplot2 theme,
                   xlim = c(0, 150)
)

plot[[1]] <- plot[[1]] + xlab(NULL) + ylab(NULL) # Remove the labels from the table
plot[[2]] <- plot[[2]] + theme(legend.title=element_blank()) # Remove the title from the legends

plot[[2]] <- plot[[2]] + expand_limits(x = 0, y = 0) #Move y axis to origin
plot[[2]] <- plot[[2]] + scale_x_continuous(expand = c(0, 0)) + scale_y_continuous(expand = c(0, 0), labels=percent)
plot[[2]] <- plot[[2]] + theme(plot.margin = unit(c(0.5,0.5,0.5,0.5), "cm"))


print(plot)

png("figure1_pfs.png", width = 1000, height = 600, units = "px", res=100, pointsize=12, bg = "white")
print(plot)
dev.off()



# Plot PFS
plot <- ggsurvplot(survival_fit_pfs, 
                   pval=TRUE,
                   main = "Progression Free Survival - All Patients",
                   ylab = "Percentage",
                   xlab = "Time From Start of Relapse Treatment (months)",
                   conf.int = TRUE,
                   legend.labs = c("PFS"),
                   risk.table = TRUE, # Add risk table
                   risk.table.title = "",
                   color = "#2E9FDF",
                   #ggtheme = theme_bw(), # Change ggplot2 theme,
                   xlim = c(0, 150)
)

plot[[1]] <- plot[[1]] + xlab(NULL) + ylab(NULL) # Remove the labels from the table
plot[[2]] <- plot[[2]] + theme(legend.title=element_blank()) # Remove the title from the legends

plot[[2]] <- plot[[2]] + expand_limits(x = 0, y = 0) #Move y axis to origin
plot[[2]] <- plot[[2]] + scale_x_continuous(expand = c(0, 0)) + scale_y_continuous(expand = c(0, 0), labels=percent)
plot[[2]] <- plot[[2]] + theme(plot.margin = unit(c(0.5,0.5,0.5,0.5), "cm"))


print(plot)

png("figure1_pfs_ci.png", width = 1000, height = 600, units = "px", res=100, pointsize=12, bg = "white")
print(plot)
dev.off()


# Plot OS
plot <- ggsurvplot(survival_fit_os, 
                   pval=TRUE,
                   main = "Overall Survival - All Patients",
                   ylab = "Percentage",
                   xlab = "Time From Start of Relapse Treatment (months)",
                   conf.int = FALSE,
                   legend.labs = c("PFS"),
                   risk.table = TRUE, # Add risk table
                   risk.table.title = "",
                   color = "#2E9FDF",
                   #ggtheme = theme_bw(), # Change ggplot2 theme,
                   xlim = c(0, 150)
)

plot[[1]] <- plot[[1]] + xlab(NULL) + ylab(NULL) # Remove the labels from the table
plot[[2]] <- plot[[2]] + theme(legend.title=element_blank()) # Remove the title from the legends

plot[[2]] <- plot[[2]] + expand_limits(x = 0, y = 0) #Move y axis to origin
plot[[2]] <- plot[[2]] + scale_x_continuous(expand = c(0, 0)) + scale_y_continuous(expand = c(0, 0), labels=percent)
plot[[2]] <- plot[[2]] + theme(plot.margin = unit(c(0.5,0.5,0.5,0.5), "cm"))


print(plot)

png("figure1_os.png", width = 1000, height = 600, units = "px", res=100, pointsize=12, bg = "white")
print(plot)
dev.off()

# Plot OS
plot <- ggsurvplot(survival_fit_os, 
                   pval=TRUE,
                   main = "Overall Survival - All Patients",
                   ylab = "Percentage",
                   xlab = "Time From Start of Relapse Treatment (months)",
                   conf.int = TRUE,
                   legend.labs = c("PFS"),
                   risk.table = TRUE, # Add risk table
                   risk.table.title = "",
                   color = "#2E9FDF",
                   #ggtheme = theme_bw(), # Change ggplot2 theme,
                   xlim = c(0, 150)
)

plot[[1]] <- plot[[1]] + xlab(NULL) + ylab(NULL) # Remove the labels from the table
plot[[2]] <- plot[[2]] + theme(legend.title=element_blank()) # Remove the title from the legends

plot[[2]] <- plot[[2]] + expand_limits(x = 0, y = 0) #Move y axis to origin
plot[[2]] <- plot[[2]] + scale_x_continuous(expand = c(0, 0)) + scale_y_continuous(expand = c(0, 0), labels=percent)
plot[[2]] <- plot[[2]] + theme(plot.margin = unit(c(0.5,0.5,0.5,0.5), "cm"))


print(plot)

png("figure1_os_ci.png", width = 1000, height = 600, units = "px", res=100, pointsize=12, bg = "white")
print(plot)
dev.off()

capture.output(survival_fit, file="figure1.txt")

