# What is this?
The Jenkins-Notifier observes a single job for new builds (by polling every 10 seconds). In case of a new build a notification-window will pop up showing the results. This comes in handy when you want to observe long-running or often-executed jobs for a temporary time, without changing the pipeline to add alert.

# How to build the project as executable jar
mvn clean install jfx:jar
(Result will be under target\jfx\app)

# How to use
1) In the browser, go to the job which you would like to observe
2) Copy the URL (complete) and paste it in the GUI of the jenkins-notifier
3) Enter your Jenkins-Login username
4) Enter your Jenkins-Token (http://{your.jenkins}/user/{your.user}/configure)
5) Press Start
