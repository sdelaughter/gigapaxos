###Create EC2 Instance
 - 64-bit linux (t2.micro)
 - Make sure all traffic is open (ignore the security warning)

###SSH to the instance

###Update Packages
    sudo yum update

###Install Dependencies
    sudo yum install gzip git curl python libssl-dev pkg-config build-essential gcc gcc-c++

###Update to Java 1.8
    sudo yum remove java-1.7.0-openjdk
    sudo yum install java-1.8.0
    sudo yum install java-1.8.0-openjdk-devel

###Install Apache Ant
    cd /tmp
    sudo wget http://archive.apache.org/dist/ant/binaries/apache-ant-1.9.0-bin.tar.gz
    sudo tar xzf apache-ant-1.9.0-bin.tar.gz
    sudo mv apache-ant-1.9.0 /usr/local/apache-ant
    echo 'export ANT_HOME=/usr/local/apache-ant' >> ~/.bashrc
    echo 'export PATH=$PATH:/usr/local/apache-ant/bin' >> ~/.bashrc

### Install Node.JS v0.12.6 from source
    cd /tmp
    wget http://nodejs.org/dist/v0.12.6/node-v0.12.6.tar.gz
    tar xvf node-v0.12.6.tar.gz
    cd node-v0.12.6
    ./configure
    make
    sudo make install

### Install Etherpad
    cd
    mkdir etherpad
    cd etherpad
    git clone git://github.com/ether/etherpad-lite.git

### Make sure you can start the Etherpad Server
    ~/etherpad/etherpad-lite/bin/run.sh
    Ctl-c
    
 - Note that this will take longer than usual the first time
 - You will get a warning about DirtyDB being used, ignore this unless you have a specific need for MySQL (DirtyDB is basically just a JSON store)
 - You will also get a warning that no Admin credentials have been set.  This is addressed below. 
 - The server runs indefinitely when started.  When you want to leave it running, you should launch it with screen, but you can just kill it with Ctl-C the first time.
 - Note that there’s no clear indication that the server has started successfully — as soon as you see a message about the plugin admin page (or a warning that admin credentials have not been set), it’s up and running.



### Configure Etherpad
 - Open ~/etherpad/etherpad-lite/settings.json in a text editor
 - Uncomment the “users” object
 - Change the default password for the ‘admin’ and ‘user’ users if desired
 - Make note of the API key in ~/etherpad/etherpad-lite/APIKEY.txt.  You’ll need to include this in any request to the server.  Also note that this file must contain ONLY the one key, on one line — any commented lines will be interpreted as part of the key and will cause requests to be rejected.

### Start the Etherpad Server
    screen ~/etherpad/etherpad-lite/bin/run.sh
 - You can detach from the screen to leave it running in the background by typing Ctrl-a d
 - Once detached, you can reattach the running screen session with screen -x

### Verify that the server is running
 - By default, etherpad runs on port 9001, though if needed you can change this by editing the “port” value in settings.json
 - Open a web browser and navigate to X:9001 (where X is the server’s public IP address or public DNS name).  You should see a screen with a “New Pad” button and a text box in which you can enter the name of a pad to create/open.
 - Note that you can also access the admin console for the etherpad server at X:9001/admin (you’ll be prompted to log in with the admin credentials you set in settings.json in the Configure Etherpad section above).  This will allow you to manage plugins, edit settings.json, and restart the etherpad server.  You can also view information about the server’s current version and its installed plugins, parts, and hooks.

### Clone the gigapaxos repository
    cd
    git clone https://github.com/MobilityFirst/gigapaxos

### Move the files included with this README into place
 - The ‘net’ folder included contains the Java API for etherpad
 - The ‘etherpad’ folder contains the example programs for testing gigapaxos with etherpad

    mv gigapaxos-etherpad/net ~/gigapaxos/src/
    mv gigapaxos-etherpad/etherpad ~/gigapaxos/src/edu/umass/cs/gigapaxos/examples/

### Compile the jar
    cd ~/gigapaxos
    ant

### Start the Servers
    java -ea -cp dist/gigapaxos-1.0.jar paxosConfig=src/edu/umass/cs/gigapaxos/examples/etherpad/gigapaxos.properties edu.umass.cs.gigapaxos.PaxosServer 100 101 102

Note that the included gigapaxos.properties file is set up to run all active replicas (100, 101, 102) on localhost (ports 2000, 2001, 2002)

### Start the Client
    java -ea -cp dist/gigapaxos-1.0.jar -DgigapaxosConfig=src/edu/umass/cs/gigapaxos/examples/etherpad/gigapaxos.properties edu.umass.cs.gigapaxos.examples.etherpad.EtherpadPaxosClient 100

EtherpadPaxosClient takes one argument: the number of requests to send the server(s).  When finished, it will print the average delay per request.
