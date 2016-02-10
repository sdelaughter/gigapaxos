##Install and Configure

###### 1. Create an Amazon EC2 Instance
 - 64-bit linux (t2.micro)
 - Make sure all network traffic is open (ignore the security warning)

###### 2. SSH to the Instance

###### 3. Update Packages
    sudo yum update

###### 4. Install Dependencies
    sudo yum install gzip git curl python libssl-dev pkg-config build-essential gcc gcc-c++

###### 5. Update to Java 1.8
    sudo yum remove java-1.7.0-openjdk
    sudo yum install java-1.8.0
    sudo yum install java-1.8.0-openjdk-devel

###### 6. Install Apache Ant
    cd /tmp
    sudo wget http://archive.apache.org/dist/ant/binaries/apache-ant-1.9.0-bin.tar.gz
    sudo tar xzf apache-ant-1.9.0-bin.tar.gz
    sudo mv apache-ant-1.9.0 /usr/local/apache-ant
    echo 'export ANT_HOME=/usr/local/apache-ant' >> ~/.bashrc
    echo 'export PATH=$PATH:/usr/local/apache-ant/bin' >> ~/.bashrc

###### 7. Install Node.JS v0.12.6 from Source
    cd /tmp
    wget http://nodejs.org/dist/v0.12.6/node-v0.12.6.tar.gz
    tar xvf node-v0.12.6.tar.gz
    cd node-v0.12.6
    ./configure
    make
    sudo make install

###### 8. Clone the Gigapaxos Repository
    cd
    git clone https://github.com/MobilityFirst/gigapaxos.git

###### 9. Move the files included with this README into place
    mv gigapaxos-etherpad/net ~/gigapaxos/src/
    mv gigapaxos-etherpad/etherpad ~/gigapaxos/src/edu/umass/cs/gigapaxos/examples/
 - The ‘net’ folder contains the Java API for etherpad
 - The ‘etherpad’ folder contains the example programs for testing gigapaxos with etherpad
 - You'll move the settings.json file later, once Etherpad is installed

###### 10. Install Etherpad
    cd
    mkdir etherpad
    cd etherpad
    git clone git://github.com/ether/etherpad-lite.git

###### 11. Test Start the Etherpad Server
    ~/etherpad/etherpad-lite/bin/run.sh
    Ctl-c
    
 - Note that this will take longer than usual the first time
 - You will get a warning about DirtyDB being used, ignore this unless you have a specific need for MySQL (DirtyDB is basically just a JSON store)
 - You will also get a warning that no Admin credentials have been set.  This is addressed below. 
 - The server runs indefinitely when started.  When you want to leave it running, you should launch it with screen, but you can just kill it with Ctl-C the first time.
 - Note that there’s no clear indication that the server has started successfully — as soon as you see a message about the plugin admin page (or a warning that admin credentials have not been set), it’s up and running.

###### 12. Configure Etherpad
 - The etherpad server's settings are stored in ~/etherpad/etherpad-lite/settings.json, which includes a lot of comments.  This is problematic since EtherpadPaxosApp needs to read the port value from the file, and the JSON libraries in Java will try to process the comments as data.  Rather than having to delete all the comments manually, you can replace settings.json with the version \included with this readme.  You should make a backup of the original settings.json file first, since the comments do contain valuable information that you may want to refer to later.  If you choose to remove the comments yourself for some reason, you will also need to make the following changes:
   - Uncomment the “users” object
   - Change the default password for the ‘admin’ and ‘user’ users if desired
 - You will also need to make sure that the contents of ~/etherpad/etherpad-lite/APIKEY.txt matches the apiKey variable in EtherpadPaxosApp.  If these do not match, the App will be unable to make any changes to the etherpad server.
   - Also note that APIKEY.txt must contain ONLY the one key, on one line — any commented lines will be interpreted as part of the key and will cause requests to be rejected.

###### 13. Start the Etherpad Server
    screen ~/etherpad/etherpad-lite/bin/run.sh
 - You can detach from the screen to leave it running in the background by typing Ctrl-a d
 - Once detached, you can reattach the running screen session with screen -x

###### 14. Verify that the Etherpad Server is Running
 - By default, etherpad runs on port 9001, though if needed you can change this by editing the “port” value in settings.json
 - Open a web browser and navigate to X.X.X.X:9001 (where X.X.X.X is the server’s IP address).  You should see a screen with a “New Pad” button and a text box in which you can enter the name of a pad to create/open.
 - Note that you can also access the admin console for the etherpad server at X.X.X.X:9001/admin (you’ll be prompted to log in with the admin credentials you set in settings.json in the Configure Etherpad section above).  This will allow you to manage plugins, edit settings.json, and restart the etherpad server.  You can also view information about the server’s current version and its installed plugins, parts, and hooks.

###### 15. Create the Testing Pad
 - EtherpadPaxosClient will send a number of requests to EtherpadPaxosApp, requesting to set the text of a pad titled 'foo' to 'bar'.
 - There is currently no error handling in either the client or the app to deal with a case where the pad 'foo' does not yet exist, so you must create this pad on each etherpad server before running the program.  Note that failure to do so will not cause the Client or App to fail, and in fact the client will still display an average delay metric upon completion.  The only way to see the failures is by reattaching to the screen session that the etherpad server's run.sh script is running in.
 - To create the testing pad:
   - Open a web browser port 9001 on the server as in the above section.
   - Type 'foo' (without the quotes) into the text box and click "OK".
   - You should be taken to the pad, with a message that starts: "Welcome to Etherpad!"
   - You can feel free to modify this text if you'd like to experiment with etherpad.  As long as the 'foo' pad exists the program will work, just be aware that running EtherpadPaxosClient/App will overwrite the entire text of the pad.

###### 16. Create Copies of Etherpad
In order to run a single-machine test, you'll need at least three distinct etherpad servers running on the same EC2 instance, each on a different port.  It's easiest to create the copies after performing the steps above to avoid repeating the setup processes for each one.  You should stop the etherpad server before entering the following commands, assuming it's still running from the previous step.
    
    cd ~/etherpad
    mv etherpad-lite etherpad-lite-1
    cp -av etherpad-lite-1 etherpad-lite-2
    cp -av etherpad-lite-1 etherpad-lite-3
    
###### 17. Configure the Port for each Etherpad Server
 - Once you've created copies of the etherpad-lite directory, you'll need to specify a different port number for each server to use.  Theoretically, you can use just about any port number you like as EtherpadPaxosApp will read the value from settigns.json when it starts.  That said, it has only been tested with ports 9001-9003, and obviously whatever ports you choose will need to be unused by other applications.
 - Modify the port value in the settings.json file in each ~/etherpad/etherpad-lite-X folder that you created above
   - I'd recommend setting etherpad-lite-1 to 9001, etherpad-lite-2 to 9002, and etherpad-lite-3 to 9003

###### 18. Start the Etherpad Servers
 - The same as in step 13 above, but for each of the three run.sh scripts in the etherpad-lite-X folders
 - You may also want to verify that they're all running as in step 14 above, but with the ports you specified in step 17
   - Make sure that if you modify the text on one etherpad server you don't see the change on either of the others (to confirm that they are in fact three separate servers with three separate databases)
 - Note: Screen's -S option offers a handy way to label your screen sessions so that you can easily reattach a specific session to debug any issues.
   - For example, launch and detach from each one as follows:
    ```
    screen -S etherpad1 ~/etherpad/etherpad-lite-1/bin/run.sh
    Ctrl-a d
    screen -S etherpad2 ~/etherpad/etherpad-lite-2/bin/run.sh
    Ctrl-a d
    screen -S etherpad3 ~/etherpad/etherpad-lite-3/bin/run.sh
    Ctrl-a d
    ```
   - Then you can reattach the etherpad-lite-2 server with:
    ```
    screen -r etherpad2
    ```
    
###### 19. Compile the JAR
    cd ~/gigapaxos
    ant


#Run

###Single Machine
 - You'll need to open 4 separate SSH sessions, one for each server instance and one for the client.  Alternatively, you could use the screen command to launch all instances from a single session, but that would make it much more difficult to monitor the output.

###### On the First Session
    cd ~/etherpad/etherpad-lite-1
    rm -rf paxos_logs
    java -ea -cp /home/ec2-user/gigapaxos/dist/gigapaxos-1.0.jar -DgigapaxosConfig=/home/ec2-user/gigapaxos/src/edu/umass/cs/gigapaxos/examples/etherpad/gigapaxos.properties edu.umass.cs.gigapaxos.PaxosServer 101

###### On the Second Session
    cd ~/etherpad/etherpad-lite-2
    rm -rf paxos_logs
    java -ea -cp /home/ec2-user/gigapaxos/dist/gigapaxos-1.0.jar -DgigapaxosConfig=/home/ec2-user/gigapaxos/src/edu/umass/cs/gigapaxos/examples/etherpad/gigapaxos.properties edu.umass.cs.gigapaxos.PaxosServer 102
    
###### On the Third Session
    cd ~/etherpad/etherpad-lite-3
    rm -rf paxos_logs
    java -ea -cp /home/ec2-user/gigapaxos/dist/gigapaxos-1.0.jar -DgigapaxosConfig=/home/ec2-user/gigapaxos/src/edu/umass/cs/gigapaxos/examples/etherpad/gigapaxos.properties edu.umass.cs.gigapaxos.PaxosServer 103
    
###### On the Fourth Session (Client)
    cd ~/gigapaxos
    rm -rf paxos_logs
    java -ea -cp /home/ec2-user/gigapaxos/dist/gigapaxos-1.0.jar -DgigapaxosConfig=/home/ec2-user/gigapaxos/src/edu/umass/cs/gigapaxos/examples/etherpad/gigapaxos.properties edu.umass.cs.gigapaxos.examples.etherpad.EtherpadPaxosClient 500
    
 - Note the 500 at the end of the client command -- this is an argument specifying the number of requests to send to the servers.  You can set it to any positive integer you'd like, but there's currently no indication of progress so be prepared to wait if you enter a very large number.
 - When running this command, you'll likely see two error messages: java.io.FileNotFoundException and org.json.JSONException.  These can safely be ignored, or (since the command doesn't contain any relative paths) you can run it from within one of the etherpad-lite-X folders to suppress the errors.

### Verify the Results
 - When finished, EtherpadPaxosClient will print the average delay per request.
 - To verify that the requests were actually processed, you can either:
   - Open a web browser to port X.X.X.X:9001/p/foo (where X.X.X.X is the etherpad server's IP) and confirm that the pad's text consists solely of the word 'bar'
   - Reattach to the screen session in which the etherpad server's run.sh script is running and view its output
